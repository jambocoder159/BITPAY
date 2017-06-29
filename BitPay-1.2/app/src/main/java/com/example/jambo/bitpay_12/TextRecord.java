package com.example.jambo.bitpay_12;

import android.nfc.NdefRecord;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by jambo on 2017/4/25.
 */

class TextRecord {
    private final String mText;
    private TextRecord(String text) {
        mText = text;
    }
    public String getText() {
        return mText;
    }
    public static boolean isText(NdefRecord ndefRecord) {
        // TODO Auto-generated method stub
        try {
            parse(ndefRecord);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 吧Ndef记录转为String，这里的格式是TNF_WELL_KNOWN with RTD_TEXT
     * @param ndefRecord
     * @return
     */
    public static String parse(NdefRecord ndefRecord) {
        // TODO Auto-generated method stub
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN
                || !Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            throw new IllegalArgumentException("NFC类型不正确...");
        }
        try {
            byte[] payload = ndefRecord.getPayload();
            //把byte转为String
            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8"
                    : "UTF-16";
            int languageCodeLength = payload[0] & 0077;
            String languageCode = new String(payload, 1, languageCodeLength,
                    "US-ASCII");
            String text = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            return text;
        } catch (UnsupportedEncodingException e) {
            // should never happen unless we get a malformed tag.
            throw new IllegalArgumentException(e);
        }
    }


    // 将纯文本内容从NdefRecord对象中解析出来
    public static TextRecord parse2(NdefRecord record) {

        if (record.getTnf() != NdefRecord.TNF_WELL_KNOWN)
            return null;
        if (!Arrays.equals(record.getType(), NdefRecord.RTD_TEXT))
            return null;

        try {
            byte[] payload = record.getPayload();
            /*
             * payload[0] contains the "Status Byte Encodings" field, per the
             * NFC Forum "Text Record Type Definition" section 3.2.1.
             *
             * bit7 is the Text Encoding Field.
             *
             * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
             * The text is encoded in UTF16
             *
             * Bit_6 is reserved for future use and must be set to zero.
             *
             * Bits 5 to 0 are the length of the IANA language code.
             */
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8"
                    : "UTF-16";
            int languageCodeLength = payload[0] & 0x3f;
            String languageCode = new String(payload, 1, languageCodeLength,
                    "US-ASCII");
            String text = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            return new TextRecord(text);
        } catch (UnsupportedEncodingException e) {
            // should never happen unless we get a malformed tag.
            throw new IllegalArgumentException(e);
        }
    }

}



