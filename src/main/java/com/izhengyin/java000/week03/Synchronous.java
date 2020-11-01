package src.main.java.com.izhengyin.java000.week03;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Create on 2020/11/1 12:47 下午
 */
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
