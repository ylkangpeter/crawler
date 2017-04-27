package com.peter.smzdm.util;

import com.peter.smzdm.config.EtcConfig;
import com.peter.smzdm.gugu_machine.GuguMessageSender;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by kangyongliang262 on 17/4/21.
 */
@Component
public class MyUtil {

    @Resource
    private EtcConfig config;

    @Resource
    private GuguMessageSender guguService;

    private BufferedWriter fileWriter = null;
    private BufferedWriter md5Writer = null;
    private String lastTime = "";
    private String latestTime = "";

    public boolean shouldStop(String time) {
        return time.compareTo(lastTime) <= 0;
    }

    public void updateTime(String time) {
        if (latestTime.compareTo(time) < 0) {
            latestTime = time;
        }
    }

    /**
     * 2017-04-11 12:20
     * 04-11 12:20
     * 12:20
     */
    public String normalizeTime(String time) {
        Calendar cal = Calendar.getInstance();
        if (time.length() == 5) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(cal.getTime()) + " " + time;
        } else if (time.length() == 11) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            return sdf.format(cal.getTime()) + "-" + time;
        } else {
            return time;
        }


    }

    public boolean isMatch(String title) {
        if (config.properties.get(EtcConfig.key_fetchAll).equals("1")) {
            return true;
        } else {
            String[] words = config.properties.get(EtcConfig.key_keywords).toString().split(",");
            for (String word : words) {
                if (title.contains(word)) {
                    return true;
                }
            }
        }
        return false;
    }

    @PostConstruct
    private void init() throws IOException {
        lastTime = config.checkpointProperties.getProperty(EtcConfig.key_checkPoint);
        File outputFile = new File(config.properties.getProperty(EtcConfig.outputFile));
        if (outputFile.exists()) {
            outputFile.renameTo(new File(config.properties.getProperty(EtcConfig.outputFile) + "." + System.currentTimeMillis()));
        }
        fileWriter = new BufferedWriter(new FileWriter(outputFile));
        md5Writer = new BufferedWriter(new FileWriter(new File(config.properties.getProperty(EtcConfig.outputFileMd5))));
    }


    @PreDestroy
    public void finish() throws Exception {
        if (fileWriter != null) {
            fileWriter.close();
        }
        // yeah, not safe
        BufferedReader br = new BufferedReader(new FileReader(config.properties.getProperty(EtcConfig.outputFile)));
        MessageDigest digest = MessageDigest.getInstance("md5");
        String line = "";
        StringBuilder message = new StringBuilder();
        while ((line = br.readLine()) != null) {
            message.append(line);
        }
        br.close();
        String md5 = new HexBinaryAdapter().marshal(digest.digest(message.toString().getBytes("utf-8")));

        br = new BufferedReader(new FileReader(config.properties.getProperty(EtcConfig.outputFileMd5)));
        String oldMd5 = br.readLine();

        if (message.length() > 0 && !md5.equals(oldMd5)) {
//             send gugu message
            guguService.printMessage(message.toString(), "T");
        }
        // write checkpoint

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(EtcConfig.checkpoint)));
        bw.write("checkpoint=" + latestTime);
        bw.close();
        // write md5
        md5Writer.write(md5);
        if (md5Writer != null) {
            md5Writer.close();
        }
    }

    public void writeOutputFile(String msg) throws IOException {
        fileWriter.write(msg);
        fileWriter.flush();
    }

}
