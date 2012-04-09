/**
 * A disease with parameters.
 */
class Disease
{
    protected double probTransmission;
    protected double probRecovery;
    protected double probInitial;
    protected double energyDrainMultiplier;
    protected String name;

    Disease()
    {
        this(0.1, 0.1, 0.1, 1.2, "default");
    }

    Disease(double pt, double pr, double pi, double edm, String name)
    {
        this.probTransmission = pt;
        this.probRecovery = pr;
        this.probInitial = pi;
        this.energyDrainMultiplier = edm;
        this.name = name;
    }

    static Disease diseaseByName(String name)
    {
        // TODO Adjust the parameters to the disease until something interesting happens
        if(name.equals("malaria")) {
            return new Disease(0.3, 0.05, 0.25, 2.0, "malaria"); // prevalent and virulent
        } else if(name.equals("cold")) {
            return new Disease(0.66, 0.2, 0.33, 1.2, "cold"); // prevalent but not virulent
        } else if(name.equals("avian-flu")) {
            return new Disease(0.5, 0.01, 0.1, 5, "avian-flu"); // not prevalent but virulent
        } else if(name.equals("none")) {
            return new Disease(0.0, 0.0, 0.0, 0.0, "none"); // no disease
        } else {
            throw new RuntimeException("Unknown disease '" + name + "'");
        }
    }
}
