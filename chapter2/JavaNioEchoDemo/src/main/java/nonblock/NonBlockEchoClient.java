package nonblock;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NonBlockEchoClient {
    private final String hostname;
    private final int port;

    public NonBlockEchoClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        String hostname = "localhost";
        int port = 8888;
        new NonBlockEchoClient(hostname, port).start();
    }

    public void start() throws Exception {
        // 创建channel & buffer缓冲区
        SocketChannel sc = SocketChannel.open(new InetSocketAddress(hostname, port));
        sc.configureBlocking(false);
        ByteBuffer buf = ByteBuffer.allocate(1024);

        try {
            // 向server发送小道消息
            buf.put("Secret message".getBytes(StandardCharsets.UTF_8));
            buf.flip();
            sc.write(buf);
            buf.clear();
            sc.shutdownOutput();
            System.out.println("Client has sent messages");

            // 接受服务器的响应消息
            StringBuilder respMsg = new StringBuilder();
            while (sc.read(buf) != -1) {
                buf.flip();
                respMsg.append(new String(buf.array(), 0, buf.limit()));
                buf.clear();
            }
            System.out.println("Client receive: " + respMsg.toString());
        } finally {
            sc.close();
        }
    }
}
