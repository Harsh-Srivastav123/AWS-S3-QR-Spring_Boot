package com.ORcode.Qr.code.generator.controller;

import com.ORcode.Qr.code.generator.constant.ApplicationConstant;
import com.ORcode.Qr.code.generator.dto.QRResponseDTO;
import com.ORcode.Qr.code.generator.service.QRServices;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/qr")
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    @Autowired
    QRServices qrServices;

    @PostMapping("/makeQr")
    public ResponseEntity<String> makeQr(
            @RequestParam(value = "message") String message,
            @RequestParam(value = "files",required = false)List<MultipartFile> fileList

            ){
        return new ResponseEntity<>(qrServices.makeQr(message,fileList), HttpStatus.OK);
    }

    //TODO :: add valdiation @valid
    @PostMapping("/generate")
    public ResponseEntity<String> generateQR(@RequestBody String qrReqBody){
        JsonObject jsonObject = JsonParser.parseString(qrReqBody).getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        log.info(type);
        if(type==null){
            return new ResponseEntity<>("type is not found in request",HttpStatus.BAD_REQUEST);
        }
        if(ApplicationConstant.ALLOWED_QR_TYPES.contains(type))
        {
            return new ResponseEntity<>(qrServices.createQr(type,jsonObject.toString()),HttpStatus.OK);
        }
        return new ResponseEntity<>("type missmatch",HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/getQr/{key}")
    public ResponseEntity<byte []> getQR(@PathVariable String key){


        byte[] image=qrServices.getContent(key);

        // Set the appropriate headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return ResponseEntity.ok().headers(headers).body(image);
    }

}
