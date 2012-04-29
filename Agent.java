import ec.util.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import java.util.ArrayList;

/**
 * An agent in the simulation. The agent has a certain amount of energy. The
 * agent can be healthy or infected.
 *
 * Uses some flocking code by Sean Luke ('flockers' example in Mason).
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

    // Flocking parameters:
    protected double cohesionFactor = 1.0;
    protected double avoidanceFactor = 1.0;
    protected double randomnessFactor = 1.0;
    protected double consistencyFactor = 1.0;
    protected double momentumFactor = 1.0;
    protected double foodAttractionFactor = 1.0;
    protected double diseaseAvoidanceFactor = 1.0;

    // XXX obsolete:
    protected static final double defaultFlockingFactor = 2.5;
    protected static double flockingFactor = defaultFlockingFactor;
    protected static double flockRepulsionFactor = 0.7;
    protected static final double repulsionFactor = 1.1;
    protected static final double orientationFactor = 1.4;
    protected static final double separationDistance = 8;
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
    public Double2D lastDirection;
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
    };
    ArrayList<Force> lastForces;

    /** Initializes an agent with the given id and location. */
    public Agent(int id, Double2D location, boolean infected, double symptomVisibility)
    {
        this.id = id;
        this.location = location;
        this.energy = initialEnergy;
        this.infected = infected;
        this.lastDirection = new Double2D();
        this.symptomVisibility = symptomVisibility;
        this.lastForces = new ArrayList<Force>();
    }

    /** Returns random symptom visibility for the given infected state. */
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

    /** Returns true if another agent look infected from this agent's perspective. */
    public boolean looksInfected(Agent guy)
    {
        if (useObservabilityRules) {
            return (guy.symptomVisibility > this.symptomTolerance);
        } else {
            return guy.infected;
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

        /* 
           System.out.println("Agent " + id + " sees " + nearbyFood.size() + " food");
           String s = new String("Agent " + id + " sees ");
           if (nearbyAgents.size() == 0) {
           s += "no agents";
           }
           for (Agent other : nearbyAgents) {
           s += " " + other.id + ",";
           }
           System.out.println(s); 
           */

        // Drain energy and remove agent from environment & schedule if the
        // energy drops to zero.
        double drain = energyDrainPerStep;
        int withinFlockingRange = 0;
        for(Agent other : nearbyAgents) {
            if(other.location.distance(this.location) <= flockingBenefitRange) {
                withinFlockingRange++;
            }
        }
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
        if(energy <= 0) {
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
        stepEat(state, nearbyFood);
        stepUpdateInfected(state, nearbyAgents);
        stepMove(state, nearbyFood, nearbyAgents);
    }

    /**
     * Go through the list of nearby food and eat the best item we can find.
     * The eaten food item gets removed from the environment and the schedule.
     */
    private void stepEat(final SimState state, ArrayList<Food> nearbyFood)
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
            energy += bestItem.energy;
            sim.totalEnergyAgents += bestItem.energy;
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

        // Modified "boids" flocking algorithm.
        Double2D avoidance = avoidance(nearbyAgents, sim.environment);
        Double2D cohesion = cohesion(nearbyAgents, sim.environment);
        Double2D randomness = randomness(sim.random);
        Double2D consistency = consistency(nearbyAgents, sim.environment);
        Double2D momentum = momentum();
        Double2D foodAttraction = food(nearbyFood, sim.environment);
        Double2D diseaseAvoidance = diseaseAvoidance(nearbyAgents, sim.environment);
        System.out.println("avoidance: " + avoidance);
        System.out.println("cohesion: " + cohesion);
        System.out.println("randomness: " + randomness);
        System.out.println("consistency: " + consistency);
        System.out.println("momentum: " + momentum);
        System.out.println("food: " + foodAttraction);
        System.out.println("diseaseAvoidance: " + diseaseAvoidance);

        // We are more attracted to food if we're hungry.
        // double actualMomentumFactor = momentumFactor * (initialEnergy / getEnergy());

        // Sum all forces.
        double dx = cohesionFactor * cohesion.x +
                    avoidanceFactor * avoidance.x +
                    consistencyFactor * consistency.x +
                    randomnessFactor * randomness.x +
                    momentumFactor * momentum.x +
                    foodAttractionFactor * foodAttraction.x +
                    diseaseAvoidanceFactor * diseaseAvoidance.x;
        double dy = cohesionFactor * cohesion.y +
                    avoidanceFactor * avoidance.y +
                    consistencyFactor * consistency.y +
                    randomnessFactor * randomness.y +
                    momentumFactor * momentum.y +
                    foodAttractionFactor * foodAttraction.y +
                    diseaseAvoidanceFactor * diseaseAvoidance.y;
        double dis = Math.sqrt(dx * dx + dy * dy);
        if (dis > 0) {
            dx = dx / dis * 1.0;
            dy = dy / dis * 1.0;
        }
        lastDirection = new Double2D(dx, dy);
        System.out.println("normalized sum: " + lastDirection + "\n");
        location = new Double2D(sim.environment.stx(location.x + dx), sim.environment.sty(location.y + dy));
        sim.environment.setObjectLocation(this, location);

        // Save forces for the visualization.
        lastForces.clear();
        lastForces.add(new Force("cohesion", cohesion, cohesionFactor));
        lastForces.add(new Force("avoidance", avoidance, avoidanceFactor));
        lastForces.add(new Force("consistency", consistency, consistencyFactor));
        lastForces.add(new Force("randomness", randomness, randomnessFactor));
        lastForces.add(new Force("momentum", momentum, momentumFactor));
        lastForces.add(new Force("foodAttraction", foodAttraction, foodAttractionFactor));
        lastForces.add(new Force("diseaseAvoidance", diseaseAvoidance, diseaseAvoidanceFactor));
    }

    // Returns "momentum" component of "boids" flocking algorithm.
    public Double2D momentum()
    {
        return lastDirection;
    }

    // Returns "consistency" component of "boids" flocking algorithm.
    public Double2D consistency(ArrayList<Agent> nearbyAgents, Continuous2D environment)
    {
        if (nearbyAgents == null || nearbyAgents.size() == 0)
            return new Double2D(0, 0);

        double x = 0; 
        double y = 0;
        int count = 0;
        for(Agent other : nearbyAgents) {
            double dx = environment.tdx(location.x, other.location.x);
            double dy = environment.tdy(location.y, other.location.y);
            Double2D m = other.momentum();
            count++;
            x += m.x;
            y += m.y;
        }
        if (count > 0) {
            x /= count;
            y /= count;
        }
        return new Double2D(x, y);
    }

    // Returns "cohesion" component of "boids" flocking algorithm.
    public Double2D cohesion(ArrayList<Agent> nearbyAgents, Continuous2D environment)
    {
        if (nearbyAgents == null || nearbyAgents.size() == 0)
            return new Double2D(0, 0);
        
        double x = 0; 
        double y = 0;        
        int count = 0;
        for(Agent other : nearbyAgents) {
            double dx = environment.tdx(location.x, other.location.x);
            double dy = environment.tdy(location.y, other.location.y);
            count++;
            x += dx;
            y += dy;
        }
        if (count > 0) {
            x /= count;
            y /= count;
        }
        return new Double2D(-x / 10, -y / 10);
    }
 
    // Returns "avoidance" component of "boids" flocking algorithm.
    // (Avoids very close agents, for flocking. Does not care whether others are
    // infected.)
    public Double2D avoidance(ArrayList<Agent> nearbyAgents, Continuous2D environment)
    {
        if (nearbyAgents == null || nearbyAgents.size() == 0)
            return new Double2D(0, 0);

        double x = 0;
        double y = 0;
        int count = 0;
        for(Agent other : nearbyAgents) {
            double dx = environment.tdx(location.x, other.location.x);
            double dy = environment.tdy(location.y, other.location.y);
            double lensquared = dx * dx + dy * dy;
            count++;
            x += dx / (lensquared * lensquared + 1);
            y += dy / (lensquared * lensquared + 1);
        }
        if (count > 0) {
            x /= count;
            y /= count;
        }
        return new Double2D(400 * x, 400 * y);      
    }

    // Returns "randomness" component of the "boids" flocking algorithm.
    public Double2D randomness(MersenneTwisterFast r)
    {
        double x = r.nextDouble() * 2 - 1.0;
        double y = r.nextDouble() * 2 - 1.0;
        double l = Math.sqrt(x * x + y * y);
        return new Double2D(0.05 * x / l, 0.05 * y / l);
    }

    // Returns our new "food" component for the "boids" flocking algorithm.
    public Double2D food(ArrayList<Food> nearbyFood, Continuous2D environment)
    {
        if (nearbyFood == null || nearbyFood.size() == 0)
            return new Double2D(0, 0);
        
        double x = 0; 
        double y = 0;        
        for(Food item : nearbyFood) {
            Double2D itemLoc = environment.getObjectLocation(item);
            if(itemLoc == null) {  // this may be null if we just ate the item
                continue;
            }
            double dx = environment.tdx(location.x, itemLoc.x);
            double dy = environment.tdy(location.y, itemLoc.y);
            double distSq = dx * dx + dy * dy;
            x += -dx / distSq;
            y += -dy / distSq;
        }
        double l = Math.sqrt(x * x + y * y);
        if(l == 0) {
            l = 1;
        }
        return new Double2D(20 * x / l, 20 * y / l);
    }

    // Returns "disease avoidance" component of our modified "boids" flocking
    // algorithm. (Avoids sick agents.)
    public Double2D diseaseAvoidance(ArrayList<Agent> nearbyAgents, Continuous2D environment)
    {
        if (nearbyAgents == null || nearbyAgents.size() == 0)
            return new Double2D(0, 0);

        double x = 0;
        double y = 0;
        int count = 0;
        for(Agent other : nearbyAgents) {
            if(!looksInfected(other)) {
                continue;
            }
            double dx = environment.tdx(location.x, other.location.x);
            double dy = environment.tdy(location.y, other.location.y);
            double lensquared = dx * dx + dy * dy;
            count++;
            x += dx / (lensquared + 1);
            y += dy / (lensquared + 1);
        }
        if (count > 0) {
            x /= count;
            y /= count;
        }
        return new Double2D(50 * x, 50 * y);      
    }
}
