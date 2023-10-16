package com.denote.client.utils.http


import java.util.concurrent.CountDownLatch

class DownloadRef {
    CountDownLatch lock = new CountDownLatch(1);
    List<String> errList = new ArrayList<>()
    int totalSize;
    int active_begin = 0;
    int chunkSizeForM = 0;
    int downloadSize = 0;
    Closure cbk;

    public synchronized Map<String, Integer> getNextBeginAndEnd() {
        if (active_begin > totalSize) {
            return null;
        }
        int crtBeginValue = active_begin;
        int crtEndValue = active_begin + chunkSizeForM;
        if (crtEndValue >= totalSize) {
            crtEndValue = totalSize
        }
        if (crtBeginValue >= totalSize) {
            crtBeginValue = totalSize
        }
        if (crtBeginValue == crtEndValue) {
            return null;
        }
        println "Size active_begin: ${crtBeginValue}, end: ${crtEndValue}"
        active_begin = crtBeginValue + chunkSizeForM
        return [
                begin: crtBeginValue,
                end  : crtEndValue
        ]
    }

    def logger = com.denote.client.utils.GLogger.g("downloadRef")

//    RandomAccessFile randomAccessFile = null

    public synchronized void writeData(File file, InputStream inputStream, int begin, int end) {
//        if(randomAccessFile == null){
//        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")
        logger.debug("seeking pos to ${begin}")
        randomAccessFile.seek(begin)
        logger.debug("preparing for reading the bytes")
        int totalBytes = Math.abs(end - begin)
        byte[] bytes = new byte[4096 * 20]
        int totalReadNum = 0;
        while (true) {
            def read = inputStream.read(bytes)
            logger.debug("read num: ${read}, it's for ${begin}-${end}")
            if (read == -1) {
                break;
            } else {
                logger.debug("writing bytes into file: ${read}, it's for ${begin}-${end}")
                randomAccessFile.write(bytes, 0, read)
                totalReadNum += read;
                downloadSize += read
                if (cbk) {
                    cbk(downloadSize, totalSize)
                }
            }
        }
        inputStream.close()
        randomAccessFile.close()
        logger.debug("closed the inputStream")
    }

    int currentSize = 0;

    public void closeRs() {
    }
}
