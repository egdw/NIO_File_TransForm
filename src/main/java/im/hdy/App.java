package im.hdy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

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
                System.out.println("接收文件方请输入0");
                System.out.println("发送文件方请输入1");
                int first = Integer.valueOf(scanner.nextLine());
                if (first != 0 && first != 1) {
                    //说明输入有问题
                    System.out.println("输入有误请重新输入!");
                    continue;
                }
                switch (first) {
                    case 0:
                        //接收文件
                        System.out.println("本机的IP = (" + InetAddress.getLocalHost().getHostName() + ") " + InetAddress.getLocalHost().getHostAddress());
                        System.out.println("请输入你要绑定的端口号.如果不输入则为默认的12345");
                        String port = scanner.nextLine();
                        //判断port是否输入
                        if (port.equals("")) {
                            port = "12345";
                        }
                        openServer(port);
                        break;
                    case 1:
                        //发送文件
                        System.out.println("1.搜索获取当前局域网ip");
                        System.out.println("2.手动输入ip");
                        String ip = null;
                        while (true) {
                            String s = scanner.nextLine();
                            if (s.equals("1")) {
                                List<String> iPs = getIPs();
                                for (int i = 0; i < iPs.size(); i++) {
                                    System.out.println((i + 1) + "." + iPs.get(i));
                                }
                                System.out.println("请输入你要连接的ip地址");
                                ip = scanner.nextLine();
                                if (ip.equals("")) {
                                    ip = "localhost";
                                }
                                break;
                            } else {
                                System.out.println("请输入ip地址:");
                                //获取到用户输入的ip地址
                                ip = scanner.nextLine();
                                if (ip.equals("")) {
                                    ip = "localhost";
                                }
                                break;
                            }
                        }
                        System.out.println("请输入端口(默认12345):");
                        //获取到用户输入的端口号
                        String port2 = scanner.nextLine();
                        if (port2.equals("")) {
                            port2 = "12345";
                        }
                        System.out.println("请输入要发送的文件夹或文件地址:");
                        //获取用户输入的文件地址
                        String sendFile = scanner.nextLine();
                        if (sendFile.equals("")) {
                            sendFile = "/Users/hdy/Desktop/数字杭科-d2j.jar";
                        }
                        //发送文件传输请求
                        sendServer(ip, Integer.valueOf(port2), sendFile);
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
    public static void openServer(String port) {
        System.out.println("开启文件服务中...");
        System.out.println("请输入你要保存到的文件夹地址:");
        //服务端保存到的地址
        String saveFile = scanner.nextLine();
        if (saveFile.equals("")) {
            saveFile = "/Users/hdy/Desktop/test";
        }
        try {
            ServerSocketChannel channel = ServerSocketChannel.open();
            Selector selector = Selector.open();
            //开启非阻塞模式
            channel.configureBlocking(false);

            //设置服务器的端口
            channel.socket().setReuseAddress(true);
            channel.socket().bind(new InetSocketAddress(Integer.valueOf(port)));
            RandomAccessFile file = new RandomAccessFile(saveFile + "/1.jar", "rw");
            FileChannel fileChannel = file.getChannel();

            channel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        //判断当前是否为可读状态
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel =
                                channel.accept();
                        socketChannel.configureBlocking(false);
                        //channel注册
                        socketChannel.register(selector, SelectionKey.OP_READ);
//                        if (socketChannel != null) {
//                            System.out.println("接受到请求");
//                            int read = socketChannel.read(buffer);
//                            buffer.flip();
//                            System.out.println("写入数据");
//                            fileChannel.write(buffer);
//                            socketChannel.close();
//                        }
                    } else if (key.isReadable()) {
                        //当前是读的状态
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        //创建缓冲区
                        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                        socketChannel.read(readBuffer);
                        byte[] bytes = readBuffer.array();
                        String msg = new String(bytes).trim();
                        System.out.println("-----接收到----客户端----发送过来的信息------:\t" + msg);
                        readBuffer.clear();
                    }
                    iterator.remove();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 开启发送文件服务
     *
     * @param ip   远程服务器id
     * @param port 端口号
     * @param path 文件路径
     */
    public static void sendServer(String ip, int port, String path) {
        try {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.connect(new InetSocketAddress(ip, port));
            channel.register(selector, SelectionKey.OP_CONNECT);
            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey next = iterator.next();
                    if (next.isConnectable()) {
                        //判断当前是否可以连接
                        SocketChannel socketChannel = (SocketChannel) next.channel();
                        if (socketChannel.isConnectionPending()) {
                            socketChannel.finishConnect();
                        }
                        socketChannel.configureBlocking(false);
//                        RandomAccessFile accessFile = new RandomAccessFile(path, "rw");
                        ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
                        byteBuffer.flip();
                        socketChannel.write(byteBuffer);
                        socketChannel.close();
                        break;
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            InetSocketAddress address = new InetSocketAddress(ip, port);
//            SocketChannel open = SocketChannel.open(address);
//            RandomAccessFile accessFile = new RandomAccessFile(path, "rw");
//            FileChannel fileChannel = accessFile.getChannel();
//            ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
//            fileChannel.read(byteBuffer);
//            byteBuffer.flip();
//            while (byteBuffer.hasRemaining()) {
//                open.write(byteBuffer);
//            }
//            byteBuffer.clear();
//            fileChannel.close();
//            open.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    /**
     * 获取当前局域网内的ip地址
     *
     * @return 返回列表
     */
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
                String[] str = inline.split(" {4}");
                list.add(str[0]);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
