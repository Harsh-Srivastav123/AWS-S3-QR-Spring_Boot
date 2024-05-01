package com.ORcode.Qr.code.generator.controller;


import com.ORcode.Qr.code.generator.S3Configurations.S3ClientConfigurationProperties;
import com.ORcode.Qr.code.generator.service.QRServices;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@RestController
@Slf4j
public class HomeController {
    @Autowired
    S3AsyncClient s3client;

    @Autowired
    S3ClientConfigurationProperties s3config;

    @Autowired
    QRServices qrServices;

    @Autowired
    AmazonS3 amazonS3;

    @Autowired
    ResourceLoader resourceLoader;
//
//    @Autowired
//    S3Client s3Client;

//    public UploadResource(S3AsyncClient s3client, S3ClientConfigurationProperties s3config) {
//        this.s3client = s3client;
//        this.s3config = s3config;
//    }

    @PostMapping("/upload")
    public Mono<ResponseEntity<com.ORcode.Qr.code.generator.model.UploadResult>> uploadHandler(
            @RequestHeader HttpHeaders headers,
            @RequestBody Flux<ByteBuffer> body) {
        // ... see section 6

        long length = headers.getContentLength();

        String fileKey = UUID.randomUUID().toString();
        MediaType mediaType = headers.getContentType();
        Map<String, String> metadata = new HashMap<String, String>();

        if (mediaType == null) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        CompletableFuture future = s3client
                .putObject(PutObjectRequest.builder()
                                .bucket(s3config.getBucket())
                                .contentLength(length)
                                .key(fileKey.toString())
                                .contentType(mediaType.toString())
                                .metadata(metadata)
                                .build(),
                        AsyncRequestBody.fromPublisher(body));

        return Mono.fromFuture(future)
                .map((response) -> {
//                    checkResult(response);
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(new com.ORcode.Qr.code.generator.model.UploadResult(HttpStatus.CREATED, Arrays.asList(fileKey)));
                });

//        return Mono.fromFuture(future).map((response)->{
//            checkResult(response);
//            return
//        })

    }

    @RequestMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            method = {RequestMethod.POST, RequestMethod.PUT})
    public Mono<ResponseEntity<UploadResult>> multipartUploadHandler(
            @RequestHeader HttpHeaders headers,
            @RequestBody Flux<Part> parts) {
        // ... see section 7
        return null;
    }

//    private static void checkResult(Sdk result) {
//        if (result.sdkHttpResponse() == null || !result.sdkHttpResponse().isSuccessful()) {
////            throw new UploadFailedException(result);
//            log.info("exception in upload");
//        }
//    }


//Download


    @GetMapping(path = "download/{filekey}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
    Mono<ResponseEntity<Flux<ByteBuffer>>> downloadFile(@PathVariable("filekey") String filekey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3config.getBucket())
                .key(filekey)
                .build();

        return Mono.fromFuture(s3client.getObject(request, AsyncResponseTransformer.toPublisher()))
                .map(response -> {
//                checkResult(response.response());
                    String filename = getMetadataItem(response.response(), "filename", filekey);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            .header(HttpHeaders.CONTENT_LENGTH, Long.toString(response.response().contentLength()))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                            .body(Flux.from(response));
                });
    }


    private String getMetadataItem(GetObjectResponse sdkResponse, String key, String defaultValue) {
        for (Map.Entry<String, String> entry : sdkResponse.metadata()
                .entrySet()) {
            if (entry.getKey()
                    .equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return defaultValue;
    }


//    @GetMapping("/video/{key}")
//    public Mono<ResponseEntity<byte[]>> streamVideo(@PathVariable String key) {
//        return qrServices.downloadVideo(key)
//                .map(responseBytes -> ResponseEntity.ok()
//                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                        .body(responseBytes.asByteArray()));
//    }




    @GetMapping(value = "download/video/{title}", produces = "video/mp4")
    public Mono<Resource> getVideo(
            @PathVariable String title,
            @RequestHeader(value = "Range", required = false) String range) {
        return Mono.fromSupplier(
                () -> {
                    return resourceLoader.getResource(title);
                });
    }



//    GetObjectRequest rangeObjectRequest = new GetObjectRequest(bucketName, key);
//rangeObjectRequest.setRange(0, 1000); // retrieve 1st 1000 bytes.
//    S3Object objectPortion = s3Client.getObject(rangeObjectRequest);
//    InputStream objectData = objectPortion.getObjectContent();




//    @GetMapping(value = "/downloadfile/{key}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
//    public ResponseEntity<StreamingResponseBody> downloadFile(HttpServletRequest request,@PathVariable  String key) {
//        //reads the content from S3 bucket and returns a S3ObjectInputStream
//        S3Object object = amazonS3.getObject("qrcode-data", key);
//        S3ObjectInputStream finalObject = object.getObjectContent();
//
//        final StreamingResponseBody body = outputStream -> {
//            int numberOfBytesToWrite = 0;
//            byte[] data = new byte[1024];
//            while ((numberOfBytesToWrite = finalObject.read(data, 0, data.length)) != -1) {
//                System.out.println("Writing some bytes..");
//                outputStream.write(data, 0, numberOfBytesToWrite);
//            }
//            finalObject.close();
//        };
//        return new ResponseEntity<>(body, HttpStatus.OK);
//    }

    @GetMapping(value = "/downloadfile/{key}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity<StreamingResponseBody>  downloadFile(HttpServletRequest request, @PathVariable  String key,
                                       @RequestHeader(value = "Range", required = false) String range ) {
//        reads the content from S3 bucket and returns a S3ObjectInputStream
        S3Object object = amazonS3.getObject("qrcode-data", key);
//        S3ObjectInputStream finalObject = object.getObjectContent();
        log.info("range - "+request.getHeader("Range"));
//
////        GetObjectRequest rangeObjectRequest = GetObjectRequest.builder().bucket("qrcode-data").key(key).range("1024").build();
////        // retrieve 1st 1000 bytes.
////        ResponseInputStream<GetObjectResponse> objectPortion = s3Client.getObject(rangeObjectRequest);
////        return objectPortion;
//
//
//        final StreamingResponseBody body = outputStream -> {
//            int numberOfBytesToWrite = 0;
//            byte[] data = new byte[1024];
//            while ((numberOfBytesToWrite = finalObject.read(data, 0, data.length)) != -1) {
////                System.out.println("Writing some bytes..");
//                outputStream.write(data, 0, numberOfBytesToWrite);
//            }
//            finalObject.close();
//        };
//        return  new ResponseEntity<>(body, HttpStatus.OK);


        S3ObjectInputStream finalObject = object.getObjectContent();

        final StreamingResponseBody body = outputStream -> {
            int numberOfBytesToWrite = 0;
            byte[] data = new byte[1024];
            while ((numberOfBytesToWrite = finalObject.read(data, 0, data.length)) != -1) {
    //    System.out.println("Writing some bytes..");
                outputStream.write(data, 0, numberOfBytesToWrite);
            }
            finalObject.close();
        };
        return new ResponseEntity<>(body, HttpStatus.OK);

    }


}
