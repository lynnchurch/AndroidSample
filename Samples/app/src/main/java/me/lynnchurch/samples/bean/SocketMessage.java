package me.lynnchurch.samples.bean;

public class SocketMessage {
    private NetAddress left;
    private NetAddress right;
    private String msg;
    private long time;

    public SocketMessage(NetAddress left, NetAddress right, String msg, long time) {
        this.left = left;
        this.right = right;
        this.msg = msg;
        this.time = time;
    }

    public NetAddress getLeft() {
        return left;
    }

    public void setLeft(NetAddress left) {
        this.left = left;
    }

    public NetAddress getRight() {
        return right;
    }

    public void setRight(NetAddress right) {
        this.right = right;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
