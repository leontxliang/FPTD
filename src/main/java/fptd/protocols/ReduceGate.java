package fptd.protocols;

import fptd.Params;
import fptd.Share;

import java.math.BigInteger;
import java.util.List;

public class ReduceGate extends Gate {
    private List<Share> filter = null; //只关注到这个filter中哪些元素是null的

    public ReduceGate(Gate inputX) {
        super(inputX, null);
        this.dim = 1;
    }

    // filter中部分元素可能为null，将InputX所对应的这些元素也设置为null
    public ReduceGate(Gate inputX, List<Share> filter) {
        super(inputX, null);
        this.dim = 1;
        this.filter = filter;
    }

    @Override
    void doReadOfflineFromFile() {}

    @Override
    void doRunOnline() {
        Share sumLambda = null;
        final int size = this.firstGate().lambda_share_list.size();
        for(int i = 0; i < size; i++){
            Share s = this.firstGate().lambda_share_list.get(i);
            if(this.firstGate().Delta_clear_list.get(i) != null && filter != null && filter.get(i) != null){
                if(sumLambda != null){//只取非空的值
                    sumLambda = sumLambda.add(s);
                }else{
                    sumLambda = s;
                }
            }
        }
        BigInteger sum2 = BigInteger.ZERO; //需要考虑到存在null的情况
        for(int i = 0; i < size; i++){
            BigInteger temp = this.firstGate().Delta_clear_list.get(i);
            if(temp != null && filter != null && filter.get(i) != null) {
                sum2 = sum2.add(temp);
            }
        }
        this.lambda_share_list = List.of(sumLambda);
        this.Delta_clear_list = List.of(sum2);


    }
}
