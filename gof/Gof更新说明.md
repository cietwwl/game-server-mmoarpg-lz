2014-08-26
=========
connsrv的通讯层增加上行消息RC4加密支持，默认关闭。在`gofConfig.properties`中添加以下配置来开启，开启时需要在客户端进行相应处理。

    conn.encrypt=true

2014-08-08
=========

修改思路：使用Java8的函数接口替换原来的反射类Method，提高执行效率

这次更新比较多，有比较难升级的直接问吧

更新步骤：

1. 下载安装最新的eclipse
------------------------


2 修改Callback使用方式
--------------------
- 不再生成回调的callbak类
- 修改为以下调用方式，直接使用函数接口
prx.listenResult(this::_result_loadGeneralData,  "humanObj", humanObj);

3 修改SeamService接口
-------------------
 SeamService各个虚函数修改为以下方式

    public int methodAccountMsg() {
        return AccountServiceProxy.EnumCall.ORG_GOF_DEMO_SEAM_ACCOUNT_ACCOUNTSERVICE_MSGHANDLER_LONG_CONNECTIONSTATUS_BYTES;
    }

4 修改MsgReceiver 和 Listener
---------------------------
 WorldStartup::main中进行以下修改：
	
    //初始化基本环境
    //MsgSender.init();
    //Event.init();
    MsgReceiverInit.init(MsgSender.instance);
    ListenerInit.init(Event.instance);

5 其他编译出错的代码单独看吧
----------------------------