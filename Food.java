import sim.engine.*;
import sim.util.Double2D;

/**
 * An food item. The item has a certain amount of energy that decays with time.
 */
public class Food implements Steppable
{
    // Food-item parameters:
    protected static final double stepInterval = 7;
    protected static final double initialEnergy = 120;
    protected static final double energyDrainPerStep = 3;

    // Food-item data:
    protected double energy;
    protected Stoppable scheduleItem;

    /** Initializes a food item. */
    public Food()
    {
        this.energy = initialEnergy;
    }

    /**
     * Decreases the energy of a food item. If the energy drops to zero, removes
     * the item from the environment and the schedule.
     */
    public void step(final SimState state)
    {
        DiseaseSpread sim = (DiseaseSpread)state;
        energy -= energyDrainPerStep;
        sim.totalEnergy -= energyDrainPerStep;
        if(energy <= 0) {
            sim.environment.remove(this);
            scheduleItem.stop();
            // System.out.println("Food item expired");
        }
    }
}
