package offline;

import org.junit.Test;
import fptd.Params;
import fptd.offline.OfflineCircuit;
import fptd.offline.OfflineGate;
import fptd.offline.FakeParty;

import java.math.BigInteger;

import static fptd.Params.N;

public class TestElemWiseMultiplyOffline {

    @Test
    public void testFakeCircuit(){
        FakeParty fakeParty = new FakeParty("test", N);
        OfflineCircuit fakeCircuit = new OfflineCircuit(fakeParty);

        OfflineGate a = fakeCircuit.input(0, 2);
        OfflineGate b = fakeCircuit.input(0, 2);
        OfflineGate c = fakeCircuit.input(0, 2);
        OfflineGate d = fakeCircuit.elemWiseMult(a, b);
        OfflineGate e = fakeCircuit.elemWiseMult(d, c);
        OfflineGate f = fakeCircuit.output(e);
        fakeCircuit.addEndpoint(f);
        fakeCircuit.runOffline();
    }

    @Test
    public void test(){
//        80480655665923849860480397293826320603251025135410149452633560051767096427797
//        45131792469121263756595834311380692157710406576632139135665297137958532589737
//        9820358897728918193505246596519104908123867432967384205693694048207467523197

        BigInteger a = new BigInteger("80480655665923849860480397293826320603251025135410149452633560051767096427797");
        BigInteger b = new BigInteger("45131792469121263756595834311380692157710406576632139135665297137958532589737");
        System.out.println(a.add(b).mod(Params.P));
    }
}
