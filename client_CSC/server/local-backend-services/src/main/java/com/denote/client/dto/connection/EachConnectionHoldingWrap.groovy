package com.denote.client.dto.connection

import java.sql.Connection

class EachConnectionHoldingWrap {
    Connection connection;
    long timestamp;
    long triggerTimes = 0;
    boolean isUsing = false;

    EachConnectionHoldingWrap(Connection connection) {
        this.connection = connection
    }
}
