// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import fitnesse.FitnesseBaseTestCase;
import org.junit.Test;
import util.TimeMeasurement;

import java.util.Random;

import static org.junit.Assert.*;

public class HashingCipherTest extends FitnesseBaseTestCase {
    private String[] inputs = new String[]{"123", "abc", "12345678901234567890", "this is a test", "!@#$%^&*()"};
    private HashingCipher crypter = new HashingCipher();

    @Test
    public void testHashReturnsDifferentValueThanPassed() throws Exception {
        String testString = "This is a test string";
        String hash = crypter.encrypt(testString);
        assertNotNull(hash);
        assertFalse(hash.equals(testString));
    }

    @Test
    public void testDifferentStringHashDifferently() throws Exception {
        String hash1 = crypter.encrypt("123456");
        String hash2 = crypter.encrypt("abcdef");
        assertFalse(hash1.equals(hash2));
    }

    @Test
    public void testLengthOfHash() throws Exception {
        for (String input : inputs) {
            String encryption = crypter.encrypt(input);
            assertEquals(input, 20, encryption.length());
        }
    }

    @Test
    public void testSameInputGivesSameOutput() throws Exception {
        for (String input : inputs) {
            String encryption1 = crypter.encrypt(input);
            String encryption2 = crypter.encrypt(input);
            assertEquals(input, encryption1, encryption2);
        }
    }

    @Test
    public void testAlgorithmSpeed() throws Exception {
        Random generator = new Random();
        int sampleSize = 1000;
        String[] inputs = new String[sampleSize];

        for (int i = 0; i < inputs.length; i++) {
            int passwordSize = generator.nextInt(20) + 1;
            byte[] passwd = new byte[passwordSize];
            generator.nextBytes(passwd);
            inputs[i] = new String(passwd);
        }

        TimeMeasurement measurement = new TimeMeasurement().start();
        for (String input : inputs) {
            crypter.encrypt(input);
        }
        long duration = measurement.elapsed();

        assertTrue(duration < 1000);
    }
}
