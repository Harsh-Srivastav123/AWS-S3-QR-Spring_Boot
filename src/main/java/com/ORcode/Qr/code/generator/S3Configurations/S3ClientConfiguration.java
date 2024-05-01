package com.ORcode.Qr.code.generator.S3Configurations;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(S3ClientConfigurationProperties.class)
public class S3ClientConfiguration {


    @Value("${secretKey}")
    String secretKey;

    @Value("${accessKey}")
    String accessKey;

    @Bean
    public S3AsyncClient s3client(S3ClientConfigurationProperties s3props, AwsCredentialsProvider credentialsProvider) {

        SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                .writeTimeout(Duration.ZERO)
                .maxConcurrency(64)
                .build();

        S3Configuration serviceConfiguration = S3Configuration.builder()
                .checksumValidationEnabled(false)
                .chunkedEncodingEnabled(true)
                .build();

        S3AsyncClientBuilder b = S3AsyncClient.builder()
                .httpClient(httpClient)
                .region(s3props.getRegion())
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(serviceConfiguration);

        if (s3props.getEndpoint() != null) {
            b = b.endpointOverride(s3props.getEndpoint());
        }

        return b.build();
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider(S3ClientConfigurationProperties s3props) {

            if (StringUtils.isBlank(s3props.getAccessKeyId())) {
            // Return default provider
            return DefaultCredentialsProvider.create();
        }
        else {
            // Return custom credentials provider
            return () -> {
                AwsCredentials creds = AwsBasicCredentials.create(s3props.getAccessKeyId(), s3props.getSecretAccessKey());
                return creds;
            };
        }
    }


//    @Bean
//    public AwsCredentialsProvider awsCredentialsProvider(S3ClientConfigurationProperties s3props) {
//        if (StringUtils.isBlank(s3props.getAccessKeyId())) {
//            return DefaultCredentialsProvider.create();
//        } else {
//            return () -> {
//                return AwsBasicCredentials.create(
//                        s3props.getAccessKeyId(),
//                        s3props.getSecretAccessKey());
//            };
//        }
//    }

    @Bean
    public AmazonS3 s3(){

        AWSCredentials awsCredentials=new BasicAWSCredentials(accessKey,secretKey);


        return AmazonS3ClientBuilder.standard().withRegion("ap-south-1").withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();

    }


}
