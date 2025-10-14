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

import static fptd.Params.P;
import static fptd.Params.PRECISE_ROUND;

public class TestDotProdThenDivOnline {

    @Test
    public void testStartCircuit1() throws InterruptedException {
       List<Circuit> circuits = buildCircuit1();

        List<Thread> threads = new ArrayList<>();
        for(int owner_idx = 0; owner_idx < Params.NUM_SERVER; owner_idx++){
            Circuit circuit = circuits.get(owner_idx);
            Thread thread = new Thread(new ServerThread(circuit, owner_idx, owner_idx == 0));
            thread.start();
            threads.add(thread);
            if (0 == owner_idx){//if it is the king
                Thread.sleep(500);
            }
        }
        for(Thread thread : threads){
            thread.join();
        }
    }


    @Test
    public void testStartCircuit2() throws InterruptedException {
        List<Circuit> circuits = buildCircuit2();

        List<Thread> threads = new ArrayList<>();
        for(int owner_idx = 0; owner_idx < Params.NUM_SERVER; owner_idx++){
            Circuit circuit = circuits.get(owner_idx);
            Thread thread = new Thread(new ServerThread(circuit, owner_idx, owner_idx == 0));
            thread.start();
            threads.add(thread);
            if (0 == owner_idx){//if it is the king
                Thread.sleep(500);
            }
        }
        for(Thread thread : threads){
            thread.join();
        }
    }

    public List<Circuit> buildCircuit1() {
        //Two workers prepare secrets
//        BigInteger secret1 = BigInteger.valueOf(40);
        BigInteger secret1 = null;
        BigInteger secret2 = BigInteger.valueOf(50);

        BigInteger secret3 = BigInteger.valueOf(30);
        BigInteger secret4 = BigInteger.valueOf(60);

        BigInteger secret5 = BigInteger.valueOf(7);
        BigInteger secret6 = BigInteger.valueOf(9);

        BigInteger secret7 = BigInteger.valueOf(9);
        BigInteger secret8 = BigInteger.valueOf(14);

        BigInteger secret9 = BigInteger.valueOf(90);
        BigInteger secret10 = BigInteger.valueOf(90);

//        System.out.println("truth = " + secret1.multiply(secret3).divide(secret5));

        //Get shares
        ShamirSharing sharing = new ShamirSharing();
//        List<Share> shares_secret1 = sharing.getShares(secret1);
        List<Share> shares_secret1 = Collections.nCopies(Params.N, null);
        List<Share> shares_secret2 = sharing.getShares(secret2);
        List<Share> shares_secret3 = sharing.getShares(secret3);
        List<Share> shares_secret4 = sharing.getShares(secret4);
        List<Share> shares_secret5 = sharing.getShares(secret5);
        List<Share> shares_secret6 = sharing.getShares(secret6);
        List<Share> shares_secret7 = sharing.getShares(secret7);
        List<Share> shares_secret8 = sharing.getShares(secret8);
        List<Share> shares_secret9 = sharing.getShares(secret9);
        List<Share> shares_secret10 = sharing.getShares(secret10);

        String jobName = "test_dot_prod_then_div";
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

        for(int owner_id = 0; owner_id < servers.size(); owner_id++){
            Circuit circuitI = circuits.get(owner_id);
            int dim = 2;
            InputGate a = circuitI.input(owner_id, dim);
            InputGate b = circuitI.input(owner_id, dim);
            InputGate c = circuitI.input(owner_id, dim);
            InputGate d = circuitI.input(owner_id, dim);
            InputGate e = circuitI.input(owner_id, dim);
            Gate f = circuitI.dotProdThenDivGate(List.of(a, b), List.of(c, d), new OutputGate(e));
            OutputGate g = circuitI.output(f);
            circuitI.addEndpoint(g);

            //Send shares to the corresponding edge server
            List<Share> shares_a = new ArrayList<>();
            shares_a.add(shares_secret1.get(owner_id));
            shares_a.add(shares_secret2.get(owner_id));
            List<Share> shares_b = List.of(shares_secret3.get(owner_id), shares_secret4.get(owner_id));
            List<Share> shares_c = List.of(shares_secret5.get(owner_id), shares_secret6.get(owner_id));
            List<Share> shares_d = List.of(shares_secret7.get(owner_id), shares_secret8.get(owner_id));
            List<Share> shares_e = List.of(shares_secret9.get(owner_id), shares_secret10.get(owner_id));

            a.setInput(shares_a);
            b.setInput(shares_b);
            c.setInput(shares_c);
            d.setInput(shares_d);
            e.setInput(shares_e);
        }
        return circuits;
    }

    public List<Circuit> buildCircuit2() {
        //Two workers prepare secrets
        BigInteger secret1 = BigInteger.valueOf(40);
        BigInteger secret2 = BigInteger.valueOf(50);
        BigInteger secret3 = BigInteger.valueOf(6);
        BigInteger secret4 = BigInteger.valueOf(7);
        BigInteger secret5 = BigInteger.valueOf(7);
        BigInteger secret6 = BigInteger.valueOf(9);
        BigInteger secret7 = BigInteger.valueOf(9);
        BigInteger secret8 = BigInteger.valueOf(9);
        BigInteger secret9 = BigInteger.valueOf(9);
        BigInteger secret10 = BigInteger.valueOf(9);

        System.out.println("truth = " + secret1.multiply(secret3).divide(secret5));

        //Get shares
        ShamirSharing sharing = new ShamirSharing();
        List<Share> shares_secret1 = sharing.getShares(secret1);
        List<Share> shares_secret2 = sharing.getShares(secret2);
        List<Share> shares_secret3 = sharing.getShares(secret3);
        List<Share> shares_secret4 = sharing.getShares(secret4);
        List<Share> shares_secret5 = sharing.getShares(secret5);
        List<Share> shares_secret6 = sharing.getShares(secret6);
        List<Share> shares_secret7 = sharing.getShares(secret7);
        List<Share> shares_secret8 = sharing.getShares(secret8);
        List<Share> shares_secret9 = sharing.getShares(secret9);
        List<Share> shares_secret10 = sharing.getShares(secret10);

        String jobName = "test_dot_prod_then_div";
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

        for(int owner_id = 0; owner_id < servers.size(); owner_id++){
            Circuit circuitI = circuits.get(owner_id);
            int dim = 2;
            InputGate a = circuitI.input(owner_id, dim);
            InputGate b = circuitI.input(owner_id, dim);
            InputGate c = circuitI.input(owner_id, dim);
            InputGate d = circuitI.input(owner_id, dim);
            InputGate e = circuitI.input(owner_id, dim);
            Gate f = circuitI.dotProdThenDivGate(List.of(a, b), List.of(c, d), new OutputGate(e), BigInteger.valueOf(PRECISE_ROUND));
            OutputGate g = circuitI.output(f);
            circuitI.addEndpoint(g);

            //Send shares to the corresponding edge server
            List<Share> shares_a = List.of(shares_secret1.get(owner_id), shares_secret2.get(owner_id));
            List<Share> shares_b = List.of(shares_secret3.get(owner_id), shares_secret4.get(owner_id));
            List<Share> shares_c = List.of(shares_secret5.get(owner_id), shares_secret6.get(owner_id));
            List<Share> shares_d = List.of(shares_secret7.get(owner_id), shares_secret8.get(owner_id));
            List<Share> shares_e = List.of(shares_secret9.get(owner_id), shares_secret10.get(owner_id));

            a.setInput(shares_a);
            b.setInput(shares_b);
            c.setInput(shares_c);
            d.setInput(shares_d);
            e.setInput(shares_e);
        }
        return circuits;
    }
}
