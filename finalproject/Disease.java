/**
 * A disease with parameters.
 */
class Disease
{
    protected double probTransmission;
    protected double probRecovery;
    protected double probInitial;
    protected double energyDrainMultiplier;

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
