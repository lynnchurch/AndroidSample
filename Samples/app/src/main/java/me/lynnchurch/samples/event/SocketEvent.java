package me.lynnchurch.samples.event;

public class SocketEvent {
    public static final int CODE_PLAIN = 0;
    public static final int CODE_TCP_SERVER_ADDRESS = 1; // 发送服务器地址
    public static final int CODE_TCP_HEARTBEAT = 2; // 心跳包
    public static final int CODE_TCP_SERVER_ALIVE = 3; // 服务器还活着
    public static final int CODE_TCP_SERVER_DEAD = 4; // 服务器已挂
    public int code;
    public String msg;
    public String data;

    public SocketEvent(String msg) {
        this(CODE_PLAIN, msg);
    }

    public SocketEvent(int code, String msg) {
        this(code, msg, null);
    }

    public SocketEvent(int code, String msg, String data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
