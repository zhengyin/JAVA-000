# Java 同步异步、阻塞非阻塞、BIO,NIO,AIO,IO多路复用、reactor线程模型、Netty、reactor-netty、WebFlux

> 几年以前从PHP转Java，刚把Java基础语法学完，就从一本《Netty权威指南》开始了我的Java第一个线上应用，一个基于Netty的消息订阅服务，现在这个服务已经被Spring Boot WebFlux 重构了，现在回看当初写的代码，会心一笑，真是初出茅庐不怕虎。
> 这次借着课程从学Netty的机会，将之前学的知识重新整理一遍，理清思路，加深印象。

> 注：以下代码示例，只为说明不同概念对应的简单编码示例，不纠结于是否严禁。

## 同步异步、阻塞非阻塞

* 同步和异步关注的是消息通信机制 (synchronous communication/ asynchronous communication)
    * 所谓同步，就是在发出一个调用时，在没有得到结果之前，该调用就不返回。
    ``` 
        public class Synchronous {
            public static void main(String[] args) {
                //主线程等待返回结果
                String res = call();
                System.out.println(res);
            }
            public static String call(){
                try {
                    TimeUnit.SECONDS.sleep(1);
                }catch (InterruptedException e){
        
                }
                return "ready";
            }
        }
    ```
    * 异步则是相反，调用在发出之后，这个调用就直接返回了，所以没有返回结果。
    ``` 
    public class Asynchronous {
        private static ExecutorService executors = Executors.newFixedThreadPool(1);
        public static void main(String[] args) {
            //主线程不等待返回结果
            asyncCall( res -> {
                System.out.println(res);
            });
        }
        public static void asyncCall(Consumer<String> consumer){
            executors.execute(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                }catch (InterruptedException e){
    
                }
                consumer.accept("ready");
            });
        }
    }
    ``` 
    
* 阻塞和非阻塞关注的是程序在等待调用结果（消息，返回值）时的状态.
    * 阻塞调用是指调用结果返回之前，当前线程会被挂起，调用线程只有在得到结果之后才会返回。
    ``` 
    public class Blocking {
        public static void main(String[] args) {
            //调用发起后主线程将被挂起,直到结果返回
            String res = blockCall();
            System.out.println(res);
        }
        public static String blockCall(){
            try {
                TimeUnit.SECONDS.sleep(1);
            }catch (InterruptedException e){
    
            }
            return "ready";
        }
    }
    ```
    * 非阻塞调用指在不能立刻得到结果之前，该调用不会阻塞当前线程。
    ``` 
    public class NonBlocking {
        public static void main(String[] args) {
            NonBlock nonBlock = new NonBlock();
            //调用发起后主线程不会被挂起 , 可以另起线程去得到结果
            int state = nonBlock.call();
            new Thread(() -> {
                while (true){
                    if(state == 1){
                        System.out.println(nonBlock.getRes());
                    }
                }
            }).start();
            System.out.println("todo other things ...");
        }
        public static class NonBlock{
            private volatile int state = 0;
            private static ExecutorService executors = Executors.newFixedThreadPool(1);
            public int call(){
                executors.execute(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        state = 1;
                    }catch (InterruptedException e){
    
                    }
                });
                return state;
            }
            private String getRes(){
                state = 0;
                return "ready";
            }
            public int getState(){
                return state;
            }
        }
    } 
    ```
  
## BIO 同步阻塞IO
    > BIO 关注的是是否阻塞，无论是java.net包下提供的Socket编程接口还是java.nio包下提供的Socket接口，都有BIO的实现，下面两段代码演示了这样的情况，也就是说不是java.nio包下的socket编程就一定是NIO。
    ``` 
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
    ```
    
    ``` 
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
    ```
## NIO 同步非阻塞IO / 多路复用IO（IO multiplexing） 
    > 传统NIO模型和阻塞IO类比，内核会立即返回，返回后获得足够的CPU时间继续做其它的事情，但是用户进程需要不断的去询问内核数据是否就绪，这一阶段是阻塞的。
    > 多路复用IO是为了弥补传统NIO模型的不足，当用户进程调用了select，那么整个进程会被block，而同时，kernel会“监视”所有select负责的socket，当任何一个socket中的数据准备好了，select就会返回。
    > 同时多路复用IO将一个Socket分为了几个阶段，可以针对不同阶段进行相应的处理
    
    ``` 
        SelectionKey.OP_CONNECT		连接就绪
        SelectionKey.OP_ACCEPT		接收就绪
        SelectionKey.OP_READ		读就绪
        SelectionKey.OP_WRITE		写就绪
    ```
    
    > Nio多路复用的示例
    ```
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
    
    ```

## AIO 异步IO

    > Linux下的asynchronous IO其实用得不多，从内核2.6版本才开始引入。用户进程发起read操作之后，立刻就可以开始去做其它的事。而另一方面，从kernel的角度，当它受到一个asynchronous read之后，首先它会立刻返回，所以不会对用户进程产生任何block。
    
    ``` 
    
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

    ```
    
