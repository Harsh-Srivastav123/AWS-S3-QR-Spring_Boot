package com.ORcode.Qr.code.generator.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.UUID;


@Service
public class AwsS3UploadServices {
    private static final Logger log = LoggerFactory.getLogger(AwsS3UploadServices.class);

    @Value("${secretKey}")
    String secretKey;

    @Value("${accessKey}")
    String accessKey;
    private static String bucketName="qrcode-data";


    public String uploadFiles(BufferedImage image,  String key){

        // Convert BufferedImage to PNG format
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey,secretKey);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion("ap-south-1")
                .build();

//        System.out.println("Image uploaded to S3 successfully.");

        try{
            // Upload the image to S3
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(os.size());
            s3Client.putObject(new PutObjectRequest(bucketName, key, is, metadata));
            // Get the URL of the uploaded object
            URL url = s3Client.getUrl(bucketName, key);
            return url.toString();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public  String uploadMediaFilesToS3(List<MultipartFile> files) {
        StringBuilder urls = new StringBuilder();
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey,secretKey);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion("ap-south-1")
                .build();
        int count=1;

        for (MultipartFile file : files) {
            String key = file.getName()+ UUID.randomUUID(); // Use the file name as the S3 key
            ObjectMetadata metadata = new ObjectMetadata();
            File newFile=null;
            try {
                newFile=convert(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            metadata.setContentLength(newFile.length());
            s3Client.putObject(new PutObjectRequest(bucketName, key,newFile ).withMetadata(metadata));
            urls.append("File ").append(count).append(" :- ");
            count++;
            // Append the S3 URL to the StringBuilder
            String url = s3Client.getUrl(bucketName, key).toString();
            urls.append(url).append("   ,   ");
        }

        // Remove the last comma
        if (urls.length() > 0) {
            urls.setLength(urls.length() - 1);
        }

        return urls.toString();
    }

    public static File convert(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }
        return file;
    }

    public S3ObjectInputStream getContent(String key){

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion("ap-south-1")
                .build();

        S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, key));
        S3ObjectInputStream objectContent = object.getObjectContent();
        log.info(objectContent.toString());
        return objectContent;
    }
}
