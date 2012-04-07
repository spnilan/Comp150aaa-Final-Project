/**
 * A disease with parameters.
 */
class Disease
{
    protected double probTransmission;
    protected double probRecovery;
    protected double probInitial;
    protected double energyDrainMultiplier;

    // TODO Adjust the parameters to the disease until something interesting happens

    // prevalent and virulent
    protected static final Disease malaria = new Disease(0.3, 0.02, 0.1, 2.0);
    
    // prevalent but not virulent
    protected static final Disease cold = new Disease(0.3, 0.2, 0.1, 1.2);

    // not prevalent but virulent
    protected static final Disease avianFlu = new Disease(0.05, 0.05, 0.1, 2.5);

    Disease()
    {
        this(0.1, 0.1, 0.1, 1.2);
    }

    Disease(double pt, double pr, double pi, double edm)
    {
        this.probTransmission = pt;
        this.probRecovery = pr;
        this.probInitial = pi;
        this.energyDrainMultiplier = edm;
    }
}
