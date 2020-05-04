package com.priv.arcsoft;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);

        String testStr = "您的百世快递到了";
        String  encode = Base64.encodeBase64String(testStr.getBytes());
        //5oKo55qE55m+5LiW5b+r6YCS5Yiw5LqG
        System.out.println(encode);

    }
}