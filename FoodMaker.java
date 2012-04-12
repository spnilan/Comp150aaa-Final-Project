import sim.engine.*;
import sim.util.Double2D;

/**
 * An "agent" that creates food and adds it to the environment. The FoodMaker
 * tries to achieve conservation of energy. It always tries to maintain
 * numAgentsAlive * targetEnergyPerAgent total energy in the simulation.
 */
public class FoodMaker implements Steppable
{
    // Food-maker parameters:
    protected static final double targetEnergyPerAgent = Agent.initialEnergy * 1.5;
    protected static final double energyDeficitThreshold = 500;
    protected static final int spawnCount = 15;
    protected static final double clusterVariance = 3;

    /**
     * If the totalEnergy of the simulation is smaller than numAgentsAlive *
     * targetEnergyPerAgent by more than energyDeficitThreshold, creates
     * spawnCount food items and adds them to the environment and the schedule.
     */
    public void step(final SimState state)
    {
        DiseaseSpread sim = (DiseaseSpread)state;

        double totalEnergyTarget = sim.getAgentsAlive() * targetEnergyPerAgent;
        if(totalEnergyTarget - sim.totalEnergy > energyDeficitThreshold) {
            // Spawn new food items in a Gaussian cluster.
            double cx = sim.random.nextDouble() * sim.xMax,
                   cy = sim.random.nextDouble() * sim.yMax;
            int addedFood = 0;
            while(addedFood < spawnCount) {
                double dx = sim.random.nextGaussian() * clusterVariance,
                       dy = sim.random.nextGaussian() * clusterVariance;
                Double2D loc = new Double2D(cx + dx, cy + dy);
                Food item = new Food();
                sim.environment.setObjectLocation(item, loc);
                item.scheduleItem = sim.schedule.scheduleRepeating(item, Food.stepInterval);
                addedFood++;
                sim.totalEnergy += item.energy;
            }
            System.out.println("FoodMaker spawned food");
        }
    }
}
