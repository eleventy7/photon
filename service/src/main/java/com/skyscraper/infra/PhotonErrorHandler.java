package com.skyscraper.infra;

import lombok.extern.slf4j.Slf4j;
import org.agrona.ErrorHandler;

@Slf4j
public class PhotonErrorHandler implements ErrorHandler
{
    public static final PhotonErrorHandler INSTANCE = new PhotonErrorHandler();

    @Override
    public void onError(Throwable throwable)
    {
        log.error("agent failure.", throwable);
    }
}
