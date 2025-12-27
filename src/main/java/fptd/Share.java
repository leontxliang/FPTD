package fptd;

import java.io.Serializable;
import java.math.BigInteger;


public class Share implements Serializable {

    private int party_id; // start from 0
    private BigInteger shr;

    public Share(int index, BigInteger shr) {
        this.party_id = index;
        this.shr = shr;
    }

    public BigInteger getShr() {
        return shr;
    }

    public Share add(Share shr2) {
        return new Share(this.party_id, this.shr.add(shr2.getShr()).mod(Params.P));
    }

    public Share add(BigInteger shr) {
        return new Share(this.party_id, this.shr.add(shr).mod(Params.P));
    }

    public Share subtract(Share shr2) {
        return new Share(this.party_id, this.shr.subtract(shr2.getShr()).mod(Params.P));
    }

    public Share subtract(BigInteger constant) {
        return new Share(this.party_id, this.shr.subtract(constant).mod(Params.P));
    }

    public int getParty_id() {
        return party_id;
    }

    public Share multiply(BigInteger constant) {
        return new Share(this.party_id, this.shr.multiply(constant).mod(Params.P));
    }

    /**
     * return a new generated Share obj
     */
    public Share setValue(BigInteger newValue) {
        return new Share(this.party_id, newValue.mod(Params.P));
    }

    @Override
    public String toString() {
        return "Share{" +
                "party_id=" + party_id +
                ", shr=" + shr +
                '}';
    }
}
