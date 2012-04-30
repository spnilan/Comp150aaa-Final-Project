import ec.util.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import java.util.ArrayList;

/**
 * An agent in the simulation. The agent has a certain amount of energy. The
 * agent can be healthy or infected.
 *
 */
public class Agent implements Steppable
{
    // Agent parameters:
    protected static final double initialEnergy = 1000;
    protected static final double satiatedEnergy = 1200;
    protected static final double energyDrainPerStep = 10;
    protected static final double sensoryRange = 20;
    protected static final double infectionRange = 10;
    protected static final double eatingRange = 1;
    protected static final double flockingBenefitRange = 10;
    protected static final int flockingMinOthers = 4;
    protected static final double flockingDrainMultiplier = 0.5;
    protected static final double sharingRange = 10;

    protected static final double sharingConstant = .3;

    // Flocking parameters:
    protected static final double foodFactor = 1.0;
    protected static final double defaultFlockingFactor = 2.5;
    protected static double flockingFactor = defaultFlockingFactor;
    protected static double flockRepulsionFactor = 1.0;
    protected static final double repulsionFactor = 1.1;
    protected static final double randomnessFactor = 1.0;
    protected static final double orientationFactor = 1.4;
    protected static final double separationDistance = 4;
    protected static final double minDistance = 1;

    // Observability parameters:
    protected static final boolean useObservabilityRules = true;
    protected static final double defaultObservability = 0.8;
    protected static final double defaultSymptomTolerance = 0.3;
    protected static double observability = defaultObservability; 
    protected static double symptomTolerance = defaultSymptomTolerance;

    // Agent data:
    public int id;
    public Double2D location;
    public Double2D orientation;
    public double energy;
    public boolean infected;
    public double symptomVisibility;
    protected Stoppable scheduleItem;

    // Attractive / repulsive forces, for visualization.
    public static class Force {
        String name;
        Double2D force;
        double multiplier;

        public Force(String n, Double2D f, double m) {
            name = n;
            force = f;
            multiplier = m;
        }

        public Force(String n, MutableDouble2D f, double m) {
            name = n;
            force = new Double2D(f);
            multiplier = m;
        }
    };
    ArrayList<Force> lastForces;

    /** Initializes an agent with the given id and location. */
    public Agent(int id, Double2D location, boolean infected, double symptomVisibility)
    {
        this.id = id;
        this.location = location;
        this.energy = initialEnergy;
        this.infected = infected;
        this.orientation = new Double2D();
        this.symptomVisibility = symptomVisibility;
        this.lastForces = new ArrayList<Force>();
    }


    /** Returns random symptom visibility for the given infected state. 
     ** If perfect observability, will be actual infected state. */
    public static double calcSymptomVisibility(final DiseaseSpread sim, boolean infected)
    {
        double chance = -1;
        while ((chance > 1) || (chance < 0)) {
            chance = Math.abs (sim.random.nextGaussian() * (1 - observability));
        };
        double symptomVisibility = chance;
        if(infected) {
            symptomVisibility = (1 - chance);
        }
        return symptomVisibility;
    }

    /** Returns true if another agent looks infected from this agent's perspective. */
    public boolean looksInfected(Agent other)
    {
        if (useObservabilityRules) {
            return (other.symptomVisibility > this.symptomTolerance);
        } else {
            return other.infected;
        }
    }

    /** Returns true if the agent is satiated (cannot eat right now). */
    public boolean isSatiated()
    {
        return energy > satiatedEnergy;
    }

    /** Returns true if the agent is infected, for display in the GUI console. */
    public boolean isInfected()
    {
        return infected;
    }

    /** Returns agent's energy, for display in the GUI console. */
    public double getEnergy()
    {
        return energy;
    }

    /** Updates the agent at every step of the simulation. */
    public void step(final SimState state)
    {
        DiseaseSpread sim = (DiseaseSpread)state;

        // Collect nearby items and sort them by type.
        Bag neighbors = sim.environment.getObjectsExactlyWithinDistance(location, sensoryRange, true);
        ArrayList<Agent> nearbyAgents = new ArrayList<Agent>();
        ArrayList<Food> nearbyFood = new ArrayList<Food>();
        for(int i = 0; i < neighbors.numObjs; i++) {
            // Ignore ourselves.
            if(neighbors.objs[i] == this) {
                continue;
            }
            if(neighbors.objs[i] instanceof Food) {
                nearbyFood.add((Food)neighbors.objs[i]);
            } else if(neighbors.objs[i] instanceof Agent) {
                nearbyAgents.add((Agent)neighbors.objs[i]);
            }
        }

        // Drain energy and remove agent from environment & schedule if the
        // energy drops to zero.
        double drain = energyDrainPerStep;
        int withinFlockingRange = 0;
        for(Agent other : nearbyAgents) {
            if(other.location.distance(this.location) <= flockingBenefitRange) {
                withinFlockingRange++;
            }
        }

        // Drain will be less if we are surrounded by other agents
        if(withinFlockingRange >= flockingMinOthers) {
            drain *= flockingDrainMultiplier;
        }
        if(infected) {
            drain *= sim.disease.energyDrainMultiplier;
        }
        double actualDrain = Math.min(drain, energy);
        energy -= drain;
        sim.totalEnergy -= actualDrain;
        sim.totalEnergyAgents -= actualDrain;
        if(energy <= 0) { // agent has died
            sim.environment.remove(this);
            scheduleItem.stop();
            System.out.println("Agent " + id + " died");
            sim.numAgentsAlive--;
            if(infected) {
                sim.numAgentsInfected--;
            }
            return;
        }

        // Take appropriate actions. These are factored out into different
        // functions for readability.
        stepEat(state, nearbyFood, nearbyAgents);
        stepUpdateInfected(state, nearbyAgents);
        stepMove(state, nearbyFood, nearbyAgents);
    }

    /**
     * Go through the list of nearby food and eat the best item we can find.
     * The eaten food item gets removed from the environment and the schedule.
     */
    private void stepEat(final SimState state, ArrayList<Food> nearbyFood, ArrayList<Agent> nearbyAgents)
    {
        DiseaseSpread sim = (DiseaseSpread)state;

        // We can't eat if we are satiated.
        if(isSatiated()) {
            return;
        }

        // Find the item with the most energy that is close enough to eat.
        Food bestItem = null;
        for(Food item: nearbyFood) {
            Double2D itemLoc = sim.environment.getObjectLocation(item);
            if(itemLoc.distance(this.location) > eatingRange) {
                continue;
            }
            if(bestItem == null || item.energy > bestItem.energy) {
                bestItem = item;
            }
        }

        // Eat the best found item, if any.
        if(bestItem != null) {
            ArrayList<Agent> sharingAgents = new ArrayList<Agent>();

            // Share half of food with close-by neighbors
            // In this way, it can be beneficial to be in a flock
            for (Agent guy: nearbyAgents) {
                if (guy.location.distance(this.location) < sharingRange) {
                    sharingAgents.add(guy);
                }
            }
            if (sharingAgents.size() > 0) {
                energy += bestItem.energy; //Removed division; no penalty for sharing.
                for (Agent other : sharingAgents) {
                    //other.energy += bestItem.energy / (2 * sharingAgents.size());
		    other.energy += bestItem.energy * sharingConstant;
                }
            } else {
                //energy += bestItem.energy;
            }

            sim.totalEnergyAgents += bestItem.energy + (sharingAgents.size() * (bestItem.energy * sharingConstant)); 
            bestItem.energy = 0;
            sim.environment.remove(bestItem);
            // bestItem will be removed from schedule on its next step().
            System.out.println("Agent " + id + " ate");
        }
    }

    /**
     * If infected, recovers from disease with some probability. If healthy,
     * gets infected with some probability if there are nearby infected agents.
     */
    private void stepUpdateInfected(final SimState state,
                                    ArrayList<Agent> nearbyAgents)
    {
        DiseaseSpread sim = (DiseaseSpread)state;

        if(infected) {
            // Recover with some probability.
            if(sim.random.nextDouble() <= sim.disease.probRecovery) {
                infected = false;
                symptomVisibility = calcSymptomVisibility(sim, false);
                System.out.println("Agent " + id + " recovered");
                sim.numAgentsInfected--;
            }
        } else {
            // Figure out if there is an infected agent nearby.
            boolean foundInfected = false;
            for(Agent agent: nearbyAgents) {
                if(agent.infected &&
                   agent.location.distance(this.location) <= infectionRange) {
                    foundInfected = true;
                    break;
                }
            }
            // If there is an infected agent nearby, get infected with some probability.
            if(foundInfected && sim.random.nextDouble() <= sim.disease.probTransmission) {
                infected = true;
                symptomVisibility = calcSymptomVisibility(sim, true);
                System.out.println("Agent " + id + " got infected");
                sim.numAgentsInfected++;
            }
        }
    }

    /**
     * Moves in an appropriate direction based on what food items and other
     * agents are nearby. This takes into account infection and flocking
     * parameters.
     */
    private void stepMove(final SimState state,
                          ArrayList<Food> nearbyFood,
                          ArrayList<Agent> nearbyAgents)
    {
        DiseaseSpread sim = (DiseaseSpread)state;

        MutableDouble2D avgOrientation = new MutableDouble2D(this.orientation),
                        foodAttraction = new MutableDouble2D(),
                        flockAttraction = new MutableDouble2D(),
                        flockRepulsion = new MutableDouble2D(),
                        agentRepulsion = new MutableDouble2D();

        // The orientation vector from the previous step
        // If we are flocking, we also account for our neighbors' orientations
        MutableDouble2D sumOrientation = new MutableDouble2D();
        for (Agent other : nearbyAgents) {
            double d = other.location.distance(this.location); 
            if (d < minDistance) {
                d = minDistance;
            }
            Double2D force = other.orientation.multiply(1.0 / (d * d));
            sumOrientation.addIn(force);
        }
        if (sumOrientation.length() > 0) {
            sumOrientation.normalize();
        }
        avgOrientation.addIn(sumOrientation.multiplyIn(0.7));

        // We are always repelled by agents perceived to be infected
        // If we are in flocking mode:
        // We are attracted to healthy agents that are in front of us
        // We are repelled by healthy agents that are too close 
        for (Agent other : nearbyAgents) {
            Double2D force, direction;
            double d = other.location.distance(this.location);
            if (d < minDistance) {
                d = minDistance;
            }

            direction = other.location.subtract(this.location);
            if (direction.length() > 0) {
                if (looksInfected(other)) {
                    force = direction.normalize().multiply(-1.0 / (d * d));
                    agentRepulsion.addIn(force);
                }
                else if (flockingFactor > 0){
                    double dot = direction.normalize().dot(this.orientation);
                    // if leading the other agent
                    if (d <= separationDistance) {
                        force = direction.normalize().multiply(-1 / (d * d));
                        flockRepulsion.addIn(force);
                    }
                    else if (dot > 0) {
                        force = direction.normalize().multiply(dot / (d * d));
                        flockAttraction.addIn(force);
                    }
                }
            }
        }

        // We are attracted to all visible food (vector sum)
        boolean foundFood = false;
        for (Food item : nearbyFood) {
            Double2D itemLoc = sim.environment.getObjectLocation(item);
            if(itemLoc == null) {  // this may be null if we just ate the item
                continue;
            }
            foundFood = true;
            double d = itemLoc.distance(this.location);

            double penaltyFactor = 1;
            for (Agent other : nearbyAgents) {
                double dother = itemLoc.distance(other.location);
                // If another agent is closer, invoke a penalty on the food
                if (dother <= d) {
                    penaltyFactor += (d - dother);
                }
            }
            
            Double2D direction = itemLoc.subtract(this.location);
            if (direction.length() > 0) {
                Double2D force = direction.normalize().multiply(item.energy / (d * d * penaltyFactor));
                foodAttraction.addIn(force);
            }
        }

        MutableDouble2D randomDirection = new MutableDouble2D(
                sim.random.nextDouble() - 0.5, sim.random.nextDouble() - 0.5);
        randomDirection.normalize();

        if (avgOrientation.length() > 0) {
            avgOrientation.normalize();
        }
        if (foodAttraction.length() > 0) {
            foodAttraction.normalize();
        }
        if (flockAttraction.length() > 0) {
            flockAttraction.normalize();
        }
        if (flockRepulsion.length() > 0) {
            flockRepulsion.normalize();
        }
        if (agentRepulsion.length() > 0) {
            agentRepulsion.normalize();
        }

        // If we don't see anything, then avgOrientation will dominate
        // randomDirection, and we will keep moving in the same direction
        // forever. To avoid this, if we don't see anything, we randomly set
        // avgOrientation to zero with a 1/10 probability.
        if (nearbyFood.isEmpty() && nearbyAgents.isEmpty() && sim.random.nextDouble() < 0.1) {
            avgOrientation.zero();
        }

        MutableDouble2D sumForces = new MutableDouble2D();
        sumForces.addIn(avgOrientation.multiplyIn(orientationFactor))
                 .addIn(foodAttraction.multiplyIn(foodFactor * satiatedEnergy / energy))
                 .addIn(flockAttraction.multiplyIn(flockingFactor))
                 .addIn(flockRepulsion.multiplyIn(flockRepulsionFactor * flockingFactor))
                 .addIn(agentRepulsion.multiplyIn(repulsionFactor))
                 .addIn(randomDirection.multiplyIn(randomnessFactor));

        if(sumForces.length() > 0) {
            sumForces.normalize();
        }

        // update our orientation and position in the environment
        this.orientation = new Double2D(sumForces);
        sumForces.addIn(this.location);
        sumForces.x = DiseaseSpread.clamp(sumForces.x, 0, DiseaseSpread.xMax);
        sumForces.y = DiseaseSpread.clamp(sumForces.y, 0, DiseaseSpread.yMax);
        this.location = new Double2D(sumForces);
        sim.environment.setObjectLocation(this, this.location);

        // Save forces for the visualization.
        lastForces.clear();
        lastForces.add(new Force("avgOrientation", avgOrientation, 1.0));
        lastForces.add(new Force("foodAttraction", foodAttraction, 1.0));
        lastForces.add(new Force("flockAttraction", flockAttraction, 1.0));
        lastForces.add(new Force("agentRepulsion", agentRepulsion, 1.0));
        lastForces.add(new Force("randomDirection", randomDirection, 1.0));
    }
}
