package com.peter.szhome.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by kangyongliang262 on 17/4/21.
 */
@Component
public class MyCrawlerFactory<T extends WebCrawler> implements CrawlController.WebCrawlerFactory<T>, ApplicationContextAware {

    @Resource
    private ApplicationContext context;

    @Override
    public T newInstance() throws Exception {
        return (T) context.getBean(com.peter.pinganfang.crawler.MyCrawler.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }
}
