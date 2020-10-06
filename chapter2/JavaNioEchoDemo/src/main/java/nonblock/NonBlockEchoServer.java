package nonblock;

import block.BlockEchoServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class NonBlockEchoServer {
    private final String hostname;
    private final int port;

    public NonBlockEchoServer(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        String hostname = "localhost";
        int port = 8888;
        new NonBlockEchoServer(hostname, port).start();
    }

    public void start() throws Exception {
        System.out.println(String.format("Server start on %s:%d", hostname, port));

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(hostname, port));

        // 区别于block的写法
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        try {
            while (selector.select() > 0) {
                Iterator<SelectionKey> skIterable = selector.selectedKeys().iterator();
                while (skIterable.hasNext()) {
                    SelectionKey sk = skIterable.next();

                    if (sk.isAcceptable()) {        // 条件是有请求
                        SocketChannel sc = ssc.accept();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ);
                    } else if (sk.isReadable()) {   // 条件是请求数据准备好了
                        SocketChannel sc = (SocketChannel) sk.channel();
                        StringBuilder reqMsg = new StringBuilder();
                        ByteBuffer buf = ByteBuffer.allocate(1024);
                        while (sc.read(buf) != -1) {
                            buf.flip();
                            reqMsg.append(new String(buf.array(), 0, buf.limit()));
                            buf.clear();
                        }
                        System.out.println("Server receive: " + reqMsg.toString());

                        buf.put(reqMsg.toString().getBytes(StandardCharsets.UTF_8));
                        buf.flip();
                        sc.write(buf);
                        sc.close();
                    }
                    skIterable.remove();
                }
            }
        } finally {
            ssc.close();
        }
    }
}
