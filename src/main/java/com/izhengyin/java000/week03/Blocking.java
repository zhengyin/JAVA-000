package src.main.java.com.izhengyin.java000.week03;

import java.util.concurrent.TimeUnit;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Create on 2020/11/1 12:47 下午
 */
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
