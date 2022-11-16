package com.skyscraper.command;

import com.dslplatform.json.CompiledJson;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Data
@NoArgsConstructor
@CompiledJson
public class FtxAuth {
    public final String op = "login";
    public Map<String, String> args;

    public void encodeArgs(String apiKey, String secretKey) {
        try {
            final var mac = Mac.getInstance("HmacSHA256");
            final var signingKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(signingKey);
            final long now = System.currentTimeMillis();
            args = Map.of("key", apiKey,
                    "sign", Hex.encodeHexString(mac.doFinal(String.format("%swebsocket_login", now)
                            .getBytes(StandardCharsets.UTF_8))),
                    "time", String.valueOf(now));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
