package fptd.protocols;

import fptd.EdgeServer;
import fptd.Share;

import java.math.BigInteger;
import java.util.List;

public abstract class Gate {

    protected EdgeServer edgeServer;

    private boolean evaluatedOffline = false;
    private boolean evaluatedOnline = false;
    private boolean readOffline = false;

    protected Gate[] inputGates;
    protected Gate inputY;
    protected int dim = 1; // dimension of the vector

    protected List<Share> lambda_share_list; //随机数
    protected List<BigInteger> Delta_clear_list;// = x + lambda，以明文的形式存在，大写的Delta是上三角形

    public void doRunOffline(){
        throw new RuntimeException("Not implemented yet");
    }
    abstract void doReadOfflineFromFile();
    abstract void doRunOnline();

    public Gate(final EdgeServer edgeServer, int dim) {
        this.edgeServer = edgeServer;
        this.dim = dim;
    }

    public Gate(final Gate ... gates) {
        this.inputGates = gates;
        this.edgeServer = gates[0].getEdgeServer();
        this.dim = gates[0].getDim();
    }

//    public void clear(){
//        this.inputGates = null;
//        this.inputY = null;
//        this.lambda_share_list = null;
//        this.Delta_clear_list = null;
//    }

    //*******************************************

    void runOffline() {
        if(this.evaluatedOffline) return;

        if(inputGates != null){
            for(Gate inputGate : inputGates) {//递归
                if(inputGate != null && !inputGate.evaluatedOffline) {
                    inputGate.runOffline();
                }
            }
        }


//        if(inputX != null && ! inputX.evaluatedOffline)
//            inputX.runOffline();
//
//        if(inputY != null && ! inputY.evaluatedOffline)
//            inputY.runOffline();

        this.doRunOffline();
        this.evaluatedOffline = true;
    }

    void readOfflineFromFile() {
        if(this.readOffline) return;
        if(inputGates != null) {
            for(Gate inputGate : inputGates) {//递归
                if(inputGate != null && !inputGate.readOffline) {
                    inputGate.readOfflineFromFile();
                }
            }
        }

//        if(inputX != null && !inputX.readOffline)
//            inputX.readOfflineFromFile();
//        if(inputY != null && ! inputY.readOffline)
//            inputY.readOfflineFromFile();

        this.doReadOfflineFromFile();
        this.readOffline = true;
    }

    void runOnline() {
        if(this.evaluatedOnline) return;

        if(inputGates != null) {
            for(Gate inputGate : inputGates) {//递归
                if(inputGate != null && !inputGate.evaluatedOnline) {
                    inputGate.runOnline();
                }
            }
        }
        this.doRunOnline();
        this.evaluatedOnline = true;
    }



    //*******************************************


    public int getDim() {
        return dim;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public EdgeServer getEdgeServer() {
        return edgeServer;
    }

    public Gate getInputY() {
        return inputY;
    }

    public List<Share> getLambda_share_list() {
        return lambda_share_list;
    }

    public List<BigInteger> getDelta_clear_list() {
        return Delta_clear_list;
    }

    protected Gate firstGate(){
        return inputGates[0];
    }

    protected Gate secondGate(){
        return inputGates[1];
    }

    protected void clear(){
        if(inputGates != null){
            for(int i = 0; i < inputGates.length; i++){
                inputGates[i] = null;
            }
        }

        if(this.lambda_share_list != null)this.lambda_share_list.clear();
        this.lambda_share_list = null;
        if(this.Delta_clear_list != null) this.Delta_clear_list.clear();
        this.Delta_clear_list = null;
        this.inputY = null;
    }
}
