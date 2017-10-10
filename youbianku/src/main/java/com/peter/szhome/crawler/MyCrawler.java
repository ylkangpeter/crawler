package com.peter.szhome.crawler;

import com.peter.pinganfang.crawler.MyController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by kangyongliang262 on 17/4/20.
 */
@Component
public class MyCrawler extends WebCrawler {

    private static final Logger logger =
            LoggerFactory.getLogger(MyCrawler.class);

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp3|zip|gz))$");

    private com.peter.smzdm.crawler.CrawlStat myCrawlStat = new com.peter.smzdm.crawler.CrawlStat();

    private boolean shouldStop = false;

    private static BufferedWriter bw = null;

    private static Set<Integer> set = new HashSet<>();

//    private static Set<String>


    static {
        try {
            bw = new BufferedWriter(new FileWriter(new File(MyController.PATH + "/out.txt"), true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "http://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && href.contains("pinganfang") && href.startsWith("https") && !url.getAttribute("rel").contains("nofollow");
//        return false;
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        myCrawlStat.incProcessedPages();
        logger.info("URL: " + url);

        try {
            bw.write(url);
            bw.write("\n");
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();

            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            myCrawlStat.incTotalLinks(links.size());
            try {
                myCrawlStat.incTotalTextSize(text.getBytes("UTF-8").length);
            } catch (UnsupportedEncodingException ignored) {
                // Do nothing
            }
            logger.info(String.format("Text length: %d , Html length: %d, Links: %d, url: %s", text.length(), html.length(), links.size(), url));
        }
    }

    @Override
    public Object getMyLocalData() {
        return myCrawlStat;
    }

    @Override
    public void onBeforeExit() {
        try {
            bw.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        dumpMyData();
        System.exit(1);
    }

    public void dumpMyData() {
        int id = getMyId();
        // You can configure the log to output to file
        logger.info("Crawler {} > Processed Pages: {}", id, myCrawlStat.getTotalProcessedPages());
        logger.info("Crawler {} > Total Links Found: {}", id, myCrawlStat.getTotalLinks());
        logger.info("Crawler {} > Total Text Size: {}", id, myCrawlStat.getTotalTextSize());
    }


}
