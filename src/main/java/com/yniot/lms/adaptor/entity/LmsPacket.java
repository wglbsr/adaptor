package com.yniot.lms.adaptor.entity;

import org.apache.log4j.Logger;
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
    private static List<Integer> header = new ArrayList<>();
    //下标长度等常量
    public static int LENGTH_MIN = 8;
    public static final String IP_PORT_SEPARATOR = ":";
    public static final int IP_PORT_SUFFIX = 0x00;

    public static final int LENGTH_INDEX = 4;
    public static final int HEADER_LENGTH = 4;
    public static final int ADDRESS_INDEX = 5;
    public static final int COMMAND_INDEX = 6;
    public static final int DATA_START_INDEX = 7;
    public static final int STATE_INDEX = 0;
    public static final int ID_LENGTH = 8;
    public static final int HEARTBEAT_RES_DATA = 0x00;

    //状态常量
    public static final int RES_HEARTBEAT_DATA = 0x00;
    public static final int RES_STATE_OK = 0x00;
    public static final int RES_DOOR_CLOSED = 0x01;
    public static final int RES_DOOR_OPENED = 0x00;

    //*控制部分*/
    //0x80 心跳包
    public static final Integer CMD_HEARTBEAT = 0x80;
    //0x81 获取设备 ID
    public static final Integer CMD_DEVICE_ID = 0x81;
    //0x82 开锁
    public static final Integer CMD_OPEN = 0x82;
    //0x83 读门状态
    public static final Integer CMD_DOOR_STATE = 0x83;
    //0x84 查询所有状态
    public static final Integer CMD_ALL_STATE = 0x84;
    //0x85 主动上传门状态变化
    public static final Integer CMD_AUTO_UPLOAD = 0x85;
    //0x86 开全部锁
    public static final Integer CMD_OPEN_ALL = 0x86;


    /*设置部分*/
    //0x91 设置  更改配置的密码 用于过滤非法更改配置的请求
    public static final Integer CMD_CHANGE_PASSWORD = 0x91;
    // 0x92 设置 TCP服务器地址及端口号
    public static final Integer CMD_CHANGE_PORT = 0x92;


    static {
        //默认前缀
        // 起始符:四字节，默认为“NBSE”，用户可通过配置工具修改
        header.add(0x4e);
        header.add(0x42);
        header.add(0x53);
        header.add(0x45);
    }

    // 状态字节 0x00 表示执行正确，其它数 值表示执行错误。
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
    }

    public LmsPacket(Integer address, Integer cmd, List<Integer> data) {
        this.address = address;
        this.command = cmd;
        if (data != null && !data.isEmpty()) {
            this.data = data;
        }
    }


    public LmsPacket(Integer address, Integer cmd) {
        this.address = address;
        this.command = cmd;
    }

    //默认为心跳包
    public LmsPacket(Integer address) {
        this.address = address;
        this.command = CMD_HEARTBEAT;
    }

    public boolean isOK() {
        if (this.data != null && !this.data.isEmpty()) {
            return this.data.get(STATE_INDEX) == RES_STATE_OK;
        }
        return false;
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

    public byte[] getBody() {
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
            resultByte[index++] = b.byteValue();
        }
        beforeCheck.addAll(header);
        resultByte[index++] = this.length.byteValue();
        beforeCheck.add(this.length);
        resultByte[index++] = this.address.byteValue();
        beforeCheck.add(this.address);
        resultByte[index++] = this.command.byteValue();
        beforeCheck.add(this.command);
        if (this.data != null && this.data.size() > 0) {
            for (Integer b : this.data) {
                resultByte[index++] = b.byteValue();
            }
            beforeCheck.addAll(this.data);
        }
        this.check = getCheckXOR(beforeCheck);
        resultByte[index] = this.check.byteValue();
        return resultByte;
    }


    //0x4E 0x42 0x53 0x45
    private static Integer getCheckXOR(List<Integer> beforeCheck) {
        int firstByte = beforeCheck.get(0);
        for (int i = 1; i < beforeCheck.size(); i++) {
            firstByte = firstByte ^ beforeCheck.get(i);
        }
        return firstByte;
    }

    /**
     * 1111111111111111111111
     * 服务端进行回复
     *
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取服务端对心跳包的回复包
     * @Date 17:13 2018-12-27
     * @Param [address]
     **/
    public static byte[] getHeartbeatRes(int address) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.CMD_HEARTBEAT, HEARTBEAT_RES_DATA);
        return lmsPacket.getBody();
    }


    /**
     * 22222222222222222222
     *
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[获取设备ID]的命令  有回复
     * @Date 17:06 2018-12-27
     * @Param [address, port]
     **/
    public static byte[] getDeviceIdCmd(int address) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.CMD_DEVICE_ID);
        return lmsPacket.getFullPack();
    }

    /**
     * 22222222222222222222
     *
     * @return java.lang.String
     * @Author wanggl(lane)
     * @Description //TODO [获取设备ID]的命令 的回复
     * //设备 ID 号:8 个字节，可通过配置软件对设备端进行配置。
     * @Date 10:38 2018-12-28
     * @Param [address, lmsPacket]
     **/
    public String getDeviceIdCmdRes(int address) {
        List<Integer> data = this.getData();
        if (data != null && isMatch(address, LmsPacket.CMD_DEVICE_ID) && data.size() == LmsPacket.ID_LENGTH) {
            StringBuffer buffer = new StringBuffer();
            for (int val : data) {
                buffer.append((char) val);
            }
            return buffer.toString();
        } else {
            return null;
        }
    }

    //22222222222222222222
    public byte[] parseIdToBytes(String id) {
        if (id.length() != LmsPacket.ID_LENGTH) {
            return null;
        }
        byte[] idBytes = id.getBytes();
        return idBytes;
    }

    //22222222222222222222
    public List<Integer> parseIdToInt(String id) {
        if (id.length() != LmsPacket.ID_LENGTH) {
            return null;
        }
        List<Integer> idList = new ArrayList<>();
        byte[] idBytes = id.getBytes();
        for (Byte b : idBytes) {
            idList.add(b.intValue());
        }
        return idList;
    }


    /**
     * 333333333333333333
     *
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[开单个锁]的命令  有回复
     * @Date 16:50 2018-12-27
     * @Param [address, portNum]
     **/
    public static byte[] getOpenCmd(int address, int portNum) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.CMD_OPEN, portNum);
        return lmsPacket.getFullPack();
    }

    /**
     * 333333333333333333
     *
     * @return int
     * @Author wanggl(lane)
     * @Description //TODO [开单个锁]指令的回复
     * @Date 10:34 2018-12-28
     * @Param [address, portNum, lmsPacket]
     **/
    public int getOpenCmdRes(int address, int portNum, LmsPacket lmsPacket) {
        if (isMatch(address, LmsPacket.CMD_OPEN) && lmsPacket.isOK()) {
            //状态(1)+通道号 (1)+锁状态(1)
            List<Integer> data = lmsPacket.getData();
            if (data.get(1).intValue() == portNum) {
                return data.get(2).intValue();
            }
        }
        return -1;
    }


    /**
     * 4444444444444444444444
     *
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[获取锁的状态]的命令  有回复
     * @Date 17:06 2018-12-27
     * @Param [address, port]
     **/
    public static byte[] getStateCmd(int address, int port) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.CMD_DOOR_STATE, port);
        return lmsPacket.getFullPack();
    }

    /**
     * 4444444444444444444444
     *
     * @return int
     * 返回:1 关闭 0 开门 -1 未知(有可能存在不匹配等情况,因此没有用布尔值)
     * @Author wanggl(lane)
     * @Description //TODO  根据packet获得目标通道的状态
     * @Date 17:03 2018-12-27
     * @Param [address, targetPort, lmsPacket]
     **/
    public int getStateCmdRes(int address, int targetPort) {
        if (isMatch(address, LmsPacket.CMD_DOOR_STATE)) {
            // 状态(1)+通道号(1)+锁状态(1)  data的组成
            List<Integer> data = this.getData();
            int port = data.get(1).intValue();
            int lockState = data.get(2).intValue();
            if (targetPort == port && this.isOK()) {
                return lockState;
            }
        }
        return -1;
    }


    /**
     * 5555555555555555555555555
     *
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[获取所有锁的状态]的命令  有回复
     * @Date 17:10 2018-12-27
     * @Param [address]
     **/
    public static byte[] getStateCmd(int address) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.CMD_ALL_STATE);
        return lmsPacket.getFullPack();
    }

    /**
     * 5555555555555555555555555
     *
     * @return java.util.List<java.lang.Integer>
     * @Author wanggl(lane)
     * @Description //TODO [获取所有锁的状态]的命令的回复
     * @Date 10:50 2018-12-28
     * @Param [address, lmsPacket]
     **/
    public List<Integer> getStateCmdRes(int address) {
        if (isMatch(address, LmsPacket.CMD_ALL_STATE)) {
            List<Integer> data = this.getData();
            //状态是否正确,长度是否匹配
            int stateListLength = data.size() - 2;
            if (this.isOK() && data.get(1).intValue() == stateListLength) {
                return data.subList(data.get(2), data.size() - 1);
            }
        }
        return null;
    }


    /**
     * 66666666666666666666666666666666666666666
     * 此包为设备端主动上传，服务端无需组包
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[主动上传门状态]的命令  无回复
     * @Date 17:11 2018-12-27
     * @Param [address]
     **/
//    public static byte[] getAutoUploadStateCmd(int address) {
//        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.CMD_AUTO_UPLOAD);
//        return lmsPacket.getFullPack();
//    }
//


    /**
     * 77777777777777777777777777777
     *
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[打开所有锁]的命令  有回复
     * @Date 16:53 2018-12-27
     * @Param [address]
     **/
    public static byte[] getOpenAllCmd(int address) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.CMD_OPEN_ALL);
        return lmsPacket.getFullPack();
    }

    /**
     * 77777777777777777777777777777
     *
     * @return int
     * @Author wanggl(lane)
     * @Description //TODO  [打开所有锁]  回复
     * @Date 11:03 2018-12-28
     * @Param [address]
     **/
    public int getOpenAllCmdRes(int address) {
        if (isMatch(address, LmsPacket.CMD_OPEN_ALL)) {
            List<Integer> data = this.getData();
            return data.get(0).intValue();
        }
        return -1;
    }


    public static final int PASSWORD_LENGTH = 8;

    /**
     * 88888888888888888888888
     *
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[修改密码]的命令  有回复
     * @Date 16:53 2018-12-27
     * @Param [address]
     **/
    public static byte[] getChangePswCmd(int address, String oldPsw, String newPsw) {
        if (oldPsw.length() == newPsw.length() && newPsw.length() == PASSWORD_LENGTH) {
            LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.CMD_CHANGE_PASSWORD);
            return lmsPacket.getFullPack();
        }
        return null;
    }

    /**
     * 88888888888888888888888
     *
     * @return int
     * @Author wanggl(lane)
     * @Description //TODO  [修改密码]的回复
     * @Date 11:03 2018-12-28
     * @Param [address]
     **/
    public int getChangePswRes(int address, String newPsw) {
        if (newPsw.length() == PASSWORD_LENGTH && isMatch(address, LmsPacket.CMD_CHANGE_PASSWORD)) {
            List<Integer> data = this.getData();
            return data.get(0).intValue();
        }
        return -1;
    }


    /**
     * 999999999999999999999999999999
     *
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[修改ip和端口]的命令  有回复
     * 密码(8)+ 服务器地址和端口号(n)
     * 服务器地址和端口号:服务器地址可以是 IP，也可以是域名，用冒号将地址和端口分开， 字符串最后加上结束符 0x00，服务器地址最大长度为 31 字节
     * @Date 16:53 2018-12-27
     * @Param [address]
     **/
    public static byte[] getChangeIpCmd(int address, String password, String ipOrDomain, String port) {
        List<Integer> data = new ArrayList<>();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(password).append(ipOrDomain).append(IP_PORT_SEPARATOR).append(port).append(IP_PORT_SUFFIX);
        byte[] dataBytes = stringBuffer.toString().getBytes();
        for (byte b : dataBytes) {
            data.add(Byte.valueOf(b).intValue());
        }
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.CMD_CHANGE_PASSWORD, data);
        return lmsPacket.getFullPack();
    }

    /**
     * 9999999999999999999999999999
     *
     * @return int
     * @Author wanggl(lane)
     * @Description //TODO  [修改ip和端口]的回复
     * @Date 11:03 2018-12-28
     * @Param [address]
     **/
    public int getChangeIpCmdRes(int address) {
        if (isMatch(address, LmsPacket.CMD_CHANGE_PASSWORD)) {
            List<Integer> data = this.getData();
            return data.get(0).intValue();
        }
        return -1;
    }


    /**
     * @return boolean
     * @Author wanggl(lane)
     * @Description //TODO 判断地址和命令是否一致
     * @Date 09:25 2018-12-28
     * @Param [address, lmsPacket]
     **/
    private boolean isMatch(int address, int cmd) {
        return this.getAddress() == address && this.getCommand() == cmd;
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
        if (this.command.intValue() == CMD_HEARTBEAT.intValue()) {
            return true;
        }
        return false;
    }

    public boolean isAutoPacket() {
        if (this.command.intValue() == CMD_AUTO_UPLOAD.intValue()) {
            return true;
        }
        return false;
    }
}
