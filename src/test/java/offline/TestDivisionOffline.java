package offline;

import org.junit.Test;
import fptd.Params;
import fptd.offline.OfflineCircuit;
import fptd.offline.OfflineGate;
import fptd.offline.FakeParty;

import static fptd.Params.N;


public class TestDivisionOffline {

    @Test
    public void testFakeCircuit(){
        FakeParty fakeParty = new FakeParty("test", N);
        OfflineCircuit fakeCircuit = new OfflineCircuit(fakeParty);

        OfflineGate a = fakeCircuit.input(0, 2);
        OfflineGate b = fakeCircuit.input(0, 2);
//        FakeGate c = fakeCircuit.input(0, 2);
        OfflineGate aa = fakeCircuit.scaling(a, Params.FIXED_DIVISOR_FOR_LOG);
        OfflineGate d = fakeCircuit.divide(aa);
        OfflineGate e = fakeCircuit.log(d);
        OfflineGate f = fakeCircuit.output(e);
        fakeCircuit.addEndpoint(f);
        fakeCircuit.runOffline();
    }


    @Test
    public void testDivisionWithAddition(){
        FakeParty fakeParty = new FakeParty("test", N);
        OfflineCircuit fakeCircuit = new OfflineCircuit(fakeParty);

        OfflineGate a = fakeCircuit.input(0, 2);
        OfflineGate b = fakeCircuit.input(0, 2);
//        FakeGate c = fakeCircuit.input(0, 2);
        OfflineGate d = fakeCircuit.divide(a);
        OfflineGate e = fakeCircuit.divide(b);
        OfflineGate f = fakeCircuit.add(d, e);

        OfflineGate out1 = fakeCircuit.output(d);
        OfflineGate out2 = fakeCircuit.output(e);
        OfflineGate out3 = fakeCircuit.output(f);
        fakeCircuit.addEndpoint(out1);
        fakeCircuit.addEndpoint(out2);
        fakeCircuit.addEndpoint(out3);
        fakeCircuit.runOffline();
    }

}
