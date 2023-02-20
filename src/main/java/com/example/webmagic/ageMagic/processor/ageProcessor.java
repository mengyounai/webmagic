package com.example.webmagic.ageMagic.processor;

import com.google.common.collect.Maps;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.example.webmagic.bilibiliMagic.processor.BilibiliProcessor;
import com.example.webmagic.blogMagic.pipeline.BlogPipeline;
import com.example.webmagic.blogMagic.processor.BlogProcessor;
import com.example.webmagic.po.Blog;
import com.example.webmagic.util.DownloadUtil;
import com.example.webmagic.util.EmptyUtil;
import com.example.webmagic.util.HttpClientUtil;
import com.example.webmagic.util.UrlUtil;
import com.google.common.math.LongMath;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Json;
import us.codecraft.webmagic.selector.Selectable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLOutput;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component
public class ageProcessor implements PageProcessor {

    private static String url = "https://www.agemys.net/_getplay?aid=20220365&playindex=2&epindex=1&r=0.4321249042211117";

    //解析页面
    @Override
    public void process(Page page) {
        System.out.println(page.getHtml());
//        List<String> list = page.getHeaders().get("set-cookie");
//        for (String s : list) {
//            System.out.println(s);
//
//        }
        //提取视频的标题

        //提取视频对应的json数据

        //提取音频的url地址


        //提取视频画面的url地址


        //下载音视频

        //合并

    }

    private Site site = Site.me()
            .setCharset("utf-8")  //设置编码
            .setTimeOut(5000)     //设置超时时间，单位ms
            .setRetryTimes(3000)    //设置重试的间隔时间，单位ms
            .setSleepTime(3)
            .addHeader("referer", "https://www.agemys.net/")
            .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36");       //设置重试次数


    @Override
    public Site getSite() {
        return site;
    }

    /**
     * initialDelay当任务启动后，多久执行
     * fixedDelay执行间隔
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 100 * 100000)
    public void process() {
        Spider.create(new ageProcessor())
                .addUrl(url)
                .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(10000 * 1000))) //设置布隆过滤器
                .thread(5)
                .run();
    }

    public static Map<String, String> getCookie(int t) {



        /*
        t1 = Math['round'](Number(f('t1')) / 0x3e8) >> 0x5;
        0x3e8表示1000
         */

        long t1 = Math.round(t / 1000) >> 5;

        /*
        f2('k2', (t1 * (t1 % 0x1000) * 0x3 + 0x1450f) * (t1 % 0x1000) + t1);
         */
        long k2 = (t1 * (t1 % 0x1000) * 0x3 + 0x1450f) * (t1 % 0x1000) + t1;
        long t2 = new Date().getTime();
        String k2_s = String.valueOf(k2);
        String t2_s = String.valueOf(t2);
        t2_s = t2_s.substring(0, t2_s.length() - 1) + k2_s.substring(k2_s.length() - 1);

        Map<String, String> map = new HashMap<>();
        map.put("k2", k2_s);
        map.put("t2", t2_s);

        return map;

    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("referer", "https://www.agemys.net/play/20220450?playid=2_3");
        map.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36");
        map.put("cookie", "Hm_lvt_7fdef555dc32f7d31fadd14999021b7b=1675487198,1675772705,1675864335,1675928990; fa_c=1; Hm_lpvt_7fdef555dc32f7d31fadd14999021b7b=1675985623; t1=1675985768757; k1=30020062584; k2=1508984721308505; t2=1675985713635; fa_t=1675985713669");
        String s = HttpClientUtil.doGet("https://www.agemys.net/play/20220450?playid=2_3", map);
        //Set-Cookie: t1=1675984003058; Path=/
        if (EmptyUtil.isEmpty(s)) {
            return;
        }


        String setcookies = s.substring(s.indexOf("=") + 1, s.indexOf(";"));
        long t1 = Long.parseLong(setcookies);

        //https://www.agemys.net/_getplay?aid=20220450&playindex=2&epindex=2&r=0.7540821650178373
        long aid = 20220450;
        long playindex = 2;
        long epindex = 2;
        long t1_tmp = Integer.valueOf((int) Math.floor((t1) / 1000 + 0.5)) >> 5;
        long k2 = (t1_tmp * (t1_tmp % 4096) * 3 + 83215) * (t1_tmp % 4096) + t1_tmp;
        long t2 = new Date().getTime();
        String stringK2 = String.valueOf(k2);
        String stringT2 = String.valueOf(t2);
        stringT2 = stringT2.substring(0,stringT2.length()-1) + stringK2.substring(stringK2.length()-1);

        Map<String, String> map2 = new HashMap<>();

        String res = "t1=" + setcookies + ";" + "k2=" + stringK2 + ";" + "t2=" + stringT2 + ";";
        map2.put("referer", "https://www.agemys.net");
        map2.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36");
        map2.put("cookie", res);

        String result = HttpClientUtil.doGet(url, map2);
        System.out.println(result);


    }
}
