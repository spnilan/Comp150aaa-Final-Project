import sim.field.continuous.*;
import sim.engine.*;
import sim.util.*;

/**
 * Encapsulates the disease-spreading simulation, with no GUI. Useful for batch
 * runs.
 */
public class DiseaseSpread extends SimState
{
    // Simulation parameters:
    protected static final double xMax = 80;
    protected static final double yMax = 60;
    protected static final int numAgents = 1;
    protected static final Disease disease = Disease.malaria; 

    // Simulation data:
    protected Continuous2D environment;
    protected FoodMaker foodMaker;

    /** Creates a DiseaseSpread simulation with the given random number seed. */
    public DiseaseSpread(long seed)
    {
        super(seed);
    }

    /** Stop the simulation when all agents are dead */
    public boolean allAgentsDead() {
        Bag bag = environment.getAllObjects();
        for (int i=0; i < bag.numObjs; i++) {
            if (bag.objs[i] instanceof Agent) {
                return false;
            }
        }
        return true;
    }

    /** Or when the disease has been eradicated */
    public boolean allAgentsHealthy() {
        Bag bag = environment.getAllObjects();
        for (int i=0; i < bag.numObjs; i++) {
            if (bag.objs[i] instanceof Agent) {
                Agent agent = (Agent)bag.objs[i];
                if (agent.infected) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Starts the simulation.
     */
    public void start()
    {
        super.start();

        // Set up environment.
        environment = new Continuous2D(25.0, xMax, yMax);

        // Create and schedule agents.
        int addedAgents = 0;
        while(addedAgents < numAgents) {
            Double2D loc = new Double2D(random.nextDouble() * xMax, random.nextDouble() * yMax);
            boolean infected = (random.nextDouble() < disease.probInitial);
            Agent agent = new Agent(addedAgents, loc, infected);
            environment.setObjectLocation(agent, loc);
            agent.scheduleItem = schedule.scheduleRepeating(agent); // default interval=1.0
            addedAgents++;
        }

        // Create and schedule a FoodMaker.
        foodMaker = new FoodMaker();
        schedule.scheduleRepeating(schedule.EPOCH, foodMaker, FoodMaker.stepInterval);
    }

    //TODO Figure out how to take in a command line arg for disease (malaria, etc)

    /** Runs the simulation without a GUI. */
    public static void main(String[] args)
    {
        doLoop(DiseaseSpread.class, args);
        System.exit(0);
    }
}
