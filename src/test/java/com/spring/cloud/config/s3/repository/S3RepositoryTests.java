package com.spring.cloud.config.s3.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.spring.cloud.config.s3.AbstractTest;
import com.spring.cloud.config.s3.exceptions.SystemException;
import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Tests for {@link S3Repository}
 *
 * @author Nagesh Salunke
 */
public class S3RepositoryTests extends AbstractTest {

  /**
   * Mock s3Client.
   */
  @MockBean
  private AmazonS3 s3Client;

  /**
   * Mock transferManager.
   */
  @MockBean
  private TransferManager transferManager;

  /**
   * S3Repository under test.
   */
  @Resource
  private S3Repository s3Repository;

  @Before
  @Override
  public void setUp() throws Exception {
  }

  @Test
  public void downloadBucket_validBucketDetails_noException()
      throws InterruptedException, SystemException {
    MultipleFileDownload download = Mockito.mock(MultipleFileDownload.class);
    Mockito.doNothing().when(download).waitForCompletion();
    when(transferManager.downloadDirectory(anyString(), any(), any())).thenReturn(download);
    s3Repository.downloadBucket("testBucket", new File("target/repos/config-repo"));
  }

  @Test(expected = SystemException.class)
  public void downloadBucket_validBucketDetails_downloadException()
      throws InterruptedException, SystemException {
    MultipleFileDownload download = Mockito.mock(MultipleFileDownload.class);
    Mockito.doAnswer(k -> {
      throw new InterruptedException("Exception while downloading bucket.");
    }).when(download).waitForCompletion();
    when(transferManager.downloadDirectory(anyString(), any(), any())).thenReturn(download);
    s3Repository.downloadBucket("testBucket", new File("target/repos/config-repo"));
  }

  @Test
  public void getBucketMetadata_withValidDetails_validMetadata() {
    Map<String, String> testMetadata = new TreeMap<String, String>();
    testMetadata.put("version", "v1");
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setUserMetadata(testMetadata);
    when(s3Client.getObjectMetadata(any())).thenReturn(objectMetadata);
    Map<String, String> actualMetadata = s3Repository.getBucketMetadata("testBucket");
    Assert.assertEquals(testMetadata, actualMetadata);
  }

  @Test
  public void getBucketVersion_withValidDetails_validVersion() {
    Map<String, String> testMetadata = new TreeMap<>();
    testMetadata.put(S3Repository.METADATA_VERSION, "v1");
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setUserMetadata(testMetadata);
    when(s3Client.getObjectMetadata(any())).thenReturn(objectMetadata);
    Optional<String> actualVersion = s3Repository.getBucketVersion("testBucket");
    Assert.assertTrue(actualVersion.isPresent());
    Assert.assertEquals("v1", actualVersion.get());
  }


  @Test
  public void getBucketVersion_withExceptionFromS3_emptyVersion() {
    Map<String, String> testMetadata = new TreeMap<>();
    testMetadata.put(S3Repository.METADATA_VERSION, "v1");
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setUserMetadata(testMetadata);
    when(s3Client.getObjectMetadata(any())).thenThrow(new AmazonS3Exception("Something went wrong"));
    Optional<String> actualVersion = s3Repository.getBucketVersion("testBucket");
    Assert.assertFalse(actualVersion.isPresent());
  }

  @Test
  public void getBucketVersion_noMetadataCase_emptyVersion() {
    Map<String, String> testMetadata = new TreeMap<>();
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setUserMetadata(testMetadata);
    when(s3Client.getObjectMetadata(any())).thenReturn(objectMetadata);
    Optional<String> actualVersion = s3Repository.getBucketVersion("testBucket");
    Assert.assertFalse(actualVersion.isPresent());
  }
}
