package block;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class BlockEchoServer {
    private final String hostname;
    private final int port;

    public BlockEchoServer(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        String hostname = "localhost";
        int port = 8888;
        new BlockEchoServer(hostname, port).start();
    }

    public void start() throws Exception {
        System.out.println(String.format("Server start on %s:%d", hostname, port));

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(hostname, port));

        try {
            for (; ; ) {
                SocketChannel sc = ssc.accept();
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
        } finally {
            ssc.close();
        }
    }
}
