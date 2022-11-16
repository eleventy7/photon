package com.skyscraper.handler;

import com.skyscraper.enums.CcyPair;
import lombok.extern.slf4j.Slf4j;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;

@Slf4j
public class CcyPairAgent implements Agent {
    private final CcyPair ccyPair;
    private final CcyPairHandler handler;
    private final ManyToOneRingBuffer ccySpecificRingBuffer;

    public CcyPairAgent(CcyPair ccyPair,
                        CcyPairHandler handler,
                        ManyToOneRingBuffer ccySpecificRingBuffer) {
        this.ccyPair = ccyPair;
        this.handler = handler;
        this.ccySpecificRingBuffer = ccySpecificRingBuffer;
        log.info("{} Agent ready", ccyPair);
    }

    @Override
    public void onStart() {
        Agent.super.onStart();
    }

    @Override
    public int doWork() {
        int workDone = 0;
        workDone += ccySpecificRingBuffer.read(handler);
        return workDone;
    }

    @Override
    public void onClose() {
        Agent.super.onClose();
        log.info("Shutting down");
    }

    @Override
    public String roleName() {
        return ccyPair + "-agent";
    }

    public CcyPair getAsset() {
        return ccyPair;
    }

}
