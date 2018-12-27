package com.yniot.lms.adaptor.server;

import com.yniot.lms.adaptor.entity.LmsPacket;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.Tio;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.server.intf.ServerAioHandler;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * @Auther: lane
 * @Date: 2018-12-26 13:55
 * @Description:
 * @Version 1.0.0
 */
public class LmsServerPacketHandler implements ServerAioHandler {
    /**
     * 解码：把接收到的ByteBuffer，解码成应用可以识别的业务消息包
     * 总的消息结构：消息头 + 消息体
     * 消息头结构：    4个字节，存储消息体的长度
     * 消息体结构：   对象的json串的byte[]
     */
    @Override
    public LmsPacket decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext) throws AioDecodeException {
        //提醒：buffer的开始位置并不一定是0，应用需要从buffer.position()开始读取数据
        //收到的数据组不了业务包，则返回null以告诉框架数据不够
        if (readableLength < LmsPacket.LENGTH_MIN) {
            return null;
        }
        byte[] headerBytes = new byte[LmsPacket.HEADER_LENGTH];
        //读取消息体的长度
        buffer.get(headerBytes, 0, headerBytes.length);
        Byte lengthByte = buffer.get();
        Integer fullPackLength = lengthByte.intValue();
        //数据不正确，则抛出AioDecodeException异常
        if (fullPackLength < 0) {
            throw new AioDecodeException("fullPack length [" + fullPackLength + "] is not right, remote:" + channelContext.getClientNode());
        }
        //除去头部和长度后的长度
        int bodyLength = fullPackLength - LmsPacket.HEADER_LENGTH - 1;
        //收到的数据是否足够组包
        // 不够消息体长度(剩下的buffer组不了消息体)
        if (readableLength - bodyLength < 0) {
            return null;
        } else //组包成功
        {
            LmsPacket lmsPacket = null;
            if (fullPackLength > 0) {
                byte address = buffer.get();
                byte cmd = buffer.get();
                byte[] data = new byte[0];
                int dataLength = fullPackLength - LmsPacket.LENGTH_MIN;
                if (dataLength > 0) {
                    data = new byte[dataLength];
                    buffer.get(data);
                }
                byte check = buffer.get();
                try {
                    lmsPacket = LmsPacket.parse(headerBytes, lengthByte, address, cmd, data, check);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            return lmsPacket;
        }
    }

    /**
     *
     */
    @Override
    public ByteBuffer encode(Packet packet, GroupContext groupContext, ChannelContext channelContext) {
        LmsPacket lmsPacket = (LmsPacket) packet;
        byte[] fullPack = lmsPacket.getFullPack();
        int packLen = 0;
        if (fullPack != null) {
            packLen = fullPack.length;
        }
        int allLen = packLen;
        ByteBuffer buffer = ByteBuffer.allocate(allLen);
        //设置字节序
        buffer.order(groupContext.getByteOrder());
        //写入消息体
        if (fullPack != null) {
            buffer.put(fullPack);
        }
        return buffer;
    }

    /**
     * 处理消息
     */
    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {
        LmsPacket helloPacket = (LmsPacket) packet;
        byte[] body = helloPacket.getBody();
        if (body != null) {
//            String str = new String(body, LmsPacket.CHARSET);
            LmsPacket respPacket = new LmsPacket(0x00, LmsPacket.HEARTBEAT, 0x00);
            Tio.send(channelContext, respPacket);
        }
        return;
    }
}
