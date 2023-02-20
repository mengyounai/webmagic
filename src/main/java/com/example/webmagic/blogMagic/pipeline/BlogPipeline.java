package com.example.webmagic.blogMagic.pipeline;

import com.example.webmagic.po.Blog;
import com.example.webmagic.service.BlogService;
import com.example.webmagic.util.EmptyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

@Component
public class BlogPipeline implements Pipeline {

    @Autowired
    private BlogService blogService;

    @Override
    public void process(ResultItems resultItems, Task task) {

        Object o = resultItems.get("blog");
        if (o instanceof Blog) {
            Blog blog = (Blog) o;
            blogService.save(blog);
        }

    }
}
