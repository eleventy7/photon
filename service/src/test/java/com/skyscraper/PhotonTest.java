package com.skyscraper;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;
import com.skyscraper.command.FtxPing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PhotonTest {
    @Test
    public void testPhoton() {
        Photon classUnderTest = new Photon();
    }

    @Test
    public void serialize() throws IOException
    {
        FtxPing ping = new FtxPing();
        ByteArrayOutputStream pingBuff = new ByteArrayOutputStream();
        final DslJson<FtxPing> json = new DslJson<>(Settings.basicSetup());
        json.serialize(new FtxPing(), pingBuff);
        final String pingStr = pingBuff.toString(StandardCharsets.UTF_8);
        Assertions.assertNotNull(pingStr);

    }
}
