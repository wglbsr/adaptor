package com.yniot.lms.adaptor.http;

import java.util.Map;

/**
 * @Auther: lane
 * @Date: 2018-12-27 09:09
 * @Description: 用于与洗衣店的http服务通讯
 * @Version 1.0.0
 */
public class HttpService extends BaseHttpService {
    public void getEnums(Map<String, String> params) throws Exception {
        super.request("enums/order/state", params);
    }
}
