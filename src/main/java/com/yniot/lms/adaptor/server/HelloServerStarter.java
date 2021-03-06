package com.yniot.lms.adaptor.server;

import com.yniot.lms.adaptor.common.Const;
import org.tio.server.ServerGroupContext;
import org.tio.server.TioServer;
import org.tio.server.intf.ServerAioHandler;
import org.tio.server.intf.ServerAioListener;

import java.io.IOException;

/**
 * @Auther: lane
 * @Date: 2018-12-26 13:56
 * @Description:
 * @Version 1.0.0
 */
public class HelloServerStarter {
    //handler, 包括编码、解码、消息处理
    public static ServerAioHandler aioHandler = new HelloServerAioHandler();
//    public static ServerAioHandler aioHandler = new LmsServerPacketHandler();
    //事件监听器，可以为null，但建议自己实现该接口，可以参考showcase了解些接口
    public static ServerAioListener aioListener = null;
    //一组连接共用的上下文对象
    public static ServerGroupContext serverGroupContext = new ServerGroupContext("hello-tio-server", aioHandler, aioListener);
    //tioServer对象
    public static TioServer tioServer = new TioServer(serverGroupContext);
    //有时候需要绑定ip，不需要则null
    public static String serverIp = null;
    //监听的端口
    public static int serverPort = Const.PORT;
    /**
     * 启动程序入口
     */
    public static void main(String[] args) throws IOException {
        serverGroupContext.setHeartbeatTimeout(Const.TIMEOUT);
        tioServer.start(serverIp, serverPort);
    }
}
