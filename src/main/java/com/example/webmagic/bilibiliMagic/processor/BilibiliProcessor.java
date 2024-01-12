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


//爬取分p视频
@Component
public class BilibiliProcessor implements PageProcessor {

    private static int index = 1;
    private static int num = index-1;
    private static String basecookie = "_uuid=53E3C6FE-1D8A-F3B2-7E23-ACA84A536D4F63234infoc; buvid3=D401D951-D96A-4691-A664-CDAF15B0234D53941infoc; CURRENT_FNVAL=16; rpdid=|(J|)JulY|Yk0J'ulmuJk)l~R; sid=a9w7482a; finger=-166317360; LIVE_BUVID=AUTO8715959029630612; PVID=3;";
    private static String beforeUrl = "https://www.bilibili.com/video/BV1LT4y1e72X?p=";
    private static String url = beforeUrl;
    private static int QUALITY = 120;
    private static String cookie =  "buvid3=22BAEFCF-20DE-704E-9EA9-7D5EA85B420505603infoc; i-wanna-go-back=-1; _uuid=991B8C5E-96C4-A9A5-A1DE-B175A64103E8907263infoc; buvid4=901B68D0-2B30-EC14-1E33-03D052809BB911094-022081021-KXkDA7bwdIpRVrb4MGeykw%3D%3D; buvid_fp_plain=undefined; DedeUserID=18757587; DedeUserID__ckMd5=79197dfe0a71c602; nostalgia_conf=-1; CURRENT_BLACKGAP=0; b_ut=5; LIVE_BUVID=AUTO5216610872674217; b_nut=100; rpdid=|(Rllmu)mRk0J'uYYm|RJ|R|; header_theme_version=CLOSE; CURRENT_FNVAL=4048; CURRENT_QUALITY="+QUALITY+"; CURRENT_PID=d0cc7200-ca4f-11ed-9db2-4752a2e4783e; FEED_LIVE_VERSION=V_TOPSWITCH_FLEX_TOTOP; fingerprint=b0d8a76a386f3885fc176f13abaa918e; buvid_fp=b0d8a76a386f3885fc176f13abaa918e; SESSDATA=1460e7b8%2C1703860564%2C34790%2A72SkTl1_wmXvcL62qWOcabWGdjJvVT9QJ45K5o2nMDqGpWljJDisNlbRQHbRUicaKCI1squQAAVwA; bili_jct=dbd379f5eabd5d0b0c96df1820246d8e; home_feed_column=5; browser_resolution=1536-754; b_lsid=22102BC57_1891CD67761; PVID=1; bp_video_offset_18757587=814181978056163300; sid=dwtl256a";

    //解析页面
    @Override
    public void process(Page page) {


        //提取视频的标题
        Document titleDoc = Jsoup.parse(page.getHtml().get());
        Elements titleScriptEle = titleDoc.select("script");
        String titleDataStr = titleScriptEle.get(4).data();
        String titleData = titleDataStr.substring(titleDataStr.indexOf("{"),titleDataStr.indexOf("function()")-2);
//        String titleData = titleDataStr.substring(titleDataStr.indexOf("{"));

        JSONObject titleJSONObject = JSONObject.parseObject(titleData);
        String title = titleJSONObject.getJSONObject("videoData")
                .getJSONArray("pages")
                .getJSONObject(num++)
                .getString("part");
        int size = titleJSONObject.getJSONObject("videoData").getJSONArray("pages").size();
        title = title.replace("  "," ")
                .replace("/","_");
        title = "P"+index+"."+title;
        System.out.println("视频标题为:"+title);
        if (EmptyUtil.isEmpty(title)){
            return;
        }
        if (index < size){
            index++;
            url =  beforeUrl + index;
            page.addTargetRequest(url);
        }

        //提取视频对应的json数据
        Document pageDoc = Jsoup.parse(page.getHtml().get());
        Elements scriptEle = pageDoc.select("script");
        String dataStr = scriptEle.get(3).data();
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
//        DownloadUtil.ffmpegMerge(videoPath,audioPath);
        if(index == size){
            DownloadUtil.mergeList();
        }

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
