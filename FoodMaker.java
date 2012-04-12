import sim.engine.*;
import sim.util.Double2D;

/**
 * An "agent" that creates food and adds it to the environment.
 */
public class FoodMaker implements Steppable
{
    // Food-maker parameters:
    protected static final double energyDeficitThreshold = 1000;
    protected static final int spawnCount = 15;
    protected static final double clusterVariance = 3;

    // Food-maker data:
    protected double totalEnergyTarget;

    /**
     * Creates a FoodMaker that tries to maintain the given totalEnergyTarget.
     */
    public FoodMaker(double totalEnergyTarget)
    {
        this.totalEnergyTarget = totalEnergyTarget;
    }

    /**
     * If the totalEnergy of the simulation is smaller than totalEnergyTarget by
     * more than energyDeficitThreshold, creates spawnCount food items and adds
     * them to the environment and the schedule.
     */
    public void step(final SimState state)
    {
        DiseaseSpread sim = (DiseaseSpread)state;

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
