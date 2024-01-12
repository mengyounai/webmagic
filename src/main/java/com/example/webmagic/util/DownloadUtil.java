package com.example.webmagic.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadUtil {

    public static String videoDownLoad(String sourceUrl, String fileName) {
        HttpURLConnection urlConnection = null;
        InputStream is = null;
        FileOutputStream fos = null;
        String outPutPath = "E:\\VideoTest\\" + fileName + ".mp4";
        try {
            URL url = new URL(sourceUrl);
            //连接
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("referer", "https://www.bilibili.com");
            urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36");
            urlConnection.connect();
            //获取流数据
            is = urlConnection.getInputStream();

            fos = new FileOutputStream(outPutPath);
            byte[] chars = new byte[1024];
            int len;
            while ((len = is.read(chars)) != -1) {
                fos.write(chars, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        urlConnection.disconnect();
        System.out.println("视频下载完成");
        return outPutPath;
    }

    public static String audioDownLoad(String sourceUrl, String fileName) {
        HttpURLConnection urlConnection = null;
        InputStream is = null;
        FileOutputStream fos = null;
        String outPutPath = "E:\\VideoTest\\" + fileName + ".flv";
//        String outPutPath = "E:\\VideoTest\\" + "a" + ".flv";
        try {
            URL url = new URL(sourceUrl);
            //连接
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("referer", "https://www.bilibili.com");
            urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36");
            urlConnection.connect();
            //获取流数据
            is = urlConnection.getInputStream();
            fos = new FileOutputStream(outPutPath);
            byte[] chars = new byte[1024];
            int len;
            while ((len = is.read(chars)) != -1) {
                fos.write(chars, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        urlConnection.disconnect();
        System.out.println("音频下载完成");
        return outPutPath;
    }

    /**
     * 合并音视频
     */
    public static void ffmpegMerge(String path1, String path2) {
        System.out.println("开始合并...");
        File file = new File(path1);
        File parentFile = file.getParentFile();
        //拼接ffmpeg命令
        String command = "E:\\软件合集\\ffmpeg\\bin\\ffmpeg.exe" + " -i "  + path1 +  " -i " + path2 + " -codec copy " +
                parentFile + "\\合成\\" + file.getName();
        Process process = null;
        try {
            //执行本地命令
            process = Runtime.getRuntime().exec(command);
//            //因为process的输出流缓冲区很小，会导致程序阻塞，因此自己写个工具类对进程的输出流进行处理
//            execStream stream = new execStream(process.getErrorStream(), "ERROR");
//            stream.start();
//            execStream stream1 = new execStream(process.getInputStream(), "STDOUT");
//            stream1.start();
//            //得到进程运行结束后的返回状态，如果进程未运行完毕则等待知道执行完毕，正确结束返回int型的0
//            int i = process.waitFor();
//            System.out.println(i);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("合并完成");
    }

    public static void mergeList(){
//        String path = "E:\\VideoTest\\待合成\\";
        String path = "E:\\VideoTest\\";
        File file = new File(path);
        File[] files = file.listFiles();
        String path1 = "";
        String path2 = "";
        File file1 = null;
        File file2 = null;
        int i = 0;
        for (File file3 : files) {
//            System.out.println(file3.getName());
            if (file3.isDirectory()){
                continue;
            }
            i++;
            if (i == 1){
                path1 = "\"" + path + file3.getName() + "\"";
                file1 = new File(path1);
            }
            if (i == 2){
                i = 0;
                path2 = "\"" + path + file3.getName() + "\"";
                file2 = new File(path2);
                ffmpegMerge(file2.getPath(),file1.getPath());
            }
        }
    }


    public static void main(String[] args) throws Exception{

        int i = 1;
        while (i<9){
            System.out.println(i%3);
            i++;
        }
//         mergeList();

//        File file = new File("\"E:\\VideoTest\\audio.m4s\"");
//        File file1 = new File("\"E:\\VideoTest\\video.m4s\"");
//        ffmpegMerge(file1.getPath(),file.getPath());
    }
}
