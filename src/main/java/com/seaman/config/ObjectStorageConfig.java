package com.seaman.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectStorageConfig {

    @Value("${object.store.key}")
    private String s3key;

    @Value("${object.store.secret}")
    private String s3Secrets;

    @Value("${object.store.endpoint}")
    private String s3endpoint;

    @Value("${object.store.region}")
    private String s3region;

    @Bean
    public AmazonS3 getS3() {
        BasicAWSCredentials creds = new BasicAWSCredentials(s3key, s3Secrets);
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(s3endpoint, s3region))
                .withCredentials(new AWSStaticCredentialsProvider(creds)).build();
    }

}
