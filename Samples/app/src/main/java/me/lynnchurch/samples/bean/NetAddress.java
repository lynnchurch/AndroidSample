package me.lynnchurch.samples.bean;

import androidx.annotation.NonNull;

public class NetAddress {
    private String ip;
    private int port;

    public NetAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetAddress that = (NetAddress) o;
        return port == that.port &&
                ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + port;
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return getIp() + ":" + getPort();
    }
}
