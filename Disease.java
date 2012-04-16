/**
 * A disease with parameters.
 */
class Disease
{
    protected static double highTransmission = .9;
    protected static double lowTransmission = .1;
    protected static double highDrain = 5;
    protected static double lowDrain = 1.15;
    protected double probTransmission;
    protected double probRecovery;
    protected double percentInitial;
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
        this.percentInitial = pi;
        this.energyDrainMultiplier = edm;
        this.name = name;
    }

    static Disease diseaseByName(String name)
    {
        // TODO Adjust the parameters to the disease until something interesting happens
        if(name.equals("malaria")) {
            return new Disease(highTransmission, 0.03, 0.15, highDrain, "malaria"); // prevalent and virulent
        } else if(name.equals("cold")) {
            return new Disease(highTransmission, 0.03, 0.15, lowDrain, "cold"); // prevalent but not virulent
        } else if(name.equals("avian-flu")) {
            return new Disease(lowTransmission, 0.03, 0.15, highDrain, "avian-flu"); // not prevalent but virulent
        } else if(name.equals("martian-cold")) {
	    return new Disease(lowTransmission, 0.03, 0.15, lowDrain, "martian-cold"); //not prevalent nor virulent
        } else if(name.equals("none")) {
            return new Disease(0, 0.0, 0.0, 0, "none"); // no disease
        } else {
            throw new RuntimeException("Unknown disease '" + name + "'");
        }
    }
}
