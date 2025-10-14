package fptd.sharing;

import fptd.Share;

import java.math.BigInteger;
import java.util.List;

public abstract class Sharing {

    abstract List<Share> getShares(BigInteger x);

    abstract BigInteger recover(List<Share> shares);

}
