package com.yniot.lms.adaptor.controller;

import com.yniot.lms.adaptor.entity.LmsPacket;

import java.io.UnsupportedEncodingException;
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
     * @Author wanggl(lane)
     * @Description //TODO 获取服务端对心跳包的回复包
     * @Date 17:13 2018-12-27
     * @Param [address]
     * @return byte[]
     **/
    public byte[] heartbeatResponse(int address) throws UnsupportedEncodingException {
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

    /**
     * @return byte[]
     * @Author wanggl(lane)
     * @Description //TODO 打开所有
     * @Date 16:53 2018-12-27
     * @Param [address]
     **/
    public byte[] getOpenCmd(int address) throws UnsupportedEncodingException {
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
     * @Author wanggl(lane)
     * @Description //TODO 获取[获取所有锁的状态]的命令
     * @Date 17:10 2018-12-27
     * @Param [address]
     * @return byte[]
     **/
    public byte[] getStateCmd(int address) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.ALL_STATE);
        return lmsPacket.getFullPack();
    }

    
    /**
     * @Author wanggl(lane)
     * @Description //TODO 获取[主动上传门状态]的命令
     * @Date 17:11 2018-12-27
     * @Param [address]
     * @return byte[]
     **/
    public byte[] autoUploadState(int address) {
        LmsPacket lmsPacket = new LmsPacket(address, LmsPacket.UPLOAD_STATE);
        return lmsPacket.getFullPack();
    }

    /**
     * @return int
     * 返回:1 关闭 0 开门 -1 未知
     * @Author wanggl(lane)
     * @Description //TODO
     * @Date 17:03 2018-12-27
     * @Param [address, targetPort, lmsPacket]
     **/
    public int getDoorState(int address, int targetPort, LmsPacket lmsPacket) {
        if (lmsPacket != null && lmsPacket.getAddress() == address) {
            // 状态(1)+通道号(1)+锁状态(1)  data的组成
            List<Integer> data = lmsPacket.getData();
            int state = data.get(0).intValue();
            int port = data.get(1).intValue();
            int lockState = data.get(2).intValue();
            if (targetPort == port && state == 0) {
                return lockState;
            }
        }
        return -1;
    }

}
