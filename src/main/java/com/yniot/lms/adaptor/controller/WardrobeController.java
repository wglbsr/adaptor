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

    public static final int HEARTBEAT_RES_DATA = 0x00;


    /**
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取服务端对心跳包的回复包
     * @Date 17:13 2018-12-27
     * @Param [address]
     **/
    public byte[] getHeartbeatRes(int address) {
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
    public byte[] getDeviceIdCmd(int address) {
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
    public String getDeviceIdCmdRes(int address, LmsPacket lmsPacket) {
        List<Integer> data = lmsPacket.getData();
        if (data != null && isMatch(address, lmsPacket, LmsPacket.CMD_DEVICE_ID) && data.size() == LmsPacket.ID_LENGTH) {
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
    public byte[] getOpenCmd(int address, int portNum) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.CMD_OPEN, portNum);
        return lmsPacket.getFullPack();
    }

    /**
     * 333333333333333333
     *
     * @return boolean
     * @Author wanggl(lane)
     * @Description //TODO [开单个锁]指令的回复
     * @Date 10:34 2018-12-28
     * @Param [address, portNum, lmsPacket]
     **/
    public boolean getOpenCmdRes(int address, int portNum, LmsPacket lmsPacket) {
        if (isMatch(address, lmsPacket, LmsPacket.CMD_OPEN) && lmsPacket.isOK()) {
            //状态(1)+通道号 (1)+锁状态(1)
            List<Integer> data = lmsPacket.getData();
            if (data.get(1).intValue() == portNum) {
                return data.get(2).intValue() == LmsPacket.RES_DOOR_OPENED;
            }
        }
        return false;
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
    public byte[] getStateCmd(int address, int port) {
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
    public int getStateCmdRes(int address, int targetPort, LmsPacket lmsPacket) {
        if (isMatch(address, lmsPacket, LmsPacket.CMD_DOOR_STATE)) {
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
     * 5555555555555555555555555
     *
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[获取所有锁的状态]的命令  有回复
     * @Date 17:10 2018-12-27
     * @Param [address]
     **/
    public byte[] getStateCmd(int address) {
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
    public List<Integer> getStateCmdRes(int address, LmsPacket lmsPacket) {
        if (isMatch(address, lmsPacket, LmsPacket.CMD_ALL_STATE)) {
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
     * 66666666666666666666666666666666666666666
     *
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[主动上传门状态]的命令  无回复
     * @Date 17:11 2018-12-27
     * @Param [address]
     **/
    public byte[] getAutoUploadStateCmd(int address) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.CMD_UPLOAD_STATE);
        return lmsPacket.getFullPack();
    }

    /**
     * 77777777777777777777777777777
     *
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 获取[打开所有锁]的命令  有回复
     * @Date 16:53 2018-12-27
     * @Param [address]
     **/
    public byte[] getOpenAllCmd(int address) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.CMD_OPEN_ALL);
        return lmsPacket.getFullPack();
    }

    public int getOpenAllCmdRes(int address, LmsPacket lmsPacket) {
        if (isMatch(address, lmsPacket, LmsPacket.CMD_OPEN_ALL)) {
            List<Integer> data = lmsPacket.getData();
            return data.get(0).intValue();
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

    private boolean isMatch(int address, LmsPacket lmsPacket, int cmd) {
        if (lmsPacket == null) {
            return false;
        }
        return lmsPacket.getAddress() == address && lmsPacket.getCommand() == cmd;
    }


}
