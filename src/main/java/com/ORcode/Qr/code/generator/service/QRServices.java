package com.ORcode.Qr.code.generator.service;

import com.ORcode.Qr.code.generator.constant.ApplicationConstant;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service

public class QRServices {

    private static final Logger log = LoggerFactory.getLogger(QRServices.class);
    @Autowired
    AwsS3UploadServices awsS3UploadServices;

//    @Autowired
//    S3Client s3Client;


    private static final String charset = "UTF-8";

    @Value("${qrcode.output.directory}")
    private String outputLocation;

    private static final String strDateFormat = "yyyyMMddhhmmss";


    public String makeQr(String message,List<MultipartFile> fileList){
        log.info("### Generating QRCode ###");

        if(fileList!=null){
            String fileUrls=awsS3UploadServices.uploadMediaFilesToS3(fileList);
            message+="               "+fileUrls;
        }

        log.info(message);
        log.info("Output directory - {}", outputLocation);
        try {
            String fileName=prepareOutputFileName();
            log.info("Final Input Message - {}", message);
            BufferedImage bufferedImage=processQRcode(message, fileName, charset, 400, 400);
            fileName=fileName.substring(8,fileName.length());
            return awsS3UploadServices.uploadFiles(bufferedImage,fileName);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private String prepareOutputFileName() {
        Date date = new Date();

        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        String formattedDate= dateFormat.format(date);

        StringBuilder sb = new StringBuilder();
        sb.append(outputLocation).append("\\").append("QRCode-").append(formattedDate).append(".png");
        log.info("Final output file - "+sb.toString());
        return sb.toString();
    }



    private BufferedImage processQRcode(String data, String path, String charset, int height, int width) throws WriterException, IOException, IOException {
        /*the BitMatrix class represents the 2D matrix of bits*/
       /* MultiFormatWriter is a factory class that finds the appropriate Writer subclass for
        the BarcodeFormat requested and encodes the barcode with the supplied contents.*/
        BitMatrix matrix = null;
        try {
            matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, width, height);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        MatrixToImageWriter.writeToFile(matrix, path.substring(path.lastIndexOf('.') + 1), new File(path));
        return MatrixToImageWriter.toBufferedImage(matrix);
    }


    public String createQr(String type, String qrReqBody) {

        String fileName=prepareOutputFileName();
        BufferedImage bufferedImage=null;

        try{
           bufferedImage=processQRcode(qrReqBody, fileName, charset, 400, 400);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        StringBuilder key=new StringBuilder();
        key.append(fileNameForUpload(type)).append(".png");
        log.info(key.toString());
        awsS3UploadServices.uploadFiles(bufferedImage,key.toString());
        return key.substring(0,key.length()-4);

    }

    private String fileNameForUpload(String type){
        log.info(ApplicationConstant.TYPES_MAPPING.get(type).toString());
        return UUID.randomUUID().toString()+ ApplicationConstant.TYPES_MAPPING.get(type);
    }

    public byte [] getContent(String key){
        key+=".png";
        S3ObjectInputStream objectContent=awsS3UploadServices.getContent(key);
        byte[] imageBytes=null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = objectContent.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            imageBytes = outputStream.toByteArray();

            outputStream.close();
            objectContent.close();

            System.out.println("Image converted to byte array successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error converting image to byte array: " + e.getMessage());
        }
        return imageBytes;
    }

//    public Mono downloadVideo(String key) {
//
//        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
//                .bucket("ap-south-1")
//                .key(key)
//                .build();
//
//        return Mono.fromFuture(() -> s3Client.getObjectAsBytes(getObjectRequest));
//    }
}



