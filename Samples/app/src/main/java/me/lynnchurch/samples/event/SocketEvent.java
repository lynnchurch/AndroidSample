package me.lynnchurch.samples.event;

public class SocketEvent<T> {
    public static final int CODE_PLAIN = 0;
    public static final int CODE_TCP_SERVER_ADDRESS = 1;
    public static final int CODE_TCP_SERVER_DEAD = 2;
    public int code;
    public String msg;
    public T data;

    public SocketEvent(String msg) {
        this(CODE_PLAIN, msg);
    }

    public SocketEvent(int code, String msg) {
        this(code, msg, null);
    }

    public SocketEvent(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
