package com.peter.pinganfang;

import com.peter.smzdm.crawler.MyController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by kangyongliang262 on 17/4/20.
 */

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class AppMain extends SpringBootServletInitializer {

    @SuppressWarnings("resource")
    public static void main(final String[] args) {
        if (args.length == 2) {
            MyController.PATH = args[0];
            MyController.numberOfCrawlers = Integer.parseInt(args[1]);
        }
        ConfigurableApplicationContext context = SpringApplication.run(AppMain.class, args);

    }
}
