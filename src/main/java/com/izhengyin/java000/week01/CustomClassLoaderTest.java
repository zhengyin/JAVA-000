package com.izhengyin.java000.week01;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Create on 2020/10/21 9:05 下午
 */
public class CustomClassLoaderTest{
    public static void main(String[] args){
        MyClassLoader classLoader = new MyClassLoader("/Users/zhengyin/project/my/JAVA-000/Week_01/Hello.class");
        try {
            classLoader.findClass("Hello").newInstance();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }catch (InstantiationException e){
            e.printStackTrace();
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }
    }

    private static class MyClassLoader extends ClassLoader {
        private final String fileName;
        public MyClassLoader(String fileName){
            this.fileName = fileName;
        }
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                byte[] bytes = readFile(this.fileName);
                return defineClass(name,bytes,0,bytes.length);
            }catch (IOException e){
                throw new ClassNotFoundException(e.getMessage(),e);
            }
        }

        private byte[] readFile(String fileName) throws IOException{
            File file = new File(fileName);
            byte[] bytes = new byte[(int)file.length()];
            FileInputStream in = null;
            try {
                in = new FileInputStream(file);
                in.read(bytes);
            } finally {
                if(in != null){
                    in.close();
                }
            }
            return bytes;
        }
    }
}
