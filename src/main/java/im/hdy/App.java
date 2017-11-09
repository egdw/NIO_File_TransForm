package im.hdy;

import javax.sound.midi.Soundbank;
import javax.sound.sampled.Port;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Hello world!
 */
public class App {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("欢迎使用文件传输.本程序支持局域网内文件和文件夹的传输!");
        init();
    }

    public static void init() {
        boolean flag = true;
        while (flag) {
            try {
                System.out.println("本机的IP = (" + InetAddress.getLocalHost().getHostName() + ") " + InetAddress.getLocalHost().getHostAddress());
                System.out.println("接收文件方请输入0");
                System.out.println("发送文件方请输入1");
                int first = Integer.valueOf(scanner.nextLine());
                List<String> iPs = getIPs();
                System.out.println(iPs);
                if (first != 0 && first != 1) {
                    //说明输入有问题
                    System.out.println("输入有误请重新输入!");
                    continue;
                }
                switch (first) {
                    case 0:
                        //接收文件
                        break;
                    case 1:
                        //发送文件
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 开启文件接收服务
     */
    public static void openServer() {
        System.out.println("开启文件服务中...");
        try {
            ServerSocketChannel channel = ServerSocketChannel.open();
            Selector selector = Selector.open();
            channel.configureBlocking(false);

            //设置服务器的端口
            channel.socket().setReuseAddress(true);
            channel.socket().bind(new InetSocketAddress(10000));

            //channel注册
            channel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 开启发送文件服务
     *
     * @param ip   远程服务器id
     * @param port 端口号
     */
    public static void sendServer(String ip, int port) {
        try {
            InetSocketAddress address = new InetSocketAddress(ip, port);
            SocketChannel open = SocketChannel.open(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<String> getIPs() {
        List<String> list = new ArrayList<String>();
        boolean flag = false;
        int count = 0;
        Runtime r = Runtime.getRuntime();
        Process p;
        try {
            p = r.exec("arp -a");
            BufferedReader br = new BufferedReader(new InputStreamReader(p
                    .getInputStream()));
            String inline;
            while ((inline = br.readLine()) != null) {
                if (inline.indexOf("接口") > -1) {
                    flag = !flag;
                    if (!flag) {
                        //碰到下一个"接口"退出循环
                        break;
                    }
                }
                if (flag) {
                    count++;
                    if (count > 2) {
                        //有效IP
                        String[] str = inline.split(" {4}");
                        list.add(str[0]);
                    }
                }
                System.out.println(inline);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(list);
        return list;
    }
}
