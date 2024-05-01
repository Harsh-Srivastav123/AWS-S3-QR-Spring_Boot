package com.ORcode.Qr.code.generator.service;

import com.ORcode.Qr.code.generator.constant.ApplicationConstant;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class AwsServices {

//    @Autowired
//    AmazonS3 s3;
//    public String saveFile(MultipartFile file,String type) {
//        String originalFilename = UUID.randomUUID().toString()+ ApplicationConstant.TYPES_MAPPING.get(type);
//        int count = 0;
//        int maxTries = 3;
//        while(true) {
//            try {
//                File file1 = convertMultiPartToFile(file);
//                PutObjectResult putObjectResult = s3.putObject("qr-code", originalFilename, file1);
//                return putObjectResult.getContentMd5();
//            } catch (IOException e) {
//                if (++count == maxTries) throw new RuntimeException(e);
//            }
//        }
//
//    }
//
//    private File convertMultiPartToFile(MultipartFile file ) throws IOException
//    {
//        File convFile = new File( file.getOriginalFilename() );
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream( convFile );
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            fos.write( file.getBytes() );
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        fos.close();
//        return convFile;
//    }
}
