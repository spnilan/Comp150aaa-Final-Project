import sim.engine.*;
import sim.util.Double2D;

/**
 * An agent in the simulation. The agent has a certain amount of energy. The
 * agent can be healthy or infected.
 */
public class Agent implements Steppable
{
    // Agent parameters:
    protected static final double initialEnergy = 1000;
    protected static final double energyDrainPerStep = 10;

    // Agent data:
    public int id;
    public Double2D location;
    public double energy;
    public boolean infected;

    /** Initializes an agent with the given id and location. */
    public Agent(int id, Double2D location, boolean infected)
    {
        this.id = id;
        this.location = location;
        this.energy = initialEnergy;
        this.infected = infected;
    }

    /**
     * Updates the agent at every step of the simulation.
     */
    public void step(final SimState state)
    {
        // doesn't do anything right now

        // TODO:
        // remove energy
        // spread disease
        // 2d movement : attaction / repulsion

        DiseaseSpread sim = (DiseaseSpread)state;

        System.out.println("Agent " + id + " has location " + location);

        /*
        Bag neighbors = hb.environment.getObjectsWithinDistance(agentLocation, 10.0 * DiseaseSpread.INFECTION_DISTANCE);
        for (int i=0; i < neighbors.numObjs; i++) {

            if (neighbors.objs[i] != null && neighbors.objs[i] != this) {
                Agent agent = (Agent)neighbors.objs[i];
                if (agent instanceof Food) {
                    Food food = (Food)agent;

                }
                else if (agent instanceof HealthAgent) {
                    HealthAgent opp = (HealthAgent)agent;
                    if (this.isInfected() && 
                           hb.withinInfectionDistance(this, agentLocation, opp, opp.agentLocation)) { 
                        opp.setInfected(true);
                    }
                }
            }
        }

        double dx = 1, dy = 1;
        agentLocation = new Double2D(agentLocation.x + dx, agentLocation.y + dy);
        hb.environment.setObjectLocation(this,agentLocation);
        */
    }
}
