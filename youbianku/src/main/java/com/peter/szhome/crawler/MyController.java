package com.peter.szhome.crawler;

import com.peter.pinganfang.crawler.MyCrawlerFactory;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * Created by kangyongliang262 on 17/4/21.
 */
@Component
public class MyController {

    private static final Logger logger =
            LoggerFactory.getLogger(MyController.class);


//    public static String PATH = "/Users/kangyongliang262/Downloads/pinganfang";


    public static String PATH = "/data1/ylkang/pinganfang/crawler";


    public static int numberOfCrawlers = 10;

    @Resource
    MyCrawlerFactory factory;

    @PostConstruct
    public void run() throws Exception {

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(PATH);
        config.setResumableCrawling(true);
        config.setIncludeBinaryContentInCrawling(false);
        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
//        controller.addSeed("https://www.youbianku.cn/node/1?amp");

        String url = "https://www.pinganfang.com";
        controller.addSeed(url);

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(factory, numberOfCrawlers);
        List<Object> crawlersLocalData = controller.getCrawlersLocalData();
        long totalLinks = 0;
        long totalTextSize = 0;
        int totalProcessedPages = 0;
        for (Object localData : crawlersLocalData) {
            com.peter.smzdm.crawler.CrawlStat stat = (com.peter.smzdm.crawler.CrawlStat) localData;
            totalLinks += stat.getTotalLinks();
            totalTextSize += stat.getTotalTextSize();
            totalProcessedPages += stat.getTotalProcessedPages();
        }

        logger.info("Aggregated Statistics:");
        logger.info("\tProcessed Pages: {}", totalProcessedPages);
        logger.info("\tTotal Links found: {}", totalLinks);
        logger.info("\tTotal Text Size: {}", totalTextSize);
    }
}