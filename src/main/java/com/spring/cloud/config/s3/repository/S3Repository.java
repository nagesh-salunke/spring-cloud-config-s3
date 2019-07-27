package com.spring.cloud.config.s3.repository;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.spring.cloud.config.s3.exceptions.SystemException;
import java.io.File;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Repository to download data/metadata of objects with Amazon S3.
 *
 * @author Nagesh Salunke
 */
@Log4j2
@Component
public class S3Repository {

  /**
   * Bean name.
   */
  public static final String BEAN_NAME = "s3FileRepository";

  /**
   * Version metadata key.
   */
  public static final String METADATA_VERSION = "version";

  /**
   * {@link AmazonS3} instance.
   */
  @Resource
  private AmazonS3 s3Client;

  /**
   * {@link TransferManager} instance.
   */
  @Resource
  private TransferManager transferManager;

  @Value("${spring.cloud.config.server.s3.metafile}")
  private String metaFileName;

  /**
   * Downloads a bucket from S3.
   *
   * @param bucketName - bucketName.
   * @param destinationDir - destinationDir where bucket will be downloaded.
   * @throws SystemException - ${@link SystemException}
   */
  public void downloadBucket(String bucketName, File destinationDir)
      throws SystemException {
    try {
      final long startTimeMillis = System.currentTimeMillis();
      MultipleFileDownload download = transferManager.downloadDirectory(bucketName,
          null, destinationDir);
      download.waitForCompletion();
      log.info("Total time taken to download S3 bucket, (bucketName={}, totalTime={} ms)",
          bucketName, (System.currentTimeMillis() - startTimeMillis));
    } catch (InterruptedException e) {
      throw new SystemException("Exception while downloading s3 bucket.", e);
    }
  }

  /**
   * Returns x-amz-meta-version of Bucket - by extracting Metadata from Default file in bucket.
   * https://docs.aws.amazon.com/AmazonS3/latest/user-guide/add-object-metadata.html
   *
   * @param bucketName - bucketName
   * @return - {@link Optional String}
   */
  public Optional<String> getBucketVersion(String bucketName) {
    String version = null;
    try {
      Map<String, String> bucketMetadata = getBucketMetadata(bucketName);
      version = bucketMetadata.get(METADATA_VERSION);
    } catch (Exception e) {
      log.error("Couldn't get bucket version.(bucketName={})", bucketName);
    }
    return Optional.ofNullable(version);
  }

  /**
   * Returns Bucket Metadata - by extracting Metadata from Default file in bucket.
   *
   * @param bucketName - bucketName.
   * @return {@link Map String, String} map of metadata.
   */
  public Map<String, String> getBucketMetadata(String bucketName) {
    log.info("Getting metadata for bucket from S3. (bucketName={})", bucketName);
    final GetObjectMetadataRequest getObjectMetadataRequest =
        new GetObjectMetadataRequest(bucketName, metaFileName);
    ObjectMetadata objectMetadata = s3Client.getObjectMetadata(getObjectMetadataRequest);
    return objectMetadata.getUserMetadata();
  }

}
