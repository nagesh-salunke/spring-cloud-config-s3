package com.spring.cloud.config.s3.config;

import com.spring.cloud.config.s3.constant.ConfigConstants;
import com.spring.cloud.config.s3.repository.S3EnvironmentRepository;
import com.spring.cloud.config.s3.repository.S3Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Custom repository - S3 configuration.
 *
 * @author Nagesh Salunke
 */
@Configuration
@EnableConfigurationProperties(S3EnvironmentRepositoryProperties.class)
public class CustomEnvironmentRepositoryConfiguration {

  @ConditionalOnProperty(prefix = ConfigConstants.S3_REPO_PROP_PATH, name = "enabled",
      havingValue = "true")
  @Bean
  public EnvironmentRepository environmentRepository(ConfigurableEnvironment environment,
      S3EnvironmentRepositoryProperties properties, S3Repository s3Repository) {
    return new S3EnvironmentRepository(environment, properties, s3Repository);
  }

}
