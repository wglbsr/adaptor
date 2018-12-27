package com.yniot.lms.adaptor.entity;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
import org.tio.core.intf.Packet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: lane
 * @Date: 2018-12-26 13:53
 * @Description:
 * @Version 1.0.0
 * <p>
 * 注意通道号port由0x01开始
 */
public class LmsPacket extends Packet {
    private static org.apache.log4j.Logger logger = Logger.getLogger(LmsPacket.class);

    public static String SEPARATOR = " ";
    public static int RADIX = 16;
    public static Integer LENGTH_MIN = 8;
    private static List<Integer> header = new ArrayList<>();
    public static final String CHARSET = "utf-8";
    public static final Integer LENGTH_INDEX = 4;
    public static final Integer HEADER_LENGTH = 4;
    public static final Integer ADDRESS_INDEX = 5;
    public static final Integer COMMAND_INDEX = 6;
    public static final Integer DATA_START_INDEX = 7;
    public static final Integer HEARTBEAT_RES_DATA = 0x00;
    //*控制部分*/
    //0x80 心跳包
    public static final Integer HEARTBEAT = 0x80;
    //0x81 获取设备 ID
    public static final Integer DEVICE_ID = 0x81;
    //0x82 开锁
    public static final Integer OPEN = 0x82;
    //0x83 读门状态
    public static final Integer DOOR_STATE = 0x83;
    //0x84 查询所有状态
    public static final Integer ALL_STATE = 0x84;
    //0x85 主动上传门状态变化
    public static final Integer UPLOAD_STATE = 0x85;
    //0x86 开全部锁
    public static final Integer OPEN_ALL = 0x86;


    /*设置部分*/
    //0x91 设置  更改配置的密码 用于过滤非法更改配置的请求
    public static final Integer CHANGE_PASSWORD = 0x91;
    // 0x92 设置 TCP服务器地址及端口号
    public static final Integer CHANGE_PORT = 0x92;


    static {
        //默认前缀
        header.add(0x4e);
        header.add(0x42);
        header.add(0x53);
        header.add(0x45);
    }

    // 状态字节 0x00 表示执行正确，其它数 值表示执行错误。
    // 起始符:四字节，默认为“NBSE”，用户可通过配置工具修改
    // 校验字节:从帧起始符到数据域最后一个字节逐字节的异或(XOR)值
    // 帧长度:一个字节，为从起始到校验字节的字节数，取值范围为 0x08 ~ 0xFF。
    // 注意数据域长度可以为0
    private byte[] body;

    public LmsPacket(Integer address, Integer cmd, Integer... data) {
        this.address = address;
        this.command = cmd;
        List<Integer> tempData = new ArrayList<>();
        for (Integer d : data) {
            tempData.add(d);
        }
        this.data = tempData;
//        this.getBody();
    }

    public LmsPacket(Integer address, Integer cmd, List<Integer> data) {
        this.address = address;
        this.command = cmd;
        if (data != null && !data.isEmpty()) {
            this.data = data;
        }
//        this.getBody();
    }


    public LmsPacket(Integer address, Integer cmd) {
        this.address = address;
        this.command = cmd;
//        this.getBody();
    }

    //默认为心跳包
    public LmsPacket(Integer address) {
        this.address = address;
        this.command = HEARTBEAT;
    }


    public static LmsPacket parse(byte[] header, byte length, byte address, byte cmd, byte[] data, byte check) throws UnsupportedEncodingException {
        LmsPacket lmsPacket = null;
        if (((Byte) length).intValue() != (LENGTH_MIN + data.length) || !headerMatch(header)) {
            logger.error("长度或头部不匹配!");
            return lmsPacket;
        } else {
            List<Integer> fullMessage = new ArrayList<>();
            //头部4byte
            for (Byte b : header) {
                fullMessage.add(b.intValue());
            }
            //长度 1byte
            fullMessage.add(((Byte) length).intValue());
            //地址 1byte
            fullMessage.add(((Byte) address).intValue());
            //命令 1byte
            fullMessage.add(((Byte) cmd).intValue());
            //数据位 n byte
            for (Byte b : data) {
                fullMessage.add(b.intValue());
            }
            //校验位
            fullMessage.add(((Byte) check).intValue());
            if (checkEveryByte(fullMessage) && headerMatch(fullMessage) && checkXOR(fullMessage)) {
                List<Integer> tempData = null;
                if (fullMessage.size() > LENGTH_MIN) {
                    tempData = fullMessage.subList(DATA_START_INDEX, fullMessage.size() - 1);
                }
                lmsPacket = new LmsPacket(fullMessage.get(ADDRESS_INDEX), fullMessage.get(COMMAND_INDEX), tempData);
            }
        }
        return lmsPacket;
    }


//    //长度校验
//    private boolean checkLength(List<Integer> message) {
//        if (message.get(LENGTH_INDEX) == message.size()) {
//            this.length = message.get(LENGTH_INDEX);
//            return true;
//        }
//        logger.info("长度不匹配!");
//        return false;
//    }


    //是否超过255  即是否大于1字节
    private static boolean checkEveryByte(List<Integer> message) {
        for (Integer integer : message) {
            if (integer.intValue() > 255) {
                logger.info("帧过长!");
                return false;
            }
        }
        return true;
    }

    //前缀是否符合,意义不大
    static boolean headerMatch(List<Integer> message) {
        for (int i = 0; i < HEADER_LENGTH; i++) {
            if (header.get(i) != message.get(i)) {
                logger.info("头部校验不通过!");
                return false;
            }
        }
        return true;
    }

    static boolean headerMatch(byte[] message) {
        for (int i = 0; i < HEADER_LENGTH; i++) {
            if (header.get(i) != ((Byte) message[i]).intValue()) {
                logger.info("头部校验不通过!");
                return false;
            }
        }
        return true;
    }

    //校验码检查
    static boolean checkXOR(List<Integer> message) {
        Integer check = message.get(message.size() - 1);
        List<Integer> beforeCheck = message.subList(0, message.size() - 1);
        Integer calculateCheck = getCheckXOR(beforeCheck);
        return calculateCheck.intValue() == check.intValue();
    }

    public byte[] getBody() throws UnsupportedEncodingException {
        byte[] fullPack = this.getFullPack();
        int bodySize = this.getFullPack().length - HEADER_LENGTH;
        byte[] body = new byte[bodySize];
        for (int i = HEADER_LENGTH; i < fullPack.length; i++) {
            body[i - HEADER_LENGTH] = fullPack[i];
        }
        this.body = body;
        return body;
    }

    public byte[] getFullPack() {
        if (this.data != null) {
            this.length = LENGTH_MIN + this.data.size();
        } else {
            this.length = LENGTH_MIN;
        }
        List<Integer> beforeCheck = new ArrayList<>();
        byte[] resultByte = new byte[LENGTH_MIN + (this.data == null || this.data.isEmpty() ? 0 : this.data.size())];
        int index = 0;
        for (Integer b : header) {
            resultByte[index] = b.byteValue();
            index++;
        }
        beforeCheck.addAll(header);
        resultByte[index] = this.length.byteValue();
        index++;
        beforeCheck.add(this.length);
        resultByte[index] = this.address.byteValue();
        index++;
        beforeCheck.add(this.address);
        resultByte[index] = this.command.byteValue();
        index++;
        beforeCheck.add(this.command);
        if (this.data != null && this.data.size() > 0) {
            for (Integer b : this.data) {
                resultByte[index] = b.byteValue();
                index++;
            }
            beforeCheck.addAll(this.data);
        }
        this.check = getCheckXOR(beforeCheck);
        resultByte[index] = this.check.byteValue();
        return resultByte;
    }

    private String fromIntList(List<Integer> integerList) {
        String result = "";
        for (Integer integer : integerList) {
            //添加空格
            result += autoAppendZero(integer);
//            result += SEPARATOR + autoAppendZero(integer);
        }
        return result;
    }


    private String autoAppendZero(Integer value) {
        String newVal = Integer.toHexString(value.intValue());
        if (newVal.length() == 1) {
            return "0" + newVal;
        } else {
            return newVal;
        }
    }

    //接收消息
    private List<Integer> received(String message) {
        List<Integer> result = new ArrayList<>();
        if (!StringUtils.isEmpty(message)) {
            String[] messageList = message.split(SEPARATOR);
            for (String temp : messageList) {
                result.add(strToHexInt(temp));
            }
        }
        //长度小于LENGTH_MIN则为无效数据
        if (result.size() < LENGTH_MIN) {
            return null;
        }
        return result;
    }


    //0x4E 0x42 0x53 0x45
    private static Integer getCheckXOR(List<Integer> beforeCheck) {
        int firstByte = beforeCheck.get(0);
        for (int i = 1; i < beforeCheck.size(); i++) {
            firstByte = firstByte ^ beforeCheck.get(i);
            System.out.println("index:[" + i + "]," + firstByte);
        }
        return firstByte;
    }

    /**
     * @return int
     * @Author wanggl(lane)
     * @Description //TODO 字符转16进制
     * @Date 17:14 2018-12-26
     * @Param [data]
     **/
    private Integer strToHexInt(String data) {
        return Integer.valueOf(data, RADIX);
    }

    private Integer length;

    public List<Integer> getHeader() {
        return header;
    }

    public int getLength() {
        return length;
    }

    public int getAddress() {
        return address;
    }

    public int getCommand() {
        return command;
    }

    public List<Integer> getData() {
        return data;
    }


    public int getCheck() {
        return check;
    }

    private Integer address;
    private Integer command;
    private List<Integer> data;
    private Integer check;

    public boolean isHeartbeat() {
        if (this.command.intValue() == HEARTBEAT.intValue()) {
            return true;
        }
        return false;
    }
}