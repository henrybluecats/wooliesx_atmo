package com.woolworths.android.digital.food.atompoc;

import com.bluecats.sdk.BCLasso;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class DataRequestHelper {

    private static final String SHARED_SECRET_KEY = "SHARED_SECRET_KEY";

//    public final static String approvedPayload = "AgQDAyowMAkrQVBQUk9WRUQDLA==";
    public final static String approvedPayload = "AgQDAyowMAMs";
    public final static String honourWithIdPayload = "AgQDAyowOAMs";
    public final static String suspectedFraudPayload = "AgQDAyo1OQMs";
    public final static String notDeclinedPayload = "AgQDAyo4NQMs";
    public static String serviceKeyPayload = null;
    public static String serviceKeyHMAC = null;

    private static Map<String, String> messageMapping;
    static {
        messageMapping = new HashMap<>();
        messageMapping.put("00", "APPROVED");
        messageMapping.put("08", "HONOUR WITH ID");
        messageMapping.put("59", "SUSPECTED FRAUD");
        messageMapping.put("85", "NOT DECLINED");
    }
    public static String getMessageByCode(String code) {

        String msg = messageMapping.get(code);
        return TextUtils.isEmpty(msg)?("Response Code "+code):msg;
    }

    private static short sequence = 0;

    public static void makeDummyServicePayload(String payload) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            byte[] decodedPayload = Base64.decode(payload, Base64.NO_WRAP);
            baos.write(decodedPayload);

            ByteBuffer bbuf = ByteBuffer.allocate(Short.SIZE/8);
            bbuf.order(ByteOrder.LITTLE_ENDIAN); // must use little endian
            bbuf.putShort(++sequence);
            baos.write(bbuf.array());
        } catch (Exception e) {
            e.printStackTrace();
        }

        serviceKeyPayload = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

        /*
         Service Level message HMAC
         - This message and signature can be created as soon as the Lasso/Atmo is detected
        */

        byte keyBytes[] = SHARED_SECRET_KEY.getBytes(Charset.forName("US-ASCII"));

        byte[] hmac = digestHmacSHA256(keyBytes, baos.toByteArray());
        if (hmac != null) {
            serviceKeyHMAC = Base64.encodeToString(hmac, Base64.NO_WRAP);
        }
    }

    public static byte[] digestHmacSHA256(byte[] key, byte[] data) {
        try {

            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
            hmac.init(secret_key);

            return hmac.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ByteBuffer makeDataRequest(String btAddr) {
        if (serviceKeyHMAC == null || serviceKeyPayload == null || btAddr == null || btAddr.isEmpty()) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] deviceKey = btAddr.toUpperCase(Locale.US).getBytes(Charset.forName("US-ASCII"));
        byte[] serviceSignature = Base64.decode(serviceKeyHMAC, Base64.NO_WRAP); //use the same base64 flag as it was encoded.
        byte[] payloadData = Base64.decode(serviceKeyPayload, Base64.NO_WRAP);

        try {
            byte[] deviceKeyHMAC = digestHmacSHA256(deviceKey, serviceSignature);
            if (deviceKeyHMAC == null) {
                throw new IOException("deviceKeyHMAC is null");
            }

            final int digestLength = 8;
            byte digestHeaderBytes[] = {(byte)(digestLength+1), BCLasso.LassoValueType.LASSO_VALUE_TYPE_DIGEST.getByteValue()};

            //fill bytearray
            baos.write(payloadData);

            baos.write(digestHeaderBytes);

            //take first 8 bytes of HMAC
            baos.write(deviceKeyHMAC, 0, digestLength);


        } catch (IOException e) {
            e.printStackTrace();
        }

        byte headerBytes[] = {(byte)(baos.size()+1), BCLasso.LASSO_DATA_TYPE_ALPHA};
        ByteArrayOutputStream retData = new ByteArrayOutputStream();

        try {
            retData.write(headerBytes);
            retData.write(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ByteBuffer.wrap(retData.toByteArray());
    }
}
