package com.yniot.lms.adaptor.http;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.tio.utils.http.HttpUtils;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

/**
 * @Auther: lane
 * @Date: 2018-12-27 09:11
 * @Description:
 * @Version 1.0.0
 */
public class BaseHttpService {
    private static final String DOMAIN = "https://www.yn-iot.cn/";
    private static final String RESULT_KEY = "result";
    private static final String DATA_KEY = "data";
    Logger logger = Logger.getLogger(BaseHttpService.class);

    public String request(String requestUrl, Map<String, String> paramMap) throws Exception {
        String fullUrl = DOMAIN + requestUrl;
        Map<String, String> header = getHeader();
        Response response = HttpUtils.post(fullUrl, header, paramMap);
        String jsonString = response.body().string();
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        return parseResult(jsonObject);
    }

    public static Map<String, String> getHeader() {
        Map<String, String> header = new HashMap<>();
        return header;
    }

    private String parseResult(JSONObject jsonObject) {
        Boolean result = (Boolean) jsonObject.get(RESULT_KEY);
        if (result) {
            String data = (String) jsonObject.get(DATA_KEY);
            logger.info("data:" + data);
            return data;
        } else {
            return null;
        }
    }
}
