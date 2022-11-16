package com.skyscraper.infra;

import org.agrona.concurrent.AtomicBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.RingBufferDescriptor;

import java.nio.ByteBuffer;

public final class RingBufferBuilder
{
    private RingBufferBuilder() {
        //
    }

    public static OneToOneRingBuffer getOneToOneRingBuffer(int capacity, boolean offheap)
    {
        final int bufferCapacity = capacity + RingBufferDescriptor.TRAILER_LENGTH;
        final AtomicBuffer buffer;
        if (offheap)
        {
            buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(bufferCapacity));
        }
        else
        {
            buffer = new UnsafeBuffer(ByteBuffer.allocate(bufferCapacity));
        }
        return new OneToOneRingBuffer(buffer);
    }

    public static ManyToOneRingBuffer getManyToOneRingBuffer(int capacity, boolean offheap)
    {
        final int bufferCapacity = capacity + RingBufferDescriptor.TRAILER_LENGTH;
        final AtomicBuffer buffer;
        if (offheap)
        {
            buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(bufferCapacity));
        }
        else
        {
            buffer = new UnsafeBuffer(ByteBuffer.allocate(bufferCapacity));
        }
        return new ManyToOneRingBuffer(buffer);
    }
}
