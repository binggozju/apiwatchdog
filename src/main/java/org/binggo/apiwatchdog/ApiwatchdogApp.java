package org.binggo.apiwatchdog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ApiwatchdogApp {
	
	private static final Logger logger = LoggerFactory.getLogger(ApiwatchdogApp.class);
	
    public static void main( String[] args ) {
    	SpringApplication app = new SpringApplication(ApiwatchdogApp.class);
		app.run(args);
		
		logger.info("start apiwatchdog successfully.");
    }
}
