package com.june.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.june.service.ThirdService;

@Component
@EnableScheduling
public class SchedulerTask {

	private Logger logger = LoggerFactory.getLogger(SchedulerTask.class);
	
	@Autowired
	private ThirdService thirdService;
	
	/**
	 * 每隔2小时刷新一次token
	 */
	@Scheduled(cron="0 0 0/2 * * *")
	public void refreshToken() {
		logger.info("----------start to refresh token----------");
		thirdService.refreshToken();
		thirdService.refreshAuthorizerToken();
		logger.info("----------end to refresh token -----------");
	}

}
