package fptd.offline;

public class OfflineAddConstantGate extends OfflineGate {

    public OfflineAddConstantGate(OfflineGate inputX) {
        super(inputX, null);
        this.dim = inputX.dim;
    }

    @Override
    void doRunOffline() {
        this.lambda_clear_list = this.firstGate().lambda_clear_list;
        this.lambda_shr_matrix = this.firstGate().lambda_shr_matrix;
    }
}
