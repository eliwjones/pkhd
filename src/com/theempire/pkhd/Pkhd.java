package com.theempire.pkhd;

import java.util.concurrent.atomic.AtomicLong;

public final class Pkhd {
    private static Pkhd _pkhd;
    private static long zero_time;
    public final AtomicLong counter = new AtomicLong();

    public static Pkhd getInstance() {
        if (_pkhd == null) {
            _pkhd = new Pkhd();
        }
        return _pkhd;
    }

    public long getZeroTime() {
        if (zero_time == 0) {
            zero_time = System.currentTimeMillis();
        }
        return zero_time;
    }
}