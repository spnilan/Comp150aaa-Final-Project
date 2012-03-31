
class Disease {
    public double probTransmission;
    public double probRecovery;
    public double probInital;

    Disease() {
        this(0.1, 0.1, 0.1);    
    }

    Disease(double pt, pr, pi) {
        this.probTransmission = pt;
        this.probRecovery = pr;
        this.probInitial = pi;
    }

}
