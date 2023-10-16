package com.denote.client.concurrent

import java.util.concurrent.CountDownLatch

class MsgChannelFlag {
    public static int COUNT_VALUE = 1;
    public static CountDownLatch countDownLatchWhenNewData = new CountDownLatch(COUNT_VALUE);
}
