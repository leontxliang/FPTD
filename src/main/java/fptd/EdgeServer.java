package fptd;

import static fptd.Params.N;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class EdgeServer {

    private boolean isKing = false;
    private int idx; // start from 0
    private ArrayList<ObjectInputStream> networkReaders = null;
    private ArrayList<ObjectOutputStream> networkWriters = null;

    private ObjectInputStream readerFromKing = null;
    private ObjectOutputStream writerToKing = null;

    private BufferedReader fileReader;

    public EdgeServer(boolean isKing, int idx, String jobName) {
        this.isKing = isKing;
        this.idx = idx;

        final String fileNameSuffix = jobName + (jobName.isEmpty() ? "party-" : "-party-");
        String currentFileName = fileNameSuffix + idx + ".txt";
        try {
            fileReader = new BufferedReader(new FileReader(Params.FAKE_OFFLINE_DIR + currentFileName));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Share> readRandShares(int size) {
        List<Share> shares = new ArrayList<>();
        try {
            for (int i = 0; i < size; i++) {
                shares.add(new Share(this.idx, new BigInteger(this.fileReader.readLine())));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return shares;
    }

    public List<BigInteger> readClear(int size) {
        List<BigInteger> values = new ArrayList<>();
        try {
            for (int i = 0; i < size; i++) {
                values.add(new BigInteger(this.fileReader.readLine()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return values;
    }

    public void connectOtherServers() throws IOException {
        if (this.isKing) {
            this.networkReaders = new ArrayList<>();
            this.networkWriters = new ArrayList<>();
            networkReaders.add(null);// 留着第一个位置
            networkWriters.add(null);
            ServerSocket serverSocket = new ServerSocket(Params.Port_King);
            for (int i = 0; i < Params.NUM_SERVER - 1; i++) { // except the king server
                Socket socket = serverSocket.accept(); // wait for other servers' connection
                networkReaders.add(new ObjectInputStream(socket.getInputStream()));
                networkWriters.add(new ObjectOutputStream(socket.getOutputStream()));
                if (Params.IS_PRINT_EXE_INFO) {
                    System.out.println("King get I/O connection from " + socket.getRemoteSocketAddress());
                }
            }
            new Thread(() -> {// 让king自我连接
                try {
                    Socket socket = serverSocket.accept();
                    networkReaders.set(0, new ObjectInputStream(socket.getInputStream()));
                    networkWriters.set(0, new ObjectOutputStream(socket.getOutputStream()));
                    if (Params.IS_PRINT_EXE_INFO) {
                        System.out.println("King get I/O connection from itself " + socket.getRemoteSocketAddress());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
        Socket socket = new Socket(Params.IP_King, Params.Port_King); // connect to the king as a server
        this.writerToKing = new ObjectOutputStream(socket.getOutputStream());
        this.readerFromKing = new ObjectInputStream(socket.getInputStream());
    }

    public void sendToKing(Object message) {
        try {
            writerToKing.writeObject(message);
            writerToKing.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object readFromKing() {
        try {
            return readerFromKing.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Object> kingReadFromAll() {
        if (!this.isKing) {
            throw new RuntimeException("Only the king calls this function");
        }

        List<Object> result = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                ObjectInputStream reader = this.networkReaders.get(finalI);
                try {
                    Object obj = reader.readObject();
                    synchronized (result) {
                        result.add(obj);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads) {
            try {
                thread.join(); // 让main thread等待threads完成
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public void kingSendToAll(Object message) {
        if (!this.isKing) {
            throw new RuntimeException("Only the king calls this function");
        }
        try {
            for (ObjectOutputStream writer : this.networkWriters) {
                writer.writeObject(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            if (this.isKing) {
                for (ObjectInputStream reader : networkReaders) {
                    reader.close();
                }
                for (ObjectOutputStream writer : networkWriters) {
                    writer.close();
                }
            }
            this.readerFromKing.close();
            this.writerToKing.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isKing() {
        return this.isKing;
    }

    public int getIdx() {
        return this.idx;
    }
}