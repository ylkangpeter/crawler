package com.peter.smzdm.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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

    private CrawlStat myCrawlStat = new CrawlStat();

    private boolean shouldStop = false;

    private static BufferedWriter bw = null;

    private static int inx_start = "https://www.youbianku.cn/node/".length();

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
//        String href = url.getURL().toLowerCase();
//        return !FILTERS.matcher(href).matches()
//                && ((href.startsWith("https://www.youbianku.cn/node/") && isContinue(href))) && !shouldStop;
        return false;
    }

    private boolean isContinue(String href) {
        int number = 0;
        if (href.endsWith("p")) {
            number = Integer.parseInt(href.substring(inx_start, href.length() - 4));
        } else {
            number = Integer.parseInt(href.substring(inx_start, href.length()));
        }
        if (set.contains(number)) {
            return false;
        } else {
            set.add(number);
        }
        return true;
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

            Document doc = Jsoup.parse(html);
            Elements items = doc.select("ul .field-item");

            String address = items.get(2).select("[itemprop]").text();
            String district = items.get(5).select("[itemprop]").text();
            String city = items.get(4).select("[itemprop]").text();
            String province = items.get(3).select("[itemprop]").text();
            String postcode = items.get(6).select("[itemprop]").text();

//            String nextUrl = items.get(items.size() - 1).select("a").text();

            try {
                bw.write(province);
                bw.write("\t");
                bw.write(city);
                bw.write("\t");
                bw.write(district);
                bw.write("\t");
                bw.write(postcode);
                bw.write("\t");
                bw.write(address);
                bw.write("\t");
                bw.write(url);
                bw.write("\n");
            } catch (Exception e) {
                e.printStackTrace();
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
