package fptd.protocols;

import fptd.Params;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static fptd.Params.CONSTANT_FOR_LOG;

public class LogarithmGate extends Gate {

    private Circuit circuit;

    public LogarithmGate(Gate inputX) {
        super(inputX, null);
        this.circuit = new Circuit(this.edgeServer);

        List<BigInteger> constants = new ArrayList<>();
        for(int i = 0; i < dim; i++){
            BigInteger negative = Params.P.subtract(CONSTANT_FOR_LOG);//CONSTANT_FOR_LOG的负数
            constants.add(negative);
        }
        Gate gateMul = this.circuit.addConstant(inputX, constants);
        Gate mulGate = this.circuit.elemMultiply(gateMul, gateMul);
        List<BigInteger> divisors = new ArrayList<>();
        for(int i = 0; i < dim; i++){
            divisors.add(Params.FIXED_DIVISOR_FOR_LOG);
        }
        Gate divFixed = this.circuit.div(mulGate, divisors);
        circuit.addEndpoint(divFixed);
    }

    @Override
    void doReadOfflineFromFile() {
        circuit.readOfflineFromFile();
    }

    @Override
    void doRunOnline() {
        circuit.runOnline();
        this.lambda_share_list = circuit.getEndpoints().getFirst().lambda_share_list;
        this.Delta_clear_list = circuit.getEndpoints().getFirst().Delta_clear_list;
    }


}


