package com.peter.smzdm.config;

import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by kangyongliang262 on 17/4/21.
 */
@Configuration
public class EtcConfig {

    public static final String outputFile = "output.file";

    public static final String outputFileMd5 = "output.file.md5";

    private static final String file = "my.properties";

    public static final String checkpoint = "checkpoint.properties";

    public static final String key_fetchAll = "fetchAll";

    public static final String key_keywords = "keywords";

    public static final String key_checkPoint = "checkPoint";

    public static final String gugu_ak = "gugu.ak";

    public static final String gugu_memobirdId = "gugu.memobirdId";


    final public Properties checkpointProperties = new Properties();
    final public Properties properties = new Properties();

    public EtcConfig() {
        InputStreamReader inputStream = null;
        try {
            inputStream = new InputStreamReader(EtcConfig.class.getClassLoader().getResourceAsStream(file), "UTF-8");
            properties.load(inputStream);
            properties.list(System.out);
        } catch (final IOException ioException) {
            ioException.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        try {
            inputStream = new InputStreamReader(EtcConfig.class.getClassLoader().getResourceAsStream(checkpoint), "UTF-8");
            checkpointProperties.load(inputStream);
            checkpointProperties.list(System.out);
        } catch (final IOException ioException) {
            ioException.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}


