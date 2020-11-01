package src.main.java.com.izhengyin.java000.week03;

import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Create on 2020/11/1 12:47 下午
 */
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
