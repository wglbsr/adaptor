package com.yniot.lms.adaptor;

import com.yniot.lms.adaptor.http.HttpService;
import org.apache.logging.log4j.util.Chars;
import org.springframework.util.StringUtils;
import sun.misc.CharacterDecoder;

import java.io.CharArrayReader;
import java.nio.charset.Charset;

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
//        HttpService httpService = new HttpService();
//        httpService.getEnums(null);
        StringBuilder chars = new StringBuilder();
        byte b1 = 48;
        Byte b = new Byte(b1);
        char c = 'a';
        System.out.println((char)49);

    }
}
