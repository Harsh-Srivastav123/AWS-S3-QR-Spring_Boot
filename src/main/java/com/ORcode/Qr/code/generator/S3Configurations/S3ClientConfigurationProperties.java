package com.ORcode.Qr.code.generator.S3Configurations;






import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

@ConfigurationProperties(prefix = "aws.s3")
@Data
public class S3ClientConfigurationProperties {


    private Region region = Region.AP_SOUTH_1;
    private URI endpoint = null;

    @Value("${secretKey}")
    String secretKey;

    @Value("${accessKey}")
    String accessKey;


    private String accessKeyId=accessKey;
    private String secretAccessKey=secretKey;

    // Bucket name we'll be using as our backend storage
    private String bucket="qrcode-data";

    // AWS S3 requires that file parts must have at least 5MB, except
    // for the last part. This may change for other S3-compatible services, so let't
    // define a configuration property for that
    private int multipartMinPartSize = 5*1024*1024;
}
