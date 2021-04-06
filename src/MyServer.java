import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class MyServer extends Thread {
    private static int t0 = 0;
    private static int R = 100;
    private static int S = 1;
    private static InetAddress inetAddr;

    static {
        try {
            // 在Java UDP中单播与广播的代码是相同的,要实现具有广播功能的程序只需要使用广播地址即可, 例如：这里使用了本地的广播地址
            inetAddr = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static int serverNums = 0;
    private Double current;
    private int serverId;
    private int serverPort;
    private Double step;

    private DatagramPacket receive;
    private DatagramSocket server;
    private DatagramSocket client;

    public MyServer(int serverId, int serverPort, Double step) throws SocketException {
        this.serverId = serverId;
        this.serverPort = serverPort;
        this.current = .0 + serverId;
        this.step = step;
        serverNums++;
        receive = new DatagramPacket(new byte[1024], 1024);
        server = new DatagramSocket(serverPort);
        client = new DatagramSocket();
    }

    @Override
    public void run() {
        while (true) {
            current += step;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if ((Math.ceil(current) - t0) % R == 0) {
                byte[] msg = current.toString().getBytes();
                System.out.println(serverId + "th Server before synchronize time:"+current);
                try {
                    client = new DatagramSocket();
                } catch (SocketException e) {
                    e.printStackTrace();
                }

                DatagramPacket sendPack;
                for (int i = 0; i < serverNums; i++) {
                    sendPack = new DatagramPacket(msg, msg.length, inetAddr, 8000 + i);
                     try {
                        client.send(sendPack);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                client.close();
                int k = 0;
                Double sum = .0;
                while (true) {
                    try {
                        server.receive(receive);
                        byte[] recvByte = Arrays.copyOfRange(receive.getData(), 0,
                                receive.getLength());
//                        System.out.println(serverId + "th Server receive msg:" + Double.valueOf(new String(recvByte)));
                        sum += Double.valueOf(new String(recvByte));
//                        System.out.println("sum:"+sum);
                        k++;
                        if (k == serverNums) {
                            current = sum / serverNums;
                            System.out.println(serverId + "th Server current time:"+current);
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
//                break;
            }

        }
    }

    public static void main(String[] args) throws SocketException {
        int n=5;
        MyServer[] servers = new MyServer[n];
        for (int i = 0; i <n; i++) {
            servers[i] = new MyServer(i, 8000 + i, 1 + (double) i / 100);
        }

        for (int i = 0; i < n; i++) {
            servers[i].start();
        }
    }
}
