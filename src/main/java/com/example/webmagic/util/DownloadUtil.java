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
        String name = file.getName();
        File parentFile = file.getParentFile();

        //拼接ffmpeg命令
        String command = "E:\\软件合集\\ffmpeg\\bin\\ffmpeg.exe" + " -i " + path1 + " -i " + path2 + " -codec copy " +
                parentFile + "\\合成\\" + file.getName();
        Process process = null;
        try {
            //执行本地命令
            process = Runtime.getRuntime().exec(command);
            //因为process的输出流缓冲区很小，会导致程序阻塞，因此自己写个工具类对进程的输出流进行处理
            execStream stream = new execStream(process.getErrorStream(), "ERROR");
            stream.start();
            execStream stream1 = new execStream(process.getInputStream(), "STDOUT");
            stream1.start();
            //得到进程运行结束后的返回状态，如果进程未运行完毕则等待知道执行完毕，正确结束返回int型的0
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("合并完成");
    }

    public static void main(String[] args) throws Exception{

//        File file = new File("\"E:\\VideoTest\\32.AST 第一个babel例子.flv\"");
//        File file1 = new File("\"E:\\VideoTest\\32.AST 第一个babel例子.mp4\"");
//        ffmpegMerge(file1.getPath(),file.getPath());
    }
}
