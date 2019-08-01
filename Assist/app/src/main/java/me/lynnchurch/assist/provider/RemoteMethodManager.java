package me.lynnchurch.assist.provider;

import java.util.HashMap;
import java.util.Map;

public class RemoteMethodManager {
    public Map<String, RemoteMethod> remoteMethodMap = new HashMap<>();

    public RemoteMethod get(String name) {
        return remoteMethodMap.get(name);
    }

    public void put(RemoteMethod remoteMethod) {
        remoteMethodMap.put(remoteMethod.getMethodName(), remoteMethod);
    }
}
