package com.spring.cloud.config.s3.component;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.spring.cloud.config.s3.AbstractTest;
import com.spring.cloud.config.s3.component.aspect.ConfigServiceHealthIndicator;
import com.spring.cloud.config.s3.repository.S3Repository;
import java.util.Optional;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Unit tests for {@link ConfigServiceHealthIndicator}.
 *
 * @author Nagesh Salunke
 */
public class ConfigServiceHealthIndicatorTests extends AbstractTest {

  /**
   * S3 repository instance.
   */
  @MockBean
  private S3Repository s3Repository;

  /**
   * Health indicator under test.
   */
  @Resource
  private ConfigServiceHealthIndicator configServiceHealthIndicator;

  /**
   * Test when valid version was returned from S3.
   */
  @Test
  public void health_validVersion_successServiceUp() {
    when(s3Repository.getBucketVersion(anyString())).thenReturn(Optional.of("-1"));
    Health health = configServiceHealthIndicator.health();
    Assert.assertEquals(Status.UP, health.getStatus());
  }

  /**
   * Test to validate status when there is exceptions due to
   * issue with getting config version from S3.
   */
  @Test
  public void health_NoVersion_serviceDown() {
    when(s3Repository.getBucketVersion(anyString()))
        .thenThrow(new RuntimeException("Some exceptions"));
    Health health = configServiceHealthIndicator.health();
    Assert.assertEquals(Status.DOWN, health.getStatus());
  }
}
