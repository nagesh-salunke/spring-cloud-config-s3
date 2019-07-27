package com.spring.cloud.config.s3.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.spring.cloud.config.s3.AbstractTest;
import com.spring.cloud.config.s3.ConfigServerTestUtils;
import com.spring.cloud.config.s3.exceptions.SystemException;
import com.spring.cloud.config.s3.config.S3EnvironmentRepositoryProperties;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.environment.SearchPathLocator.Locations;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.FileSystemUtils;

/**
 * Tests for {@link S3EnvironmentRepository}
 *
 * @author Nagesh Salunke
 */
public class S3EnvironmentRepositoryTests extends AbstractTest {

  @Resource
  private StandardEnvironment standardEnvironment;

  @Resource
  private S3EnvironmentRepositoryProperties properties;

  @MockBean
  private S3Repository s3Repository;

  private S3EnvironmentRepository s3EnvironmentRepository;

  private File basedir = new File("target/repos/config-repo");

  @Before
  @Override
  public void setUp() throws Exception {
    this.s3EnvironmentRepository = new S3EnvironmentRepository(standardEnvironment, properties, s3Repository);
    this.s3EnvironmentRepository.setUri("s3://anything");
    this.s3EnvironmentRepository.setBasedir(this.basedir);
    if (this.basedir.exists()) {
      FileSystemUtils.deleteRecursively(this.basedir);
    }
  }

  @Test
  public void getConfigHappyCase_withValidRepo_getsHierarchicalConfig()
      throws SystemException {
    mockPrepareLocalRepo();
    when(s3Repository.getBucketVersion(anyString())).thenReturn(Optional.of("v1"));
    Environment environment = this.s3EnvironmentRepository.findOne("bar", "staging", "master");
    Assert.assertEquals(3, environment.getPropertySources().size());
  }

  @Test(expected = IllegalStateException.class)
  public void getConfigExceptionOnDownload_withValidRepo_getsException_test()
      throws SystemException {
    Mockito.doAnswer(k -> {
      throw new SystemException("Exception when downloading s3 bucket.");
    }).when(s3Repository).downloadBucket(anyString(), any());
    when(s3Repository.getBucketVersion(anyString())).thenReturn(Optional.of("v1"));
    this.s3EnvironmentRepository.findOne("bar", "staging", "master");
  }

  @Test
  public void getConfigForceRefresh_withValidRepo_getsNewVersion()
      throws SystemException {
    mockPrepareLocalRepo();
    when(s3Repository.getBucketVersion(anyString())).thenReturn(Optional.of("v1"));
    this.s3EnvironmentRepository.findOne("bar", "staging", "master");
    when(s3Repository.getBucketVersion(anyString())).thenReturn(Optional.of("v2"));
    s3EnvironmentRepository.refresh();
    Assert.assertEquals("v2", s3EnvironmentRepository.getConfigVersion());
  }

  @Test
  public void getConfigNotPresentInS3_withValidRepo_getsDefaultVersion()
      throws SystemException {
    String bucketName = "b3";
    this.s3EnvironmentRepository.setUri("s3://" + bucketName);
    mockPrepareLocalRepo();
    when(s3Repository.getBucketVersion(bucketName)).thenReturn(Optional.of("v1"));
    s3EnvironmentRepository.refresh();
    Assert.assertEquals("v1", s3EnvironmentRepository.getConfigVersion());
    when(s3Repository.getBucketVersion(bucketName)).thenReturn(Optional.empty());
    s3EnvironmentRepository.refresh();
    Assert.assertEquals(s3EnvironmentRepository.DEFAULT_CONFIG_VERSION, s3EnvironmentRepository.getConfigVersion());
  }

  @Test
  public void getConfigLocations_withValidRepo_validVersionAndLocation()
      throws SystemException {
    mockPrepareLocalRepo();
    when(s3Repository.getBucketVersion(anyString())).thenReturn(Optional.of("v1"));
    Locations locations = s3EnvironmentRepository.getLocations("bar", "staging", "master");
    Assert.assertEquals("v1", locations.getVersion());
    Assert.assertEquals(3, locations.getLocations().length);
  }

  @Test
  public void getConfig_deletedLocalRepoBeforeRefresh_restoreLocalRepo()
      throws SystemException, IOException {
    mockPrepareLocalRepo();
    when(s3Repository.getBucketVersion(anyString())).thenReturn(Optional.of("v1"));
    Locations locations = s3EnvironmentRepository.getLocations("bar", "staging", "master");
    //locations
    Assert.assertEquals("v1", locations.getVersion() );
    Assert.assertEquals(3, locations.getLocations().length);

    //If local files are deleted by some process.
    FileSystemUtils.deleteRecursively(basedir.toPath());

    locations = s3EnvironmentRepository.getLocations("bar", "staging", "master");
    s3EnvironmentRepository.refresh(); // this should restore deleted config.
    Assert.assertEquals("v1", locations.getVersion(), "v1");
    Assert.assertEquals(3, locations.getLocations().length);
  }

  private void mockPrepareLocalRepo() throws SystemException {
    Mockito.doAnswer(k -> {
      Object[] args = k.getArguments();
      File destDir = (File) args[1];
      ConfigServerTestUtils.prepareLocalRepo(destDir, "config-repo");
      return null;
    }).when(s3Repository).downloadBucket(anyString(), any());
  }
}
