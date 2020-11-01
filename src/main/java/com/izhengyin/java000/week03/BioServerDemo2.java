package com.izhengyin.java000.week03;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * nio库的，同步阻塞的IO示例
 * @author zhengyin zhengyinit@outlook.com
 * Create on 2020/11/1 4:56 下午
 */
public class BioServerDemo2 {
    private final static ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 启动以后运行， telnet 127.0.0.1 8100 测试
     * @param args
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        try{
            serverSocket.socket().bind(new InetSocketAddress(8100));
            while(true){
                //阻塞到有结果返回
                SocketChannel socketChannel =  serverSocket.accept();
                EXECUTOR.execute(() -> {
                    try {
                        byte[] response = "Hello Client\n".getBytes();
                        ByteBuffer buffer = ByteBuffer.allocate(response.length);
                        //响应客户端消息
                        buffer.put(response);
                        buffer.flip();
                        if(buffer.hasRemaining()){
                            try {
                                socketChannel.write(buffer);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    }finally {
                        try {
                            socketChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }finally{
            serverSocket.close();
        }
    }
}
