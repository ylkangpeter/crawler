package com.peter.smzdm.crawler;

import com.peter.smzdm.util.MyUtil;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
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

    private CrawlStat myCrawlStat = new CrawlStat();

    @Resource
    private MyUtil util;

    private boolean shouldStop = false;

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
                && href.startsWith("http://www.smzdm.com/youhui/p") && !shouldStop;
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

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            String time = "";

            Document doc = Jsoup.parse(html);
            Elements items = doc.select("div.list.list_preferential");
            for (Iterator<Element> iter = items.iterator(); iter.hasNext(); ) {
                Element e = iter.next();
                time = util.normalizeTime(e.select("span.lrTime").text());
                util.updateTime(time);
                if (!util.shouldStop(time)) {
                    String title = e.select("div h2 a").text();
                    if (util.isMatch(title)) {
                        String itemUrl = e.select("div h2 a").attr("href");
                        String desc = e.select("div div.lrInfo p").text();
                        try {
                            String msg = String.format("%s\r\n%s\r\n%s\r\n%s\r\n", time, title, itemUrl, desc);
                            logger.info("found match: \n" + msg);
                            util.writeOutputFile(msg);
                        } catch (IOException e1) {
                            logger.error(e1.getMessage(), e1);
                        }
                    }
                } else {
                    logger.info(String.format("reach last crawler point %s stooooop!", time));
                    shouldStop = true;
                    return;
                }

            }

            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            myCrawlStat.incTotalLinks(links.size());
            try {
                myCrawlStat.incTotalTextSize(text.getBytes("UTF-8").length);
            } catch (UnsupportedEncodingException ignored) {
                // Do nothing
            }
            logger.info("Text length: " + text.length());
            logger.info("Html length: " + html.length());
            logger.info("Number of outgoing links: " + links.size());
        }
    }

    @Override
    public Object getMyLocalData() {
        return myCrawlStat;
    }

    @Override
    public void onBeforeExit() {
        try {
            util.finish();
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
