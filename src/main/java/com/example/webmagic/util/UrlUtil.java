package com.example.webmagic.util;

public class UrlUtil {

    public static String getImgUrl(String s){
        String url = s.substring(s.indexOf("h"), s.indexOf("alt") - 2);
        return url;

    }

    public static void main(String[] args) {
        String imgUrl = getImgUrl("<img src=\"http://image-jishanle2.test.upcdn.net//blog/acg.gy_29Wud4.jpg\" alt=\"\" class=\"ui fluid rounded image\">");
        System.out.println(imgUrl);
    }
}
