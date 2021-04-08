import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class MyServer extends Thread {
//所有机器都认定的时间t0
    private static int t0 = 0;
//再同步间隔参数R
    private static int R = 100;
//S?
    private static int S = 1;
//初始化本地广播地址 255.255.255.255
    private static InetAddress inetAddr;
    static {
        try {
            inetAddr = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
//总的机器数量
    private static int serverNums = 0;

//用一个浮点数表示机器当前时间
    private Double current;
//机器id号
    private final int serverId;
//时间增加的步长
    private final Double step;

    private final DatagramPacket receive;
    private final DatagramSocket server;
    private DatagramSocket client;

    public MyServer(int serverId, int serverPort, Double step) throws SocketException {
        this.serverId = serverId;
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
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            广播当前时间
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
//               监听其他机器广播的时间
                int k = 0;
                double sum = .0;
                while (true) {
                    try {
                        server.receive(receive);
                        byte[] recvByte = Arrays.copyOfRange(receive.getData(), 0,
                                receive.getLength());
                        sum += Double.parseDouble(new String(recvByte));
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
