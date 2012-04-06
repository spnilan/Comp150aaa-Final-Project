/**
 * A disease with parameters.
 */
class Disease
{
    public double probTransmission;
    public double probRecovery;
    public double probInitial;

    Disease()
    {
        this(0.1, 0.1, 0.1);    
    }

    Disease(double pt, double pr, double pi)
    {
        this.probTransmission = pt;
        this.probRecovery = pr;
        this.probInitial = pi;
    }
}
