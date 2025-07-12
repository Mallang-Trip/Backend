package mallang_trip.backend.domain.kgInicis.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SEED_ENC {

    @Value("${identification.seed}")
    private String SEED;

    public String decrypt(String str , String key) {

        /*복호화 문자열이 null이거나 비어있으면 null 반환*/
        if (str == null || str.isEmpty()) {
            return null;
        }

        byte[] Key = new String(key).getBytes();
        byte[] SEEED_IV = new String(SEED).getBytes();

        Decoder decoder1 = Base64.getDecoder();
        byte[] SEED_Key = decoder1.decode(Key);


        Decoder decoder = Base64.getDecoder();
        byte[] msg = decoder.decode(str);

        String result = "";
        byte[] dec = null;

        dec = KISA_SEED_CBC.SEED_CBC_Decrypt(SEED_Key, SEEED_IV, msg, 0, msg.length);
        result = new String(dec, StandardCharsets.UTF_8);
        return result;
    }

}
