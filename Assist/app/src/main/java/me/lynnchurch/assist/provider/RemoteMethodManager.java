package me.lynnchurch.assist.provider;

import java.util.HashMap;
import java.util.Map;

public class RemoteMethodManager {
    public static Map<String, RemoteMethod> remoteMethodMap = new HashMap<>();

    public static RemoteMethod get(String name) {
        return remoteMethodMap.get(name);
    }

    public static void put(RemoteMethod remoteMethod) {
        remoteMethodMap.put(remoteMethod.getMethodName(), remoteMethod);
    }
}
