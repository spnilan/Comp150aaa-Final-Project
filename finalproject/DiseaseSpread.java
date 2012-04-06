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
    public static final double XMAX = 800;
    public static final double YMAX = 600;
    public static final int NUM_AGENTS = 100;

    /*
    public static final double DIAMETER = 8;

    public static final double HEALING_DISTANCE = 20;
    public static final double HEALING_DISTANCE_SQUARED = HEALING_DISTANCE * HEALING_DISTANCE;
    public static final double INFECTION_DISTANCE = 20;
    public static final double INFECTION_DISTANCE_SQUARED = INFECTION_DISTANCE * INFECTION_DISTANCE;

    public static final int NUM_FOOD = 20;

    Disease malaria = new Disease(0.2, 0.2, 0.2);
    */

    // Simulation data:
    public Continuous2D environment = null;

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
        environment = new Continuous2D(25.0, XMAX, YMAX);

        // Create and schedule agents.
        int addedAgents = 0;
        while(addedAgents < NUM_AGENTS) {
            Double2D loc = new Double2D(random.nextDouble() * XMAX, random.nextDouble() * YMAX);
            Agent agent = new Agent(addedAgents, loc); // TODO does Agent need to know loc?
            environment.setObjectLocation(agent, loc);
            schedule.scheduleRepeating(agent);
            addedAgents++;

            // TODO: set some agents infected at beginning of simulation
        }
    }

    /** Runs the simulation without a GUI. */
    public static void main(String[] args)
    {
        doLoop(DiseaseSpread.class, args);
        System.exit(0);
    }
}
