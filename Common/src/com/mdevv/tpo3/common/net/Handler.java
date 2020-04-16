package com.mdevv.tpo3.common.net;

public abstract class Handler implements Runnable {
    public abstract void handle();

    @Override
    public void run() {
        handle();
    }
}

