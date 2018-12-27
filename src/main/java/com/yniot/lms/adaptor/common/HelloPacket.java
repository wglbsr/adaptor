package com.yniot.lms.adaptor.common;

import org.tio.core.intf.Packet;

/**
 * @Auther: lane
 * @Date: 2018-12-26 13:53
 * @Description:
 * @Version 1.0.0
 */
public class HelloPacket extends Packet {
    private static final long serialVersionUID = -172060606924066412L;
    public static final int HEADER_LENGHT = 4;//消息头的长度
    public static final String CHARSET = "utf-8";
    private byte[] body;
    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

}
