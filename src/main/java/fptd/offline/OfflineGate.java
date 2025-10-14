package fptd.offline;

import fptd.Share;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public abstract class OfflineGate {

    protected FakeParty fakeParty;
    private boolean evaluatedOffline = false;

    protected int dim;
    protected OfflineGate[] inputGates;

    protected List<BigInteger> lambda_clear_list = new ArrayList<>();
    protected List<List<Share>> lambda_shr_matrix = new ArrayList<>();

    public OfflineGate(FakeParty edgeServer, int dim) {
        this.fakeParty = edgeServer;
        this.dim = dim;
    }

    public OfflineGate(OfflineGate... inputGates) {
        if(inputGates == null || inputGates.length == 0) {
            throw new IllegalArgumentException("inputGates is null or empty");
        }

        this.inputGates = inputGates;
        this.fakeParty = firstGate().fakeParty;
        this.dim = firstGate().dim;
    }

    abstract void doRunOffline();

    public void runOffline() {
        if(this.evaluatedOffline) return;

        if(inputGates != null) {
            for(OfflineGate gate : inputGates) {
                if(gate != null && !gate.evaluatedOffline){
                    gate.runOffline();
                }
            }
        }



//        if(inputX != null && !inputX.evaluatedOffline)
//            inputX.runOffline();
//
//        if(inputY != null && !inputY.evaluatedOffline)
//            inputY.runOffline();

        this.doRunOffline();

        this.evaluatedOffline = true;
    }


    protected OfflineGate firstGate(){
        return this.inputGates[0];
    }

    protected OfflineGate secondGate(){
        return this.inputGates[1];
    }

    protected OfflineGate getIthGate(int i){
        if(i >= inputGates.length) throw new IllegalArgumentException("Illegal index");
        return this.inputGates[i];
    }
}
