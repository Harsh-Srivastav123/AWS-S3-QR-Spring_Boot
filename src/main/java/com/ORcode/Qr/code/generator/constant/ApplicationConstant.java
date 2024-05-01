package com.ORcode.Qr.code.generator.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ApplicationConstant {
    public static final List<String> ALLOWED_QR_TYPES = Arrays.asList("img","text","video","audio","ppt");
    public static final HashMap<String, String> TYPES_MAPPING =new HashMap<>(){{
        put("text","001");
        put("img","002");
        put("video","003");
        put("audio","004");
        put("ppt","005");
    }};

}
