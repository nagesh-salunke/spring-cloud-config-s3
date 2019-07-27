package com.spring.cloud.config.s3.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AWS Config.
 *
 * @author Nagesh Salunke
 */
@Configuration
public class AwsConfig {

  /**
   * AWS region.
   */
  @Value("${aws.region}")
  private String region;

  /**
   * AWS access key.
   */
  @Value("${aws.accessKey}")
  private String accessKey;

  /**
   * AWS secret key.
   */
  @Value("${aws.secretKey}")
  private String secretKey;


  /**
   * Creates {@link AWSStaticCredentialsProvider} bean.
   *
   * @return {@link AWSStaticCredentialsProvider}
   */
  @Bean
  public AWSStaticCredentialsProvider awsCredentials() {
    return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
  }

  /**
   * Creates {@link AmazonS3} bean.
   *
   * @return {@link AmazonS3}
   */
  @Bean
  public AmazonS3 s3Client(AWSCredentialsProvider awsCredentialsProvider) {
    return AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider)
        .withRegion(region)
        .build();
  }

  /**
   * Creates {@link TransferManager} bean.
   * @return {@link TransferManager}
   */
  @Bean
  public TransferManager transferManager(AmazonS3 amazonS3) {
    return TransferManagerBuilder.standard().withS3Client(amazonS3).build();
  }

}
