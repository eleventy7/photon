package com.skyscraper.bookconsumer;

import org.agrona.concurrent.Agent;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;

import java.util.List;

public class BookConsumerAgent implements Agent
{
    private final List<OneToOneRingBuffer> inputRingBuffers;
    private final BookConsumerHandler handler;

    public BookConsumerAgent(final List<OneToOneRingBuffer> inputRingBuffers,
                             BookConsumerHandler handler)
    {
        this.inputRingBuffers = inputRingBuffers;
        this.handler = handler;
    }

    @Override
    public void onStart()
    {
        Agent.super.onStart();
    }

    @Override
    public int doWork()
    {
        int workCount = 0;
        for (int i = 0, inputRingBuffersSize = inputRingBuffers.size(); i < inputRingBuffersSize; i++) {
            OneToOneRingBuffer inputRingBuffer = inputRingBuffers.get(i);
            workCount += inputRingBuffer.read(handler);
        }
        return workCount;
    }

    @Override
    public void onClose()
    {
        Agent.super.onClose();
    }

    @Override
    public String roleName()
    {
        return "BookConsumer";
    }
}
