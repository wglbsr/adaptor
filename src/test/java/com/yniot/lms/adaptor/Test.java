package com.yniot.lms.adaptor;

import com.yniot.lms.adaptor.http.HttpService;

/**
 * @Auther: lane
 * @Date: 2018-12-26 17:18
 * @Description:
 * @Version 1.0.0
 */
public class Test {

    public static void main(String[] args) throws Exception {
//        Integer test = Integer.valueOf("ff",16);
//        System.out.println(Integer.toString(test,16));

//        LmsPacket lmsPacket = new LmsPacket("4E 42 53 45 08 00 80 92");
//        for(Byte b:lmsPacket.getBody()){
//            System.out.println(b.intValue());
//        }
        HttpService httpService = new HttpService();
        httpService.getEnums(null);

    }
}
