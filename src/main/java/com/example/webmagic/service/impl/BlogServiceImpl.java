package com.example.webmagic.service.impl;

import com.example.webmagic.dao.BlogRepository;
import com.example.webmagic.po.Blog;
import com.example.webmagic.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Override
    @Transactional
    public void save(Blog blog) {

        //判断是否存在
        Blog b = blogRepository.findByTitle(blog.getTitle());
        if (b!=null){
            //存在，继续判断是否更新
            if (b.getUpdateTime() != blog.getUpdateTime()){
                blogRepository.save(b);
            }
        }else {
            //不存在，新增
            blogRepository.save(blog);
        }

    }
}
