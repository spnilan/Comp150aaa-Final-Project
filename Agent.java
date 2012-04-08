import sim.engine.*;
import sim.util.*;
import java.util.ArrayList;

/**
 * An agent in the simulation. The agent has a certain amount of energy. The
 * agent can be healthy or infected.
 */
public class Agent implements Steppable
{
    // Agent parameters:
    protected static final double initialEnergy = 1000;
    protected static final double energyDrainPerStep = 10;
    protected static final double sensoryRange = 20;
    protected static final double infectionRange = 10;
    protected static final int stepsSatiated = 10;

    protected static final double defaultFlockingFactor = 1.0;
    protected static double flockingFactor;
    protected static double repulsionFactor = 1.0;

    // Agent data:
    public int id;
    public Double2D location;
    public double energy;
    public boolean infected;
    public int stepsUntilCanEat;
    protected Stoppable scheduleItem;

    /** Initializes an agent with the given id and location. */
    public Agent(int id, Double2D location, boolean infected)
    {
        this.id = id;
        this.location = location;
        this.energy = initialEnergy;
        this.infected = infected;
        this.stepsUntilCanEat = 0;
    }

    /** Updates the agent at every step of the simulation. */
    public void step(final SimState state)
    {
        DiseaseSpread sim = (DiseaseSpread)state;

        // Check if the agent has recovered from the disease with a 
        // given probability at each step
        if (infected && sim.random.nextDouble() < sim.disease.probRecovery) {
            infected = false;
        }

        // Drain energy and remove agent from environment & schedule if the
        // energy drops to zero.
        double drain = energyDrainPerStep;
        if(infected) {
            drain *= sim.disease.energyDrainMultiplier;
        }
        energy -= drain;
        if(energy <= 0) {
            sim.environment.remove(this);
            scheduleItem.stop();
            System.out.println("Agent " + id + " died");
            return;
        }

        // Go through the list of nearby items and do something depending on
        // what type they are.
        Bag neighbors = sim.environment.getObjectsWithinDistance(location, sensoryRange);
        Food bestItem = null;
        MutableDouble2D sumForces = new MutableDouble2D();

        ArrayList<Agent> nearbyAgents = new ArrayList<Agent>();

        for(int i = 0; i < neighbors.numObjs; i++) {
            // Doc says getObjectsWithinDistance() may return objects outside the
            // range, so we have to ignore those objects.
            if(sim.environment.getObjectLocation(neighbors.objs[i]).distance(location) > sensoryRange
                    || neighbors.objs[i] == this) {
                continue;
            }
            if(neighbors.objs[i] instanceof Food) {
                Food item = (Food)neighbors.objs[i];
                if(bestItem == null || item.energy > bestItem.energy) {
                    bestItem = item;
                }
            } else if(neighbors.objs[i] instanceof Agent) {
                Agent agent = (Agent)neighbors.objs[i];

                // infect any nearby agents
                if (infected && location.distance(agent.location) <= infectionRange
                        && !agent.infected && sim.random.nextDouble() <= sim.disease.probTransmission) {
                    agent.infected = true;
                }
                nearbyAgents.add(agent);
            }

        }

        // If we can eat and we saw food, eat it.
        if(bestItem != null && bestItem.energy > 0 && stepsUntilCanEat == 0) {
            energy += bestItem.energy;
            bestItem.energy = 0;
            sim.environment.remove(bestItem);
            // bestItem will be removed from schedule on its next step().
            System.out.println("Agent " + id + " ate");
            stepsUntilCanEat = stepsSatiated;
        } else if(stepsUntilCanEat > 0) {
            stepsUntilCanEat--;
        }

        // Move slightly in a random direction.
        // TODO attraction / repulsion; flocking / no flocking cases...

        for (Agent agent : nearbyAgents) {
            double d = location.distance(agent.location);
            if (d == 0) {
                continue;
            }

            MutableDouble2D force = new MutableDouble2D();
            if (!infected && agent.infected) {
               double factor = repulsionFactor / (d*d);
               force.setTo( (location.x - agent.location.x) * factor, (location.y - agent.location.y) * factor);
            } else if (!agent.infected) {
               double factor = flockingFactor / (d*d);
               force.setTo( (agent.location.x - location.x) * factor, (agent.location.y - location.y) * factor);
            }
            sumForces.addIn(force);
        }

        System.out.println("Number of nearby agents: " + nearbyAgents.size());
        System.out.println("Agent " + id + " force: [x=" + sumForces.x + ", y=" + sumForces.y + "]");

        double dx, dy;
        if (sumForces.length() > 0) {
           dx = sumForces.normalize().x;
           dy = sumForces.normalize().y;

           // throw in some randomness too
           dx += 0.2 * sim.random.nextDouble();
           dy += 0.2 * sim.random.nextDouble();

        } else {
            // move in a random direction
            dx = 1.5 - 3* sim.random.nextDouble();
            dy = 1.5 - 3 * sim.random.nextDouble();
        }

        location = new Double2D(location.x + dx, location.y + dy);
        sim.environment.setObjectLocation(this, location);

    }
}
