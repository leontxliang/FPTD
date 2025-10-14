package fptd.offline;

public class OfflineOutputGate extends OfflineGate {

    public OfflineOutputGate(OfflineGate inputX) {
        super(inputX, null);
        this.dim = inputX.dim;
    }

    @Override
    void doRunOffline() {
        //Do nothing
    }
}
