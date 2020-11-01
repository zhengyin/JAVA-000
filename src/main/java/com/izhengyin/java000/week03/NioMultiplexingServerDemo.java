package com.izhengyin.java000.week03;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 多路复用的Nio示例
 * @author zhengyin zhengyinit@outlook.com
 * Create on 2020/11/1 4:56 下午
 */
public class NioMultiplexingServerDemo {
    private final static ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);
    /**
     * 启动以后运行， telnet 127.0.0.1 8100 测试
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(8100));

        //不阻塞
        serverSocketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true){
            //阻塞，直到有就绪的Key
            selector.select();
            Iterator keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey selectionKey = (SelectionKey)keyIterator.next();
                try {
                    if(selectionKey.isValid()){

                        if(selectionKey.isAcceptable()){
                            try {
                                ServerSocketChannel ssc = (ServerSocketChannel)selectionKey.channel();
                                //产生一个新的 socketChannel，处理客户端的数据
                                SocketChannel socketChannel = ssc.accept();
                                socketChannel.configureBlocking(false);
                                //注册一个已读的事件监听
                                socketChannel.register(selector, SelectionKey.OP_READ);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                        //读就绪
                        if(selectionKey.isReadable()){
                            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
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
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (selectionKey != null) {
                        selectionKey.cancel();
                        selectionKey.channel().close();
                    }
                }
                keyIterator.remove();
            }
        }
    }


}
