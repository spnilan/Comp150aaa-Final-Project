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
    protected static final int defaultNumAgents = 20;
    protected static int numAgents;
    protected static final Disease defaultDisease = Disease.diseaseByName("malaria");
    protected static Disease disease;

    // Simulation data:
    protected Continuous2D environment;
    protected FoodMaker foodMaker;

    /**
     * Creates a DiseaseSpread simulation with the given random number seed,
     * number of agents, disease type, and flocking factor.
     */
    public DiseaseSpread(long seed, int numAgents, Disease disease,
                         double flockingFactor)
    {
        super(seed);
        System.out.println("DiseaseSpread: seed=" + seed + " numAgents=" +
                           numAgents + " disease=" + disease.name +
                           " flockingFactor=" + flockingFactor);
        this.numAgents = numAgents;
        this.disease = disease;
        Agent.flockingFactor = flockingFactor;
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

    /**
     * Helper: Returns value of cmdline argument. Stolen from
     * sim.agent.SimState.
     */
    static String argumentForKey(String key, String[] args)
    {
        for(int x = 0; x < args.length - 1; x++) { // key can't be the last string
            if (args[x].equalsIgnoreCase(key)) {
                return args[x + 1];
            }
        }
        return null;
    }

    /** Handles our custom command-line parameters. */
    protected static class SimMaker implements MakesSimState
    {
        /**
         * Returns a new DiseaseSpread instance, handling cmdline parameters.
         */
        public SimState newInstance(long seed, String[] args)
        {
            // We use defaults for any parameters that are not provided.
            int numAgents = DiseaseSpread.defaultNumAgents;
            Disease disease = DiseaseSpread.defaultDisease;
            double flockingFactor = Agent.defaultFlockingFactor;

            String sna = argumentForKey("-num", args);
            if(sna != null) {
                numAgents = Integer.parseInt(sna);
            }

            String sdn = argumentForKey("-disease", args);
            if(sdn != null) {
                disease = Disease.diseaseByName(sdn);
            }

            String sff = argumentForKey("-flocking", args);
            if(sff != null) {
                flockingFactor = Double.parseDouble(sff);
            }

            return new DiseaseSpread(seed, numAgents, disease, flockingFactor);
        }

        public Class simulationClass()
        {
            return DiseaseSpread.class;
        }
    };

    /** Runs the simulation without a GUI. */
    public static void main(String[] args)
    {
        doLoop(new SimMaker(), args);
        System.exit(0);
    }
}
