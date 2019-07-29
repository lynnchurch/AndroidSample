package me.lynnchurch.samples.bean;

import androidx.annotation.Nullable;

import java.util.Objects;

public class ServerItem {
    private NetAddress netAddress;
    private long foundTime;

    public ServerItem(NetAddress netAddress, long foundTime) {
        this.netAddress = netAddress;
        this.foundTime = foundTime;
    }

    public NetAddress getNetAddress() {
        return netAddress;
    }

    public void setNetAddress(NetAddress netAddress) {
        this.netAddress = netAddress;
    }

    public long getFoundTime() {
        return foundTime;
    }

    public void setFoundTime(long foundTime) {
        this.foundTime = foundTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerItem that = (ServerItem) o;
        return netAddress.equals(that.netAddress);
    }

    @Override
    public int hashCode() {
        return netAddress.hashCode();
    }
}
