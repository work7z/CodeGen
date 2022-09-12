package com.denote.client.exceptions

class CannotDownloadException extends RuntimeException {
    CannotDownloadException(String str) {
        super("cannot download the href: "+str)
    }
}
