package cc.codegen.client.service;

import java.util.concurrent.atomic.AtomicInteger;

public class NetService {
    private static AtomicInteger portRef = new AtomicInteger(18000);

    public static synchronized String getPort() {
        return portRef.getAndIncrement() + "";
    }

}
