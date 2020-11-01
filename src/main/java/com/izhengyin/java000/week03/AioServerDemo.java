package com.izhengyin.java000.week03;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Aio Server 示例
 * @author zhengyin zhengyinit@outlook.com
 * Create on 2020/11/1 6:50 下午
 */
public class AioServerDemo {

    public static void main(String[] args) throws IOException,InterruptedException {
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withCachedThreadPool(Executors.newFixedThreadPool(10), 3);
        AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open(group);
        serverChannel.bind(new InetSocketAddress(8100));
        ListenerHandler listenerHandler = new ListenerHandler(serverChannel);
        listenerHandler.doAccept(new AcceptCompileHandler());

        while (true){
            TimeUnit.SECONDS.sleep(1);
        }
    }


    public static class ListenerHandler extends Thread{
        private AsynchronousServerSocketChannel serverChannel;
        public ListenerHandler(AsynchronousServerSocketChannel serverChannel) {
            this.serverChannel = serverChannel;
        }
        public void doAccept(AcceptCompileHandler acceptCompileHandler){
            this.serverChannel.accept(this,acceptCompileHandler);
        }
        public void doAccept(ListenerHandler listenerHandler,AcceptCompileHandler acceptCompileHandler){
            this.serverChannel.accept(listenerHandler, acceptCompileHandler);
        }
    }

    private static class AcceptCompileHandler implements CompletionHandler<AsynchronousSocketChannel, ListenerHandler> {
        @Override
        public void completed(AsynchronousSocketChannel socketChannel, ListenerHandler listenerHandler) {
            //继续监听客户端连接
            listenerHandler.doAccept(listenerHandler, this);
            //读数据
            ByteBuffer buffer = ByteBuffer.allocate(128);
            socketChannel.read(buffer, buffer, new ReadCompileHandler(socketChannel));
        }
        @Override
        public void failed(Throwable exc, ListenerHandler listenerHandler) {
            exc.printStackTrace();
        }
    }

    private static class  ReadCompileHandler implements CompletionHandler<Integer, ByteBuffer> {
        private AsynchronousSocketChannel socketChannel;
        public ReadCompileHandler(AsynchronousSocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        @Override
        public void completed(Integer readSize, ByteBuffer buffer) {
            try {
                write("Hello Client\n");
            }finally {
                try {
                    socketChannel.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            exc.printStackTrace();
        }

        private void write(String msg){
            byte[] bytes = msg.getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            socketChannel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer buffer) {
                    //没有发送完，接着发
                    if(buffer.hasRemaining()){
                        socketChannel.write(buffer, buffer, this);
                    }
                }
                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                }
            });
        }
    }
}
