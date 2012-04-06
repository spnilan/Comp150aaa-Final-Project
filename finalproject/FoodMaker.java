import sim.engine.*;
import sim.util.Double2D;

/**
 * An "agent" that creates food and adds it to the environment.
 */
public class FoodMaker implements Steppable
{
    // Food-maker parameters:
    protected static final double stepInterval = 100.0;
    protected static final int spawnCount = 10;

    /**
     * Creates spawnCount food items and adds them to the environment and the
     * schedule.
     */
    public void step(final SimState state)
    {
        DiseaseSpread sim = (DiseaseSpread)state;

        // Spawn new food items.
        // TODO make clusters of food instead of distributing it randomly
        int addedFood = 0;
        while(addedFood < spawnCount) {
            Double2D loc = new Double2D(sim.random.nextDouble() * sim.xMax, sim.random.nextDouble() * sim.yMax);
            Food item = new Food();
            sim.environment.setObjectLocation(item, loc);
            item.scheduleItem = sim.schedule.scheduleRepeating(item, Food.stepInterval);
            addedFood++;
        }

        System.out.println("FoodMaker stepped");
    }
}
