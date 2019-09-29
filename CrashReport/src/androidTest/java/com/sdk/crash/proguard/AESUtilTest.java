package com.sdk.crash.proguard;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AESUtilTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void doEncodeAndDecode() {
        String text = "this is a text need to encrypt";
        String encodeText = AESUtil.getInstance().encrypt(text);
        String decodeText = AESUtil.getInstance().decrypt(encodeText);

        Assert.assertEquals(text, decodeText);
    }
}
