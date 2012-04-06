import sim.engine.*;
import sim.util.*;

/**
 * An agent in the simulation. The agent has a certain amount of energy. The
 * agent can be healthy or infected.
 */
public class Agent implements Steppable
{
    // Agent parameters:
    protected static final double initialEnergy = 1000;
    protected static final double energyDrainPerStep = 10;
    protected static final double sensoryRange = 10;

    // Agent data:
    public int id;
    public Double2D location;
    public double energy;
    public boolean infected;
    protected Stoppable scheduleItem;

    /** Initializes an agent with the given id and location. */
    public Agent(int id, Double2D location, boolean infected)
    {
        this.id = id;
        this.location = location;
        this.energy = initialEnergy;
        this.infected = infected;
    }

    /** Updates the agent at every step of the simulation. */
    public void step(final SimState state)
    {
        DiseaseSpread sim = (DiseaseSpread)state;

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
        for(int i = 0; i < neighbors.numObjs; i++) {
            // Doc says getObjectsWithinDistance() may return objects outside the
            // range, so we have to ignore those objects.
            if(sim.environment.getObjectLocation(neighbors.objs[i]).distance(location) > sensoryRange) {
                continue;
            }
            if(neighbors.objs[i] instanceof Food) {
                Food item = (Food)neighbors.objs[i];
                if(bestItem == null || item.energy > bestItem.energy) {
                    bestItem = item;
                }
            } else if(neighbors.objs[i] instanceof Agent) {
                // TODO compute forces
            }
        }

        // Eat the best food item found, if any.
        if(bestItem != null && bestItem.energy > 0) {
            energy += bestItem.energy;
            bestItem.energy = 0;
            sim.environment.remove(bestItem);
            // bestItem will be removed from schedule on its next step().
            System.out.println("Agent " + id + " ate");
        }

        // Move slightly in a random direction.
        // TODO attraction / repulsion; flocking / no flocking cases...
        double dx = 5 - 10 * sim.random.nextDouble(),
               dy = 5 - 10 * sim.random.nextDouble();
        location = new Double2D(location.x + dx, location.y + dy);
        sim.environment.setObjectLocation(this, location);

        // If a sick agent is nearby, contract disease with some probability.
        // TODO
    }
}
