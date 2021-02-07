package src.main.java.com.izhengyin.java000.week03;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Create on 2020/11/1 12:47 下午
 */
public class NonBlocking {
    public static void main(String[] args) {
        NonBlock nonBlock = new NonBlock();
        //调用发起后主线程不会被挂起 , 可以另起线程去得到结果
        nonBlock.call();
        new Thread(() -> {
            while (true){
                if(nonBlock.getState() == 1){
                    System.out.println(nonBlock.getRes());
                    break;
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
