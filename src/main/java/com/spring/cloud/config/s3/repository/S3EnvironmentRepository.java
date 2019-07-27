package com.spring.cloud.config.s3.repository;

import com.amazonaws.services.s3.AmazonS3URI;
import com.spring.cloud.config.s3.config.S3EnvironmentRepositoryProperties;
import com.spring.cloud.config.s3.constant.ConfigConstants;
import com.spring.cloud.config.s3.exceptions.SystemException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.server.environment.AbstractScmEnvironmentRepository;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.cloud.config.server.environment.SearchPathLocator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

/**
 * Repository to read configuration from Amazon Simple Storage Service.
 * Implementation of {@link EnvironmentRepository} for Amazon S3.
 *
 * @author Nagesh Salunke
 */
@Log4j2
public class S3EnvironmentRepository extends AbstractScmEnvironmentRepository
    implements EnvironmentRepository, SearchPathLocator, InitializingBean {

  /**
   * {@link S3Repository} for operations from S3.
   */
  private S3Repository s3Repository;

  /**
   * Default version for configuration.
   */
  public static final String DEFAULT_CONFIG_VERSION = "latest";

  @Setter
  @Getter
  private String configVersion;

  /**
   * Constructor with env and properties as parameters.
   * @param environment - environment.
   * @param properties - S3EnvironmentRepository properties.
   */
  @Autowired
  public S3EnvironmentRepository(ConfigurableEnvironment environment,
      S3EnvironmentRepositoryProperties properties, S3Repository s3Repository) {
    super(environment, properties);
    this.s3Repository = s3Repository;
  }

  @Override
  public synchronized void afterPropertiesSet() {
    Assert.state(getUri() != null,
        "You need to configure a uri for the s3 bucket (e.g. 's3://bucket/')");
    new AmazonS3URI(getUri());
  }

  @Override
  public synchronized Locations getLocations(String application, String profile, String label) {
    String version = refresh();
    return new Locations(application, profile, label, version,
        getSearchLocations(getWorkingDirectory(), application, profile, label));
  }

  /**
   * Refreshes the config state with local directory.
   * {@link Scheduled} Operation configured by {spring.cloud.config.server.s3.pollingRefreshRate}
   * @return Current version of the configuration.
   */
  @Scheduled(fixedRateString = "${spring.cloud.config.server.s3.pollingRefreshRate:60000}")
  public synchronized String refresh() {
    try {
      if (shouldFetchConfig()) {
        log.debug("Attempting to refresh configuration by pulling from s3");
        //fetchConfiguration : To temp directory
        final Path tmpDir = Files.createTempDirectory(ConfigConstants.TEMP_CONFIG_REPO_PREFIX);
        fetchConfiguration(tmpDir);
        //deleteBaseDir : New config is available
        deleteBaseDirIfExists();
        //Copy newly fetched config to baseDir
        copyConfigurationToBaseDir(tmpDir);
        //Cleanup tempDir
        FileSystemUtils.deleteRecursively(tmpDir);
      }
      log.info("Current configuration version (configVersion={})", getConfigVersion());
    } catch (SystemException | IllegalStateException | IOException e) {
      log.error("Exception on refreshing local config, Serving old configuration."
          + "(configVersion={})", getConfigVersion());
      throw new IllegalStateException("Unable to refresh local config.", e);
    }
    return getConfigVersion();
  }

  /**
   * Fetch configuration from S3 to destDir.
   *
   * @param destDir - destination directory
   */
  private void fetchConfiguration(Path destDir) throws SystemException {
    String bucketName = new AmazonS3URI(getUri()).getBucket();
    try {
      log.info("Fetching config from s3 bucket to local directory. (bucketName={}, localDir={})",
          bucketName, destDir.toAbsolutePath());

      s3Repository.downloadBucket(bucketName, destDir.toFile());
      Optional<String> configVersion = s3Repository.getBucketVersion(bucketName);
      //Set current config version details
      this.setConfigVersion(configVersion.orElse(DEFAULT_CONFIG_VERSION));
    } catch (Throwable t) {
      log.error("Exception while fetching configuration from S3. (bucketName={})", bucketName);
      throw new SystemException("Could not download s3 bucket.", t);
    }
  }

  /**
   * Deletes base directories if Exists.
   * Synchronize here so that multiple requests don't try to delete the baseDirectory.
   */
  private synchronized void deleteBaseDirIfExists() {
    try {
      if (getBasedir().exists()) {
        log.debug("Deleting base directory. (baseDir={})", getBasedir().getAbsolutePath());
        FileSystemUtils.deleteRecursively(getBasedir().toPath());
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to cleanup base directory", e);
    }
  }

  /**
   * Returns true if new config should be fetched, false otherwise.
   * @return boolean indicating if fresh config should be fetched.
   */
  private boolean shouldFetchConfig() {

    //if base directory doesn't exist, always fetch.
    if (!getBasedir().exists()) {
      return true;
    }

    //always fetch when current config version is set to default or is empty.
    if (StringUtils.isEmpty(configVersion)
        || DEFAULT_CONFIG_VERSION.equals(configVersion)) {
      return true;
    }

    String bucketName = new AmazonS3URI(getUri()).getBucket();
    String s3ConfigVersion = s3Repository.getBucketVersion(bucketName).orElse(
        DEFAULT_CONFIG_VERSION);

    //Either s3 version is not present or is Default OR
    //Current version != s3 version - Only then we should fetch new config from s3.
    if (s3ConfigVersion.equals(DEFAULT_CONFIG_VERSION)
          || !s3ConfigVersion.equals(configVersion)) {
      return true;
    }
    return false;
  }

  /**
   * Copy configuration from srcDir to Base directory to serve.
   * @param srcDir - Source Directory from where the config will be copied.
   * @throws IOException - {@link IOException}
   */
  private void copyConfigurationToBaseDir(Path srcDir) {
    try {
      log.debug("Copying configuration to base directory. (srcDir={}, baseDir={})",
          srcDir.toAbsolutePath(), getBasedir().getAbsolutePath());
      FileSystemUtils.copyRecursively(srcDir.toFile(), getBasedir());
    } catch (IOException e) {
      throw new IllegalStateException("Exception while copying configuration to Base Dir", e);
    }
  }

}
