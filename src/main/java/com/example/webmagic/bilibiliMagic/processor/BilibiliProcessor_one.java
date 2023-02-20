package com.example.webmagic.bilibiliMagic.processor;

import com.alibaba.fastjson.JSONObject;
import com.example.webmagic.util.DownloadUtil;
import com.example.webmagic.util.EmptyUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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

//爬取单个视频
//@Component
public class BilibiliProcessor_one implements PageProcessor {

    private static String url = "https://www.bilibili.com/video/BV1gB4y1m7BR/?spm_id_from=333.851.b_62696c695f7265706f72745f646f756761.9&vd_source=01fd8b7382cc4009c06bedf3c5353d82";

    //解析页面
    @Override
    public void process(Page page) {

        //提取视频的标题
        String title = page.getHtml().css("div#viewbox_report h1.video-title","text").get();
        if (EmptyUtil.isEmpty(title)){
            return;
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

        Spider.create(new BilibiliProcessor_one())
                .addUrl(url)
//                .setDownloader(httpClientDownloader)
                .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(10000 * 1000))) //设置布隆过滤器
                .thread(10)
                .run();
    }

    public static void main(String[] args) {
        Spider.create(new BilibiliProcessor_one())
                .addUrl(url)
                .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(10000 * 1000))) //设置布隆过滤器
                .thread(5)
                .run();
    }
}
