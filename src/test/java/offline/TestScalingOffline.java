package offline;

import org.junit.Test;
import fptd.Params;
import fptd.offline.OfflineCircuit;
import fptd.offline.OfflineGate;
import fptd.offline.FakeParty;

import static fptd.Params.N;


public class TestScalingOffline {

    @Test
    public void testFakeCircuit(){
        FakeParty fakeParty = new FakeParty("test", N);
        OfflineCircuit fakeCircuit = new OfflineCircuit(fakeParty);

        OfflineGate a = fakeCircuit.input(0, 2);
//        FakeGate b = fakeCircuit.input(0, 2);
//        FakeGate c = fakeCircuit.input(0, 2);
        OfflineGate d = fakeCircuit.scaling(a, Params.FIXED_DIVISOR_FOR_LOG);
//        FakeGate e = fakeCircuit.add(d, c);
        OfflineGate f = fakeCircuit.output(d);
        fakeCircuit.addEndpoint(f);
        fakeCircuit.runOffline();
    }

}
