package fptd.utils;

import static fptd.Params.N;

import fptd.Params;
import fptd.Share;
import fptd.sharing.ShamirSharing;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Tool {

    public static BigInteger getRand() {
        Random random = new Random();
        BigInteger r = new BigInteger(Params.P.bitLength() - 20, random);
        if (r.compareTo(BigInteger.ZERO) < 0) {
            throw new RuntimeException("r is negative");
        }
        return r;
    }

    public static BigInteger getRand(int bitlength) {
        Random random = new Random();
        BigInteger r = new BigInteger(64, random);
//        BigInteger r = BigInteger.valueOf(345435765).multiply(BigInteger.valueOf(1000));
//        int bitlen = BigInteger.valueOf(345435765).multiply(BigInteger.valueOf(1000)).bitLength();
//        BigInteger r = new BigInteger(bitlen, random);
//        BigInteger r = BigInteger.valueOf(345435765).multiply(BigInteger.valueOf(1000));
//        System.out.println("r = " + r);
        if (r.compareTo(BigInteger.ZERO) < 0) {
            throw new RuntimeException("r is negative");
        }
        return r;
    }

    /**
     * recover shares from N servers to get the values in clear
     *
     * @param dim, columns number, also the size of the values for each server
     * @param receivedSharesFromServers, shares from N servers with N rows, dim columns
     * @return
     */
    public static List<BigInteger> openShares2Values(int dim, List<Object> receivedSharesFromServers) {
        List<BigInteger> result = new ArrayList<>();
        ShamirSharing sharing = new ShamirSharing();
        for (int colIdx = 0; colIdx < dim; colIdx++) {
            List<Share> shrsToRecover = new ArrayList<>();
            for (int rowIdx = 0; rowIdx < N; rowIdx++) {
                List<Share> row = (List<Share>) receivedSharesFromServers.get(rowIdx);
                shrsToRecover.add(row.get(colIdx));
            }
            BigInteger lambda = sharing.recover(shrsToRecover);
            result.add(lambda);
        }
        return result;
    }

    public static List<BigInteger> getRands(int size) {
        Random random = new Random();
        List<BigInteger> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(new BigInteger(Params.P.bitLength(), random).mod(Params.P));
        }
        return result;
    }

    /**
     * 计算精确度 for continuous data
     *
     * @param predictTruth 估计的真值
     * @param e2truth 用户验证的真值
     * @param preciseRound 精确度
     * @param metric RMSE 或 MAE
     * @return
     */
    public static double getAccuracy(Map<String, BigInteger> predictTruth,
            Map<String, Double> e2truth,
            long preciseRound, Metric metric,
            boolean isCategorical) {
        if (isCategorical) {
            Map<String, Long> groundTruth = new TreeMap<>();
            for (Map.Entry<String, Double> entry : e2truth.entrySet()) {
                groundTruth.put(entry.getKey(), (long) (preciseRound * entry.getValue()));
            }
            Set<Long> allCategories = new TreeSet<>();
            if (isCategorical) {
                for (Map.Entry<String, Long> entry : groundTruth.entrySet()) {
                    allCategories.add(entry.getValue());
                }
            }
            int tCount = 0;
            int count = 0;
            for (Map.Entry<String, BigInteger> entry : predictTruth.entrySet()) {
                String e = entry.getKey();
                if (!e2truth.containsKey(e)) {
                    continue;
                }
                long predictedValue = entry.getValue().longValue();
                count += 1;
                long truth = groundTruth.get(e);
                long minValue = Long.MAX_VALUE;
                long targetLabel = 0;
                for (long l : allCategories) {
                    long v = Math.abs(predictedValue - l);
                    if (v < minValue) {
                        minValue = v;
                        targetLabel = l;
                    }
                }
                if (targetLabel == truth) {
                    tCount += 1;
                }
            }
            return (double) tCount / (double) count;
        } else {
            double difference = 0.0;
            int count = 0;
            for (Map.Entry<String, BigInteger> entry : predictTruth.entrySet()) {
                String e = entry.getKey();
                if (!e2truth.containsKey(e)) {
                    continue;
                }
                BigInteger predictedValue = entry.getValue();
                count += 1;
                Double truth = e2truth.get(e);
                double ptruth = (double) predictedValue.longValue() / preciseRound;
                if (metric == Metric.RMSE) {
                    difference += Math.pow(ptruth - truth, 2);
                } else if (metric == Metric.MAE) {
                    difference += Math.abs(ptruth - truth);
                } else {
                    throw new RuntimeException("unexpected metric " + metric);
                }
            }
            if (metric == Metric.RMSE) {
                return Math.pow(difference / count, 0.5);
            } else if (metric == Metric.MAE) {
                return difference / count;
            } else {
                throw new RuntimeException("Unsupported metric");
            }
        }
    }

    /*
     * return the byte len of the obj
     * */
    public static int getObjSize(Object obj) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
            out.writeObject(obj);
            out.flush();

//            ObjectInputStream in = new ObjectInputStream();
            return byteOut.toByteArray().length;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public static int countMissingValue(List<List<BigInteger>> missingValuesMatrix, int workerIdx){
//        List<BigInteger> list = missingValuesMatrix.get(workerIdx);
//        int result = 0;
//        for(BigInteger value : list){
//            if(BigInteger.ZERO.equals(value)){
//                result++;
//            }
//        }
//        return result;
//    }

    public static void main(String[] args) {
        Share s1 = new Share(1, BigInteger.ONE);
        Share s2 = new Share(1, BigInteger.ONE);
        Share s3 = new Share(1, BigInteger.ONE);
        List<Share> list = List.of(s1, s2);
        System.out.println(getObjSize(s1));
        System.out.println(getObjSize(s2));
        System.out.println(getObjSize(s3));
        System.out.println(getObjSize(list));
    }
}

