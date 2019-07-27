package com.spring.cloud.config.s3.config;

import com.spring.cloud.config.s3.constant.ConfigConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Scheduler configuration - This configuration is for ThreadPool for scheduler.
 * Config Refresh Task threads will use this scheduler config.
 *
 * @author Nagesh Salunke
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = ConfigConstants.S3_REPO_PROP_PATH, name = "enablePollingschedule",
    havingValue = "true", matchIfMissing = true)
public class SchedulerConfig implements SchedulingConfigurer {

  private static final int DEFAULT_POOL_SIZE = 2;

  @Override
  public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(DEFAULT_POOL_SIZE);
    threadPoolTaskScheduler.setThreadNamePrefix("ScheduleTaskPool-");
    threadPoolTaskScheduler.initialize();
    scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
  }
}