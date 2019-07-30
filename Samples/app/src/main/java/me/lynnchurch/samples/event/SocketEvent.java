package me.lynnchurch.samples.event;

public class SocketEvent {
    public static final int CODE_PLAIN = 0;
    public static final int CODE_TCP_SERVER_ADDRESS = 1; // 发送服务器地址
    public static final int CODE_TCP_HEARTBEAT = 2; // 心跳包
    public static final int CODE_TCP_NEW_CLIENT_CONNECTED = 3; // 新的客户端连接
    public static final int CODE_TCP_SERVER_ALIVE = 4; // 服务器还活着
    public static final int CODE_TCP_SERVER_DEAD = 5; // 服务器已挂
    public static final int CODE_TCP_CLIENT_ALIVE = 6; // 客户端还活着
    public static final int CODE_TCP_CLIENT_DEAD = 7; // 客户端已挂
    public static final int CODE_TCP_TEXT = 8; // 文本消息
    public int code;
    public String msg;
    public byte[] data;

    public SocketEvent(String msg) {
        this(CODE_PLAIN, msg);
    }

    public SocketEvent(int code, String msg) {
        this(code, msg, null);
    }

    public SocketEvent(int code, String msg, byte[] data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
