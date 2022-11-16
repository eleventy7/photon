package com.skyscraper.bookconsumer;

import lombok.extern.slf4j.Slf4j;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.MessageHandler;

@Slf4j
public class BookConsumerHandler implements MessageHandler
{
    @Override
    public void onMessage(int msgTypeId, MutableDirectBuffer buffer, int index, int length)
    {
        //removed
    }
}
