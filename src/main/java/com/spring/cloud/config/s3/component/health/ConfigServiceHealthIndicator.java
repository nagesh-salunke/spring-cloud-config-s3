package com.spring.cloud.config.s3.component.aspect;

import com.amazonaws.services.s3.AmazonS3URI;
import com.spring.cloud.config.s3.repository.S3Repository;
import java.util.Optional;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for configuration service.
 * Checks
 *  - Meta version from S3 bucket for file - config.metadata.
 *
 * @author Nagesh Salunke
 */
@Component("ConfigurationServiceHealthIndicator")
public class ConfigServiceHealthIndicator implements HealthIndicator {

  /**
   * S3Repository instance.
   */
  @Resource
  private S3Repository s3Repository;

  /**
   * S3 bucket name.
   */
  @Value("${spring.cloud.config.server.s3.uri}")
  private String bucketNameURI;

  @Override
  public Health health() {
    try {
      String bucketName = new AmazonS3URI(bucketNameURI).getBucket();
      Optional<String> currentBucketVersion = s3Repository.getBucketVersion(bucketName);
      if (currentBucketVersion.isPresent()) {
        return Health.up().withDetail("config-version", currentBucketVersion.get()).build();
      } else {
        return Health.down().withDetail("reason", "Could not access bucket " + bucketName).build();
      }
    } catch (Exception e) {
      return Health.down().withDetail("reason", "Could not access bucket " + bucketNameURI).build();
    }
  }
}
