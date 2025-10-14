package fptd.offline;

import fptd.Params;
import fptd.Share;
import fptd.sharing.ShamirSharing;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


public class FakeParty {
    private int partyNum;
    private List<BufferedWriter> writers = new ArrayList<>();

    public FakeParty(String jobName, int partyNum) {
        this.partyNum = partyNum;
        File folder = new File(Params.FAKE_OFFLINE_DIR);
        if(!folder.exists()) {
            folder.mkdir();
        }
        final String fileNameSuffix = jobName + (jobName.isEmpty() ? "party-": "-party-");
        try {
            for(int i = 0; i < partyNum; i++){
                String currentFileName = fileNameSuffix + String.valueOf(i) + ".txt";
                BufferedWriter writer = new BufferedWriter(new FileWriter(Params.FAKE_OFFLINE_DIR + "/" + currentFileName));
                writers.add(writer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final List<List<Share>> generateAllPartiesShares(List<BigInteger> values){
        List<List<Share>> result = new ArrayList<>();//one row for an edge server
        ShamirSharing sharing = new ShamirSharing();
        for(BigInteger value : values){//generate shares for value
            List<Share> shares = sharing.getShares(value);
            if(result.isEmpty()){
                for(int i = 0; i < shares.size(); i++){
                    result.add(new ArrayList<>());
                }
            }
            for(int i = 0; i < result.size(); i++){
                List<Share> sharesIthParty = result.get(i);
                sharesIthParty.add(shares.get(i));//put the shares to various rows
            }
        }
        return result;
    }

    public void writeSharesToAllParties(List<List<Share>> shares){
        try {
            for (int party_idx = 0; party_idx < partyNum; party_idx++) {
                BufferedWriter writer = writers.get(party_idx);
                for (Share share : shares.get(party_idx)) {
                    writer.write(share.getShr().toString());
                    writer.newLine();
                }
                writer.flush();
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void writeClearToIthParty(List<BigInteger> values, int party_id){
        BufferedWriter writer = writers.get(party_id);
        try {
            for(BigInteger value : values) {
                writer.write(value.toString());
                writer.newLine();
            }
            writer.flush();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeClearToAllParties(List<BigInteger> values){
        for(int party_idx = 0; party_idx < partyNum; party_idx++){
            writeClearToIthParty(values, party_idx);
        }
    }
}
