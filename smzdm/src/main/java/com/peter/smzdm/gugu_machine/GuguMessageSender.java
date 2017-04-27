package com.peter.smzdm.gugu_machine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peter.smzdm.config.EtcConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

/**
 * Created by kangyongliang262 on 17/4/19.
 */
@Service
public class GuguMessageSender {

    private static final Logger logger =
            LoggerFactory.getLogger(GuguMessageSender.class);

    private final static String USER_AGENT = "Mozilla/5.0";

    private static ObjectMapper mapper = new ObjectMapper(); // create once, reuse

    private static String outputFile = "../output/output.json";
    private static String md5File = "../output/output.md5";

    @Resource
    EtcConfig config;

    // HTTP GET request
    private int getUserId() throws Exception {

        String str = String.format("http://open.memobird.cn/home/setuserbind?ak=%s&" +
                        "timestamp=2014-11-14%%2014:22:39&memobirdID=%s&useridentifying=12121233",
                config.properties.getProperty(EtcConfig.gugu_ak), config.properties.getProperty(EtcConfig.gugu_memobirdId));

        URL url = new URL(str);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Accept", "text/plain");

        int responseCode = con.getResponseCode();
        logger.info("\nSending 'GET' request to URL : " + str);
        logger.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        InputStream is = url.openStream();
        UserIdResponse resp = mapper.readValue(is, UserIdResponse.class);
        logger.info("Response : " + resp);
        return resp.getShowapi_userid();

    }

    private static class Data {
        private String title;
        private String[] image_urls;
        private Object[] images;
        private String path;

        private String page_url;
        private String time;
        private String desc;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String[] getImage_urls() {
            return image_urls;
        }

        public void setImage_urls(String[] image_urls) {
            this.image_urls = image_urls;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public Object[] getImages() {
            return images;
        }

        public void setImages(Object[] images) {
            this.images = images;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getPage_url() {
            return page_url;
        }

        public void setPage_url(String page_url) {
            this.page_url = page_url;
        }
    }

    private static class UserIdResponse {
        private int showapi_res_code;
        private String showapi_res_error;
        private int showapi_userid;

        public int getShowapi_res_code() {
            return showapi_res_code;
        }

        public void setShowapi_res_code(int showapi_res_code) {
            this.showapi_res_code = showapi_res_code;
        }

        public String getShowapi_res_error() {
            return showapi_res_error;
        }

        public void setShowapi_res_error(String showapi_res_error) {
            this.showapi_res_error = showapi_res_error;
        }

        public int getShowapi_userid() {
            return showapi_userid;
        }

        public void setShowapi_userid(int showapi_userid) {
            this.showapi_userid = showapi_userid;
        }

        @Override
        public String toString() {
            return "UserIdResponse{" +
                    "showapi_res_code=" + showapi_res_code +
                    ", showapi_res_error='" + showapi_res_error + '\'' +
                    ", showapi_userid=" + showapi_userid +
                    '}';
        }
    }

    // HTTP POST request
    public void printMessage(String message, String type) throws Exception {
        int userId = getUserId();
        String url = "http://open.memobird.cn/home/printpaper";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        message = new String(Base64.getEncoder().encode(message.getBytes("GBK")));
        String urlParameters = String.format("ak=%s&printcontent=%s:%s&memobirdID=%s&userID=%d",
                config.properties.getProperty(EtcConfig.gugu_ak), config.properties.getProperty(EtcConfig.gugu_memobirdId), type, message, userId);

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        logger.info("\nSending 'POST' request to URL : " + url);
        logger.info("Post parameters : " + urlParameters);
        logger.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        logger.info(response.toString());
    }

}
