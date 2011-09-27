// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilTest {

    @Test
    public void replaceAll() throws Exception {
        assertEquals("my name is Bob, Bob is my name", StringUtil.replaceAll("my name is $name, $name is my name", "$name", "Bob"));
        assertEquals("_$namex_", StringUtil.replaceAll("_$name_", "$name", "$namex"));
    }

    @Test
    public void shouldStripCarriageReturns() throws Exception {
        assertEquals("\n", StringUtil.stripCarriageReturns("\n"));
        assertEquals("\n", StringUtil.stripCarriageReturns("\n\r"));
        assertEquals("\n", StringUtil.stripCarriageReturns("\n\r\r\r\r\r"));
        assertEquals("\n", StringUtil.stripCarriageReturns("\r\n"));
        assertEquals("\n\n", StringUtil.stripCarriageReturns("\r\n\r\n\r\r\r"));
        assertEquals("This\nis\na\nset\nof\nlines.\n",
                StringUtil.stripCarriageReturns("This\n\ris\r\na\nset\r\n\rof\nlines.\n\r"));
    }

}
