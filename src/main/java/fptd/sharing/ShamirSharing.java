package fptd.sharing;

import fptd.Params;
import fptd.Share;

import java.math.BigInteger;
import java.util.*;

import static fptd.Params.N;
import static fptd.Params.T;

public class ShamirSharing extends Sharing {

    private final int threshold;
    private final int totalShares;

    public ShamirSharing() {
        this.threshold = T;
        this.totalShares = N;
    }

    public ShamirSharing(int threshold, int totalShares) {
        if (threshold <= 0 || totalShares <= 0 || threshold > totalShares) {
            throw new IllegalArgumentException("Invalid threshold or total shares");
        }
        this.threshold = threshold;
        this.totalShares = totalShares;
    }



    @Override
    public List<Share> getShares(BigInteger secret) {
        if (secret.compareTo(BigInteger.ZERO) < 0 || secret.compareTo(Params.P) >= 0) {
            throw new IllegalArgumentException("Secret must be in [0, p-1]");
        }

        // Generate random coefficients for the polynomial
        List<BigInteger> coefficients = new ArrayList<>();
        coefficients.add(secret); // a0 is the secret

        Random random = new Random();
        for (int i = 1; i < threshold; i++) {
            BigInteger coeff;
            do {
                coeff = new BigInteger(Params.P.bitLength(), random);
            } while (coeff.compareTo(Params.P) >= 0);
            coefficients.add(coeff);
        }
        // Generate shares by evaluating the polynomial at different points
        List<Share> shares = new ArrayList<>();
        for (int x = 1; x <= totalShares; x++) {
            BigInteger y = evaluatePolynomial(coefficients, BigInteger.valueOf(x));
            //使用x=1开始计算多项式，但Share中的idx表示的是edge server的下标，从0开始
            shares.add(new Share(x-1, y));
        }
        return shares;
    }

    @Override
    public BigInteger recover(List<Share> shares) {
        if (shares == null || shares.size() < threshold) {
            throw new IllegalArgumentException("Not enough shares to recover the secret");
        }
        // Use Lagrange interpolation to recover the constant term (secret)
        BigInteger secret = BigInteger.ZERO;
        for (int i = 0; i < threshold; i++) {
            Share currentShare = shares.get(i);
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            for (int j = 0; j < threshold; j++) {
                if (i == j) continue;

                Share otherShare = shares.get(j);
                int x_other = otherShare.getParty_id() + 1;//
                numerator = numerator.multiply(BigInteger.valueOf(-x_other)).mod(Params.P);
                int x_curr = currentShare.getParty_id() + 1;
                denominator = denominator.multiply(BigInteger.valueOf(x_curr - x_other)).mod(Params.P);
            }
            BigInteger lagrangeCoeff = numerator.multiply(denominator.modInverse(Params.P)).mod(Params.P);
            secret = secret.add(currentShare.getShr().multiply(lagrangeCoeff)).mod(Params.P);
        }
        return secret;
    }

    private BigInteger evaluatePolynomial(List<BigInteger> coefficients, BigInteger x) {
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < coefficients.size(); i++) {
            BigInteger coeff = coefficients.get(i);
            BigInteger xPow = x.pow(i);
            result = result.add(coeff.multiply(xPow)).mod(Params.P);
        }
        return result;
    }

    public static void main(String[] args) {
        BigInteger secret1 = BigInteger.valueOf(4);
        BigInteger secret2 = BigInteger.valueOf(1);
        //Get shares
        ShamirSharing sharing = new ShamirSharing();
        List<Share> shares_secret1 = sharing.getShares(secret1);
        List<Share> shares_secret2 = sharing.getShares(secret2);

        System.out.println("in main = " + sharing.recover(shares_secret1).mod(Params.P));
        System.out.println("in main = " + sharing.recover(shares_secret2));

    }
}

