package com.lawnroad.common.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NCPStorageConfig {
  
  @Value("${ncp.storage.region}")
  private String region;
  
  @Value("${ncp.storage.endpoint}")
  private String endpoint;
  
  @Value("${ncp.storage.accessKey}")
  private String accessKey;
  
  @Value("${ncp.storage.secretKey}")
  private String secretKey;
  
  @Bean
  public AmazonS3 amazonS3() {
    return AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
        .enablePathStyleAccess()  // 중요: NCP는 path-style로 작동해야 함
        .build();
  }
}