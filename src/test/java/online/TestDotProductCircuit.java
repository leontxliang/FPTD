package online;

import org.junit.Test;
import fptd.EdgeServer;
import fptd.Params;
import fptd.ServerThread;
import fptd.Share;
import fptd.protocols.Circuit;
import fptd.protocols.Gate;
import fptd.protocols.InputGate;
import fptd.protocols.OutputGate;
import fptd.sharing.ShamirSharing;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestDotProductCircuit {




    public void runCircuit(List<Circuit> circuitsOfAllServers) {
        List<Thread> threads = new ArrayList<>();
        for(int owner_idx = 0; owner_idx < Params.NUM_SERVER; owner_idx++){
            Circuit circuit = circuitsOfAllServers.get(owner_idx);
            Thread thread = new Thread(new ServerThread(circuit, owner_idx, owner_idx == 0));
            thread.start();
            threads.add(thread);
            if (0 == owner_idx){//if it is the king
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for(Thread thread : threads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void doRun() {
        //Two workers prepare secrets
        BigInteger secret1 = BigInteger.valueOf(4);
        BigInteger secret2 = Params.P.subtract(BigInteger.valueOf(5));
//        BigInteger secret3 = BigInteger.valueOf(6);
//        BigInteger secret4 = BigInteger.valueOf(7);
        BigInteger secret3 = secret1;
        BigInteger secret4 = secret2;


//        System.out.println("truth = " + secret1.multiply(secret3).add(secret2.multiply(secret4)).mod(Params.P));

        //Get shares
        ShamirSharing sharing = new ShamirSharing();
        List<Share> shares_secret1 = sharing.getShares(secret1);
        List<Share> shares_secret2 = sharing.getShares(secret2);
        List<Share> shares_secret3 = sharing.getShares(secret3);
        List<Share> shares_secret4 = sharing.getShares(secret4);
//        List<Share> shares_secret4 = Collections.nCopies(Params.N, null);

        String jobName = "test";
        List<EdgeServer> servers = new ArrayList<EdgeServer>();
        for(int i = 0; i < Params.NUM_SERVER; i++){
            if(i == 0){
                EdgeServer king = new EdgeServer(true, i, jobName);
                servers.add(king);
            }else{
                servers.add(new EdgeServer(false, i, jobName));
            }
        }
        List<Circuit> circuits = new ArrayList<>();
        for(int i = 0; i < servers.size(); i++){
            Circuit circuit = new Circuit(servers.get(i));
            circuits.add(circuit);
        }

        Gate dotProdGate = null;
        for(int owner_id = 0; owner_id < servers.size(); owner_id++){
            //Send shares to the corresponding edge server
            List<Share> shares_a = List.of(shares_secret1.get(owner_id), shares_secret2.get(owner_id));
            Share temp = shares_secret4.get(owner_id);
            List<Share> shares_b = new ArrayList<>();
            shares_b.add(shares_secret3.get(owner_id));
            shares_b.add(temp);
//            List<Share> shares_c = List.of(shares_secret5.get(owner_id), shares_secret6.get(owner_id));

            Circuit circuitI = circuits.get(owner_id);
            int dim = 2;
            InputGate a = circuitI.input(owner_id, dim);
            InputGate b = circuitI.input(owner_id, dim);
            dotProdGate = circuitI.dotProduct(a, b);
            OutputGate e = circuitI.output(dotProdGate);

            circuitI.addEndpoint(e);

            a.setInput(shares_a);
            b.setInput(shares_b);
        }

        runCircuit(circuits);

//        for(int owner_id = 0; owner_id < servers.size(); owner_id++){
//            Circuit circuitI = circuits.get(owner_id);
//            circuitI.clearOutputGates();
//            circuitI.clearEndpoints();
//            Gate e = circuitI.scaling(dotProdGate, List.of(Params.ROUND_FOR_DIVISION1));
//
//            OutputGate f = circuitI.output(e);
//            circuitI.addEndpoint(f);
//        }
//
//        runCircuit(circuits);
    }
}
