package com.yniot.lms.adaptor.handler;

import org.tio.core.ChannelContext;
import org.tio.core.intf.AioListener;
import org.tio.core.intf.Packet;

/**
 * @Auther: lane
 * @Date: 2018-12-27 10:55
 * @Description:
 * @Version 1.0.0
 */
public class LmsAioHandler implements AioListener {
    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {

    }

    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) throws Exception {

    }

    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {

    }

    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {

    }

    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {

    }

    @Override
    public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) throws Exception {

    }
}
