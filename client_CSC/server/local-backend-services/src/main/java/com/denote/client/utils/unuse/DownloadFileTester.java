package com.denote.client.utils.unuse;

//import com.ejlchina.okhttps.OkHttps;
//
//import java.io.File;
//import java.util.concurrent.CountDownLatch;

public class DownloadFileTester {
//    static String url = "https://mirrors.tuna.tsinghua.edu.cn/AdoptOpenJDK/8/jre/x64/linux/OpenJDK8U-jre_x64_linux_hotspot_8u322b06.tar.gz";
//
//    public static void main(String[] args) throws InterruptedException {
//        long totalSize = OkHttps.sync(url).get().getBody()
//                .close()             // 因为这次请求只是为了获得文件大小，不消费报文体，所以直接关闭
//                .getLength();        // 获得待下载文件的大小（由于未消费报文体，所以该请求不会消耗下载报文体的时间和网络流量）
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//        File toFile = new File("/Users/jerrylai/test/jdk_multiple_thread.tar.gz");
//        if (!toFile.getParentFile().exists()) {
//            toFile.getParentFile().mkdirs();
//        }
//        if (toFile.exists()) {
//            toFile.delete();
//        }
//
//        downloadWithContinueDownloading(totalSize, 0, toFile.getAbsolutePath(), countDownLatch);      // 从第 0 块开始下载
////        countDownLatch.await();
//    }
//
//    static void downloadWithContinueDownloading(long totalSize, int index, String toFile, CountDownLatch countDownLatch) {
//        long size = 3 * 1024 * 1024;                 // 每块下载 3M
//        long start = index * size;
//        long end = Math.min(start + size, totalSize);
//        OkHttps.sync(url)
//                .setRange(start, end)                // 设置本次下载的范围
//                .get().getBody()
//                .toFile(toFile)      // 下载到同一个文件里
//                .setAppended()                       // 开启文件追加模式
//                .setOnSuccess((File file) -> {
//                    if (end < totalSize) {           // 若未下载完，则继续下载下一块
//                        System.out.println("Continue download");
//                        downloadWithContinueDownloading(totalSize, index + 1, toFile, countDownLatch);
//                    } else {
//                        System.out.println("下载完成");
////                        countDownLatch.countDown();
//                    }
//                })
////                .setOnFailure((Download.Failure f) -> {
////                    System.out.println("下载失败");
////                    countDownLatch.countDown();
////                })
//                .start();
//    }
}
