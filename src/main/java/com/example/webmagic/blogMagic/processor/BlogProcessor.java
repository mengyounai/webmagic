package com.example.webmagic.blogMagic.processor;

import com.example.webmagic.blogMagic.pipeline.BlogPipeline;
import com.example.webmagic.po.Blog;
import com.example.webmagic.service.BlogService;
import com.example.webmagic.util.EmptyUtil;
import com.example.webmagic.util.UrlUtil;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//@Component
public class BlogProcessor implements PageProcessor {

    private String url = "http://gasaiyuno.top/";

    @Autowired
    private BlogPipeline blogPipeline;

    //解析页面
    @Override
    public void process(Page page) {

        //获取所有标题节点
        List<Selectable> nodes = page.getHtml().css("h3 a.m-black").nodes();

        //判断获取到的节点是否为空
        if (EmptyUtil.isEmpty(nodes)) {
            //如果为空，表示这是详情页，解析页面，保存数据
            this.saveBlog(page);
        } else {
            //如果不为空，表示这个列表页，解析出详情页的url地址，放到队列中
            for (Selectable node : nodes) {
                //获取详情页地址
                String detailUrl = node.links().toString();
                //放入到队列中，先放先处理
                page.addTargetRequest(detailUrl);
            }

        }
        //获取下一页的url
        String nextUrl = page.getHtml().css("div.bottom div div.right a").links().get();
        //把url放到队列中
        page.addTargetRequest(nextUrl);

        //解析页面,并把结果放到resultItem中
//        page.putField("h3",page.getHtml().css("h3 a.m-black").all());
//        page.putField("最新推荐",page.getHtml().xpath("//div[@class=segment]/a[@class=m-text-thin]").all());
//        page.putField("Mirai",page.getHtml().xpath("//div/a").regex(".*Mirai.*").all());
//        page.addTargetRequests(page.getHtml().css("h3 a.m-black").links().all());
//        page.putField("h2",page.getHtml().css("div.padded h2").get());
    }

    private void saveBlog(Page page) {
        //创建对象
        Blog blog = new Blog();
        //解析对象
        Html html = page.getHtml();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String updateTime = html.css("div#waypoint span", "text").toString();
        Date parse = new Date();
        try {
            parse = sdf.parse(updateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String title = html.css("div.padded h2", "text").toString();
        String content = Jsoup.parse(html.css("div#content").toString()).text();
        String firstPicture = UrlUtil.getImgUrl(html.css("div img.fluid").toString());

        //将数据封装到对象中
        blog.setTitle(title);
        blog.setContent(content);
        blog.setFirstPicture(firstPicture);
        blog.setUpdateTime(parse);
        blog.setCreateTime(new Date());

        //保存数据
        page.putField("blog", blog);

    }

    private Site site = Site.me()
            .setCharset("utf-8")  //设置编码
            .setTimeOut(5000)     //设置超时时间，单位ms
            .setRetryTimes(3000)    //设置重试的间隔时间，单位ms
            .setSleepTime(3);       //设置重试次数


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
        Spider.create(new BlogProcessor())
                .addUrl(url)
                .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(10000 * 1000))) //设置布隆过滤器
                .addPipeline(blogPipeline)
                .thread(5)
                .run();
    }

    public static void main(String[] args) {
        Spider.create(new BlogProcessor())
                .addUrl("http://gasaiyuno.top/")
                .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(10000 * 1000))) //设置布隆过滤器
//                .addPipeline(new FilePipeline("result"))
                .thread(5)
                .run();
    }
}
