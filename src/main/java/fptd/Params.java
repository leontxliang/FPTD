package fptd;

import java.math.BigInteger;

public class Params {

    public static final boolean IS_PRINT_EXE_INFO = true;

    public static final int NUM_SERVER = 7;
    public static final int N = NUM_SERVER;
    public static final int T = (N/2) + 1;
    public static int ITER_TD = 3;

    public static final BigInteger P = new BigInteger("3351951982485649274893506249551461531869841455148098344430890360930441007518386744200468574541725856922507964546621512713438470702986642486608412251521039");

    public static final String IP_King = "127.0.0.1";
    public static final int Port_King = 8874;

    public static final long PRECISE_ROUND = (long)Math.pow(10, 5);

    public static final String FAKE_OFFLINE_DIR = "./offline_data/";

    private static final int exp = 13;
    public static final BigInteger CONSTANT_FOR_LOG = BigInteger.valueOf(10).pow(exp);
    public static final BigInteger FIXED_DIVISOR_FOR_LOG = BigInteger.valueOf(10).pow(exp - 1);

//    public final static String sensingDataFile = "datasets/d_Duck_Identification/answer.csv";
//    public final static String truthFile = "datasets/d_Duck_Identification/truth.csv";
//    public final static boolean isCategoricalData = true;

//        public final static String sensingDataFile = "datasets/s4_Dog_data/answer.csv";
//        public final static String truthFile = "datasets/s4_Dog_data/truth.csv";
//        public final static boolean isCategoricalData = true;

        public final static String sensingDataFile = "datasets/weather/answer.csv";
        public final static String truthFile = "datasets/weather/truth.csv";
        public final static boolean isCategoricalData = false;
}






