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
    protected static final int numAgents = 100;
    protected static final Disease disease = new Disease(0.2, 0.2, 0.2);

    /*
    public static final double DIAMETER = 8;

    public static final double HEALING_DISTANCE = 20;
    public static final double HEALING_DISTANCE_SQUARED = HEALING_DISTANCE * HEALING_DISTANCE;
    public static final double INFECTION_DISTANCE = 20;
    public static final double INFECTION_DISTANCE_SQUARED = INFECTION_DISTANCE * INFECTION_DISTANCE;

    public static final int NUM_FOOD = 20;
    */

    // Simulation data:
    protected Continuous2D environment;
    protected FoodMaker foodMaker;

    /** Creates a DiseaseSpread simulation with the given random number seed. */
    public DiseaseSpread(long seed)
    {
        super(seed);
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
            schedule.scheduleRepeating(agent); // default interval=1.0
            addedAgents++;
        }

        // Create and schedule a FoodMaker.
        foodMaker = new FoodMaker();
        schedule.scheduleRepeating(schedule.EPOCH, foodMaker, FoodMaker.stepInterval);
    }

    /** Runs the simulation without a GUI. */
    public static void main(String[] args)
    {
        doLoop(DiseaseSpread.class, args);
        System.exit(0);
    }
}
