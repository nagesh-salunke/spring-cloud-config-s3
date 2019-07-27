package com.spring.cloud.config.s3.config;

import com.spring.cloud.config.s3.constant.ConfigConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.config.server.support.AbstractScmAccessorProperties;

/**
 * Repository properties for configuring s3 as config repository.
 *
 * @author Nagesh Salunke
 */
@Getter
@Setter
@ConfigurationProperties(ConfigConstants.S3_REPO_PROP_PATH)
public class S3EnvironmentRepositoryProperties extends AbstractScmAccessorProperties {

  /**
   * Label to indicate repository tag/branch.
   */
  private static final String DEFAULT_LABEL = "master";

  public S3EnvironmentRepositoryProperties() {
    super();
    setDefaultLabel(DEFAULT_LABEL);
  }

}
