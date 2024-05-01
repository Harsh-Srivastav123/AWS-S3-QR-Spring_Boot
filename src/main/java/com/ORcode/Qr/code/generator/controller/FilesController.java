package com.ORcode.Qr.code.generator.controller;

import com.ORcode.Qr.code.generator.constant.ApplicationConstant;
import com.ORcode.Qr.code.generator.service.AwsServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FilesController {

    @Autowired
    AwsServices awsServices;

//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadFile(
//            @RequestParam(value = "text",required = false) String message,
//            @RequestParam(value = "files",required = true) MultipartFile file){
//        String type=file.getContentType();
//        if(ApplicationConstant.ALLOWED_QR_TYPES.contains(type)){
//            awsServices.uploadFile(type);
//        }
//    }


}
