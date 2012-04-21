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
    protected static final int defaultNumAgentsInitial = 20;
    protected static int numAgentsInitial;
    protected static final Disease defaultDisease = Disease.diseaseByName("malaria");
    protected static Disease disease;

    // Simulation data:
    protected Continuous2D environment;
    protected FoodMaker foodMaker;
    protected int numAgentsAlive;
    protected int numAgentsInfected;
    protected double totalEnergy;
    protected double totalEnergyAgents;
    // TODO: Currently every time the energy of a food item or agent changes, we
    // have to remember to update totalEnergy and totalEnergyAgents. Figure out
    // a way to factor this out so it is in one place, instead of all over the
    // code.

    // Statistics collected and displayed when the simulation ends:


    /**
     * Creates a DiseaseSpread simulation with the given random number seed,
     * number of agents, disease type, and flocking factor.
     */
    public DiseaseSpread(long seed, int numAgentsInitial, Disease disease,
                         double flockingFactor, double observability, double symptomTolerance)
    {
        super(seed);
        System.out.println("DiseaseSpread: seed=" + seed + " numAgentsInitial=" +
                           numAgentsInitial + " disease=" + disease.name +
                           " flockingFactor=" + flockingFactor +
                           " observability=" + observability + 
                           " symptomTolerance=" + symptomTolerance);
        this.numAgentsInitial = numAgentsInitial;
        this.disease = disease;
        Agent.flockingFactor = flockingFactor;
        Agent.observability = observability;
        Agent.symptomTolerance = symptomTolerance;
    }

    /** Returns number of living agents. */
    public int getAgentsAlive()
    {
        return numAgentsAlive;
    }

    /** Returns number of healthy agents. */
    public int getAgentsHealthy()
    {
        return numAgentsAlive - numAgentsInfected;
    }

    /** Returns number of infected agents. */
    public int getAgentsInfected()
    {
        return numAgentsInfected;
    }

    /** Returns total energy (agents + food) in the environment. */
    public double getTotalEnergy()
    {
        return totalEnergy;
    }

    /** Returns total energy of the agents in the environment. */
    public double getTotalEnergyAgents()
    {
        return totalEnergyAgents;
    }

    /** Returns total energy of food items in the environment. */
    public double getTotalEnergyFood()
    {
        return totalEnergy - totalEnergyAgents;
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
        numAgentsAlive = 0;
        numAgentsInfected = 0;
        totalEnergy = 0;
        totalEnergyAgents = 0;
        // Controlling number of initial sick agents.
        double numInitialSickAgents = disease.percentInitial * defaultNumAgentsInitial;

        while(numAgentsAlive < numAgentsInitial) {
            Double2D loc = new Double2D(random.nextDouble() * xMax, random.nextDouble() * yMax);
            boolean infected = false;
            if (numAgentsInfected < numInitialSickAgents) {
              infected = true;
            }
            Agent agent = new Agent(numAgentsAlive, loc, infected);
            environment.setObjectLocation(agent, loc);
            agent.scheduleItem = schedule.scheduleRepeating(agent); // default interval=1.0
            numAgentsAlive++;
            totalEnergy += agent.getEnergy();
            totalEnergyAgents += agent.getEnergy();
            if(infected) {
                numAgentsInfected++;
            }
        }

        // Create and schedule a FoodMaker.
        foodMaker = new FoodMaker();
        schedule.scheduleRepeating(foodMaker); // default interval=1.0
    }
    
    /**
     * Finishes the simulation and displays accumulated stats.
     */
    public void finish()
    {
        super.finish();

        System.out.println("==============================================================================");
        System.out.println("End-of-run statistics:");
        System.out.println("numAgentsAlive=" + numAgentsAlive);
        System.out.println("numAgentsInfected=" + numAgentsInfected);
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

    static boolean hasArgument(String key, String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    static void displayHelp() {

        System.out.println("-disease {none, malaria, avian-flu, cold, martian-cold}");
        System.out.println("-num X                  X number of agents at beginning");
        System.out.println("-flocking X             X is a real number");
        System.out.println("-observability X        X is a double from 0 to 1");
        System.out.println("-symptom-tolerance X    X is a double from 0 to 1"); 

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
            int numAgentsInitial = DiseaseSpread.defaultNumAgentsInitial;
            Disease disease = DiseaseSpread.defaultDisease;
            double flockingFactor = Agent.defaultFlockingFactor;
            double symptomTolerance = Agent.defaultSymptomTolerance;
            double observability = Agent.defaultObservability;

            boolean help = hasArgument("-help", args);
            if (help) {
                displayHelp();
                System.exit(0);
            }

            String sof = argumentForKey("-observability", args);
            if (sof != null) {
                observability = Double.parseDouble(sof);
            }

            String sst = argumentForKey("-symptom-tolerance", args);
            if (sst != null) {
                symptomTolerance = Double.parseDouble(sst);
            }

            String sna = argumentForKey("-num", args);
            if(sna != null) {
                numAgentsInitial = Integer.parseInt(sna);
            }

            String sdn = argumentForKey("-disease", args);
            if(sdn != null) {
                disease = Disease.diseaseByName(sdn);
            }

            String sff = argumentForKey("-flocking", args);
            if(sff != null) {
                flockingFactor = Double.parseDouble(sff);
            }

            return new DiseaseSpread(seed, numAgentsInitial, disease, flockingFactor, 
                                     observability, symptomTolerance);
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

    /** Helper: returns value clamped to [min, max] interval. */
    public static double clamp(double val, double min, double max)
    {
        return Math.min(max, Math.max(val, min));
    }
}
