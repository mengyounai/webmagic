package com.example.webmagic.bilibiliMagic.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.example.webmagic.blogMagic.pipeline.BlogPipeline;
import com.example.webmagic.blogMagic.processor.BlogProcessor;
import com.example.webmagic.po.Blog;
import com.example.webmagic.util.DownloadUtil;
import com.example.webmagic.util.EmptyUtil;
import com.example.webmagic.util.UrlUtil;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLOutput;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//爬取分p视频
@Component
public class BilibiliProcessor implements PageProcessor {

    private static int index = 114;
    private static int num = index-1;
    private static String basecookie = "_uuid=53E3C6FE-1D8A-F3B2-7E23-ACA84A536D4F63234infoc; buvid3=D401D951-D96A-4691-A664-CDAF15B0234D53941infoc; CURRENT_FNVAL=16; rpdid=|(J|)JulY|Yk0J'ulmuJk)l~R; sid=a9w7482a; finger=-166317360; LIVE_BUVID=AUTO8715959029630612; PVID=3;";
    private static String url = "https://www.bilibili.com/video/BV1Vf4y1T7bw/?p=" +index + "&vd_source=01fd8b7382cc4009c06bedf3c5353d82";
    private static String cookie =  basecookie+"_jct=2b7857b0d0b011eaa7f77ea539b9b3b7; DedeUserID=391625460; DedeUserID__ckMd5=accdac0e7e5f4f56; SESSDATA=a7c5c1cc%2C1611478568%2C56408*71; bili_jct=fe0e50c7ef84f6938d345f2b0c5e31d4";

    //解析页面
    @Override
    public void process(Page page) {

        //提取视频的标题
        Document titleDoc = Jsoup.parse(page.getHtml().get());
        Elements titleScriptEle = titleDoc.select("script");
        String titleDataStr = titleScriptEle.get(3).data();
        String titleData = titleDataStr.substring(titleDataStr.indexOf("{"),titleDataStr.indexOf("function()")-2);
        JSONObject titleJSONObject = JSONObject.parseObject(titleData);
        String title = titleJSONObject.getJSONObject("videoData")
                .getJSONArray("pages")
                .getJSONObject(num++)
                .getString("part");
        System.out.println("视频标题为:"+title);
        if (EmptyUtil.isEmpty(title)){
            return;
        }
        if (index < 200){
            index++;
            url = "https://www.bilibili.com/video/BV1Vf4y1T7bw/?p=" +index + "&vd_source=01fd8b7382cc4009c06bedf3c5353d82";
            page.addTargetRequest(url);
        }

        //提取视频对应的json数据
        Document pageDoc = Jsoup.parse(page.getHtml().get());
        Elements scriptEle = pageDoc.select("script");
        String dataStr = scriptEle.get(2).data();
//        System.out.println("视频json数据为:"+dataStr);
        String data = dataStr.substring(dataStr.indexOf("{"));
        JSONObject jsonObject = JSONObject.parseObject(data);
//        System.out.println(data);


        //提取音频的url地址
        String audio_url = jsonObject
                .getJSONObject("data")
                .getJSONObject("dash")
                .getJSONArray("audio")
                .getJSONObject(0)
                .getJSONArray("backupUrl")
                .getString(0);

        //提取视频画面的url地址
        String video_url  = jsonObject
                .getJSONObject("data")
                .getJSONObject("dash")
                .getJSONArray("video")
                .getJSONObject(0)
                .getJSONArray("backupUrl")
                .getString(0);

        //下载音视频
        String videoPath = DownloadUtil.videoDownLoad(video_url, title);
        String audioPath = DownloadUtil.audioDownLoad(audio_url,title);
        //合并
        DownloadUtil.ffmpegMerge(videoPath,audioPath);
    }

    private Site site = Site.me()
            .setCharset("utf-8")  //设置编码
            .setTimeOut(5000)     //设置超时时间，单位ms
            .setRetryTimes(3000)    //设置重试的间隔时间，单位ms
            .setSleepTime(3)
            .addHeader("referer","https://www.bilibili.com/")
            .addCookie("cookie",cookie)
            .addHeader("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36")
            ;       //设置重试次数


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
        //创建下载器DownLoader
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(new Proxy("27.157.156.1",16207)));

        Spider.create(new BilibiliProcessor())
                .addUrl(url)
//                .setDownloader(httpClientDownloader)
                .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(10000 * 1000))) //设置布隆过滤器
                .thread(10)
                .run();
    }

    public static void main(String[] args) {
        Spider.create(new BilibiliProcessor())
                .addUrl(url)
                .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(10000 * 1000))) //设置布隆过滤器
                .thread(5)
                .run();
    }
}
