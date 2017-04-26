package com.peter.smzdm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * config files for input/output etc
 * Created by kangyongliang262 on 17/4/20.
 */
@Configuration
public class CrawlerConfig {

    @Value("${crawl.init.url}")
    public String initUrl;

    @Value("${crawl.data.path}")
    public String dataFolder;

    @Value("${crawl.data.file}")
    public String dataFile;

    @Value("${crawl.md5.file}")
    public String md5File;

    @Value("${gugu.ak}")
    public String guguAk;

    @Value("${crawl.threadNumber}")
    public int threadNumber;
}
