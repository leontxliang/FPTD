package fptd.utils;

import fptd.Params;
import fptd.Share;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author yihuai liang
 */
public class LinearAlgebra {

    /**
     * element-wise addition
     *
     * @param X
     * @param Y
     * @return
     */
    public static List<Share> addSharesVec(List<Share> X, List<Share> Y) {
        if (X.size() != Y.size()) {
            throw new IllegalArgumentException("X and Y must have the same size.");
        }
        List<Share> result = new ArrayList<Share>();
        Iterator<Share> iterator = X.iterator();
        Iterator<Share> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            Share shareX = iterator.next();
            Share shareY = iteratorY.next();
            if (shareX != null && shareY != null) {
                result.add(shareX.add(shareY));
            } else {
                result.add(null);
            }
        }
        return result;
    }

    public static List<Share> subtractSharesVec(List<Share> X, List<Share> Y) {
        if (X.size() != Y.size()) {
            throw new IllegalArgumentException("X and Y must have the same size.");
        }
        List<Share> result = new ArrayList<Share>();
        Iterator<Share> iterator = X.iterator();
        Iterator<Share> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            Share shareX = iterator.next();
            Share shareY = iteratorY.next();
            if (shareX != null && shareY != null) {
                result.add(shareX.subtract(shareY));
            } else {
                result.add(null);
            }

        }
        return result;
    }

    public static List<BigInteger> subtractBigIntVec(List<BigInteger> X, List<BigInteger> Y) {
        if (X.size() != Y.size()) {
            throw new IllegalArgumentException("X and Y must have the same size.");
        }
        List<BigInteger> result = new ArrayList<>();
        Iterator<BigInteger> iterator = X.iterator();
        Iterator<BigInteger> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            BigInteger valuesX = iterator.next();
            BigInteger valuesY = iteratorY.next();
            if (valuesX != null && valuesY != null) {
                result.add(valuesX.subtract(valuesY).mod(Params.P));
            } else {
                result.add(null);
            }
        }
        return result;
    }

    public static List<Share> subtractVec(List<Share> X, List<BigInteger> Y) {
        if (X.size() != Y.size()) {
            throw new IllegalArgumentException("X and Y must have the same size.");
        }
        List<Share> result = new ArrayList<>();
        Iterator<Share> iterator = X.iterator();
        Iterator<BigInteger> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            Share valuesX = iterator.next();
            BigInteger valuesY = iteratorY.next();
            if (valuesX != null && valuesY != null) {
                result.add(valuesX.subtract(valuesY));
            } else {
                result.add(null);
            }

        }
        return result;
    }

    public static List<Share> subtractVec2(List<BigInteger> X, List<Share> Y) {
        if (X.size() != Y.size()) {
            throw new IllegalArgumentException("X and Y must have the same size.");
        }
        List<Share> result = new ArrayList<>();
        Iterator<BigInteger> iterator = X.iterator();
        Iterator<Share> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            BigInteger valuesX = iterator.next();
            Share valuesY = iteratorY.next();
            if (valuesX != null && valuesY != null) {
                result.add(valuesY.setValue(valuesX.subtract(valuesY.getShr())));
            } else {
                result.add(null);
            }
        }
        return result;
    }

    public static List<Share> addVec(List<Share> X, List<BigInteger> Y) {
        if (X.size() != Y.size()) {
            throw new IllegalArgumentException("X and Y must have the same size.");
        }
        List<Share> result = new ArrayList<>();
        Iterator<Share> iterator = X.iterator();
        Iterator<BigInteger> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            Share valuesX = iterator.next();
            BigInteger valuesY = iteratorY.next();
            if (valuesX != null && valuesY != null) {
                result.add(valuesX.add(valuesY));
            } else {
                result.add(null);
            }

        }
        return result;
    }


    public static List<BigInteger> elemWiseMultiply(List<BigInteger> X, List<BigInteger> Y) {
        if (X.size() != Y.size()) {
            throw new IllegalArgumentException("X and Y must have the same size.");
        }
        List<BigInteger> result = new ArrayList<>();
        Iterator<BigInteger> iterator = X.iterator();
        Iterator<BigInteger> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            BigInteger valuesX = iterator.next();
            BigInteger valuesY = iteratorY.next();
            if (valuesX != null && valuesY != null) {
                result.add(valuesX.multiply(valuesY).mod(Params.P));
            } else {
                result.add(null);
            }

        }
        return result;
    }

    /**
     * 若Y中的元素为false，将X中对应的元素设置为null
     *
     * @param X
     * @param Y
     * @return
     */
    public static <T> List<T> doFilter(List<T> X, List<Boolean> Y) {
        if (X.size() != Y.size()) {
            throw new IllegalArgumentException("X and Y must have the same size.");
        }
        List<T> result = new ArrayList<>();
        Iterator<T> iterator = X.iterator();
        Iterator<Boolean> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            T valuesX = iterator.next();
            Boolean valuesY = iteratorY.next();
            if (valuesY == true) {
                result.add(valuesX);
            } else {
                result.add(null);
            }
        }
        return result;
    }

    public static Share reduceSum(List<Share> xList) {
        Share result = null;
        for (int i = 0; i < xList.size(); i++) {
            if (result == null) {
                result = xList.get(i);
            } else {
                if (xList.get(i) != null) {
                    result = result.add(xList.get(i));
                }
            }
        }
        return result;
    }

    public static List<BigInteger> dotProduct(List<BigInteger> X, List<BigInteger> Y) {
        if (X.size() != Y.size()) {
            throw new IllegalArgumentException("X and Y must have the same size.");
        }
        BigInteger result = BigInteger.ZERO;
        Iterator<BigInteger> iterator = X.iterator();
        Iterator<BigInteger> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            BigInteger valuesX = iterator.next();
            BigInteger valuesY = iteratorY.next();
            if (valuesX != null && valuesY != null) {
                result = result.add(valuesX.multiply(valuesY).mod(Params.P));
            }
        }
        return List.of(result);
    }


    public static List<Share> dotProduct2(List<Share> X, List<BigInteger> Y) {
        if (X.size() != Y.size()) {
            throw new IllegalArgumentException("X and Y must have the same size.");
        }
        BigInteger dotPro = BigInteger.ZERO;
        Iterator<Share> iterator = X.iterator();
        Iterator<BigInteger> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            Share valuesX = iterator.next();
            BigInteger valuesY = iteratorY.next();
            if (valuesX != null && valuesY != null) {
                dotPro = dotPro.add(valuesX.getShr().multiply(valuesY).mod(Params.P));
            }
        }
        return List.of(X.getFirst().setValue(dotPro));
    }

    public static List<Share> elemWiseMultiply2(List<Share> X, List<BigInteger> Y) {
        if (X.size() != Y.size()) {
            throw new IllegalArgumentException("X and Y must have the same size.");
        }
        List<Share> result = new ArrayList<>();
        Iterator<Share> iterator = X.iterator();
        Iterator<BigInteger> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            Share valuesX = iterator.next();
            BigInteger valuesY = iteratorY.next();
            if (valuesX != null && valuesY != null) {
                result.add(valuesX.multiply(valuesY));
            } else {
                result.add(null);
            }

        }
        return result;
    }


    /**
     * element-wise addition
     *
     * @param X
     * @param Y
     * @return
     */
    public static List<BigInteger> addBigIntVec(List<BigInteger> X, List<BigInteger> Y) {
        if (X.size() != Y.size()) {
            throw new IllegalArgumentException("X and Y must have the same size.");
        }
        List<BigInteger> result = new ArrayList<BigInteger>();
        Iterator<BigInteger> iterator = X.iterator();
        Iterator<BigInteger> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            BigInteger shareX = iterator.next();
            BigInteger shareY = iteratorY.next();
            if (shareX != null && shareY != null) {
                result.add(shareX.add(shareY));
            } else {
                result.add(null);
            }

        }
        return result;
    }

    /**
     * element-wise addition
     *
     * @param X
     * @param Y
     * @return
     */
    public static List<List<Share>> addShareMatrix(List<List<Share>> X, List<List<Share>> Y) {
        if (X.isEmpty() || X.size() != Y.size()) {
            throw new IllegalArgumentException("X and Y must have the same size.");
        }
        List<List<Share>> result = new ArrayList<>();

        Iterator<List<Share>> iterator = X.iterator();
        Iterator<List<Share>> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            List<Share> shareX = iterator.next();
            List<Share> shareY = iteratorY.next();
            if (shareX != null && shareY != null) {
                List<Share> shareZ = addSharesVec(shareX, shareY);
                result.add(shareZ);
            } else {
                result.add(null);
            }

        }
        return result;
    }

    /**
     * element-wise addition
     *
     * @param X
     * @param Y
     * @return
     */
    public static List<List<BigInteger>> addBigIntMatrix(List<List<BigInteger>> X, List<List<BigInteger>> Y) {
        if (X.isEmpty() || X.size() != Y.size() || X.get(0).size() != Y.get(0).size()) {
            throw new IllegalArgumentException("X and Y must have the same size.");
        }
        List<List<BigInteger>> result = new ArrayList<>();

        Iterator<List<BigInteger>> iterator = X.iterator();
        Iterator<List<BigInteger>> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            List<BigInteger> shareX = iterator.next();
            List<BigInteger> shareY = iteratorY.next();
            if (shareX != null && shareY != null) {
                List<BigInteger> shareZ = addBigIntVec(shareX, shareY);
                result.add(shareZ);
            } else {
                result.add(null);
            }

        }
        return result;
    }

    public static List<List<Share>> subtractShareMatrix(List<List<Share>> X, List<List<Share>> Y) {
        if (X.isEmpty() || X.size() != Y.size()) {
            throw new IllegalArgumentException("X and Y should not be empty and must have the same size.");
        }
        List<List<Share>> result = new ArrayList<>();

        Iterator<List<Share>> iterator = X.iterator();
        Iterator<List<Share>> iteratorY = Y.iterator();
        while (iterator.hasNext()) {
            List<Share> shareX = iterator.next();
            List<Share> shareY = iteratorY.next();
            if (shareX != null && shareY != null) {
                List<Share> shareZ = subtractSharesVec(shareX, shareY);
                result.add(shareZ);
            } else {
                result.add(null);
            }
        }
        return result;
    }
}
