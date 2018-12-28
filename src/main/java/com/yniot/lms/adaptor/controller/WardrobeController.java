package com.yniot.lms.adaptor.controller;

import com.yniot.lms.adaptor.entity.LmsPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: lane
 * @Date: 2018-12-27 10:06
 * @Description:
 * @Version 1.0.0
 */
public class WardrobeController {

    public static final Integer HEARTBEAT_RES_DATA = 0x00;


    /**
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取服务端对心跳包的回复包
     * @Date 17:13 2018-12-27
     * @Param [address]
     **/
    public byte[] heartbeatResponse(int address) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.HEARTBEAT, HEARTBEAT_RES_DATA);
        return lmsPacket.getBody();
    }

    /**
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取开锁指令包
     * @Date 16:50 2018-12-27
     * @Param [address, portNum]
     **/
    public byte[] getOpenCmd(int address, int portNum) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.OPEN, portNum);
        return lmsPacket.getFullPack();
    }

    public boolean openSuccess(int address, LmsPacket lmsPacket) {
        if (isMatch(address, lmsPacket)) {
            List<Integer> data = lmsPacket.getData();
        }

        return false;
    }

    /**
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 打开所有
     * @Date 16:53 2018-12-27
     * @Param [address]
     **/
    public byte[] getOpenCmd(int address) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.OPEN_ALL);
        return lmsPacket.getFullPack();
    }

    /**
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[获取锁的状态]的命令
     * @Date 17:06 2018-12-27
     * @Param [address, port]
     **/
    public byte[] getStateCmd(int address, int port) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.DOOR_STATE, port);
        return lmsPacket.getFullPack();
    }


    /**
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[获取设备ID]的命令
     * @Date 17:06 2018-12-27
     * @Param [address, port]
     **/
    public byte[] getDeviceId(int address) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.DEVICE_ID);
        return lmsPacket.getFullPack();
    }

    //设备 ID 号:8 个字节，可通过配置软件对设备端进行配置。
    public String parseToID(int address, LmsPacket lmsPacket) {
        List<Integer> data = lmsPacket.getData();
        if (data != null && isMatch(address, lmsPacket) && data.size() == LmsPacket.ID_LENGTH) {
            StringBuffer buffer = new StringBuffer();
            for (int val : data) {
                buffer.append((char) val);
            }
            return buffer.toString();
        } else {
            return null;
        }
    }

    public byte[] parseIdToBytes(String id) {
        if (id.length() != LmsPacket.ID_LENGTH) {
            return null;
        }
        byte[] idBytes = id.getBytes();
        return idBytes;
    }

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
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[获取所有锁的状态]的命令
     * @Date 17:10 2018-12-27
     * @Param [address]
     **/
    public byte[] getStateCmd(int address) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.ALL_STATE);
        return lmsPacket.getFullPack();
    }


    public List<Integer> parseAllState(int address, LmsPacket lmsPacket) {
        if (isMatch(address, lmsPacket)) {
            List<Integer> data = lmsPacket.getData();
            //状态是否正确,长度是否匹配
            int stateListLength = data.size() - 2;
            if (lmsPacket.isOK() && data.get(1).intValue() == stateListLength) {
                return data.subList(data.get(2), data.size() - 1);
            }
        }
        return null;
    }

    /**
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[主动上传门状态]的命令
     * @Date 17:11 2018-12-27
     * @Param [address]
     **/
    public byte[] autoUploadState(int address) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.UPLOAD_STATE);
        return lmsPacket.getFullPack();
    }

    /**
     * @return int
     * 返回:1 关闭 0 开门 -1 未知
     * @Author wanggl(lane)
     * @Description //TODO  根据packet获得目标通道的状态
     * @Date 17:03 2018-12-27
     * @Param [address, targetPort, lmsPacket]
     **/
    public int getDoorState(int address, int targetPort, LmsPacket lmsPacket) {
        if (lmsPacket != null && lmsPacket.getAddress() == address) {
            // 状态(1)+通道号(1)+锁状态(1)  data的组成
            List<Integer> data = lmsPacket.getData();
            int port = data.get(1).intValue();
            int lockState = data.get(2).intValue();
            if (targetPort == port && lmsPacket.isOK()) {
                return lockState;
            }
        }
        return -1;
    }

    /**
     * @return boolean
     * @Author wanggl(lane)
     * @Description //TODO 判断地址是否一致
     * @Date 09:25 2018-12-28
     * @Param [address, lmsPacket]
     **/
    private boolean isMatch(int address, LmsPacket lmsPacket) {
        if (lmsPacket == null) {
            return false;
        }
        return lmsPacket.getAddress() == address;
    }


}
