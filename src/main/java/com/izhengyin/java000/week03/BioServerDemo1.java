package src.main.java.com.izhengyin.java000.week03;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 同步阻塞IO示例
 * @author zhengyin zhengyinit@outlook.com
 * Create on 2020/11/1 4:45 下午
 */
public class BioServerDemo1 {
    private final static ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * telnet 127.0.0.1 8100 测试
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8100);
        while (true){
            //阻塞到有结果返回
            Socket socket = serverSocket.accept();
            EXECUTOR.execute(() -> {
                try {
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
                    printWriter.write("Hello Client\n");
                    printWriter.close();
                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            });
        }
    }

}
