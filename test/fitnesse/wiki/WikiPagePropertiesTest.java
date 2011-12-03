// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import util.Clock;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static org.junit.Assert.*;

public class WikiPagePropertiesTest extends WikiBaseTestCase {
    private WikiPageProperties properties;

    static String endl = System.getProperty("line.separator"),
            tab = "\t";
    static String sampleXml =
            "<?xml version=\"1.0\"?>" + endl +
                    "<properties>" + endl +
                    tab + "<Edit/>" + endl +
                    tab + "<ParentOne>" + endl +
                    tab + tab + "<ChildOne>child one value</ChildOne>" + endl +
                    tab + "</ParentOne>" + endl +
                    tab + "<ParentTwo value=\"parent 2 value\">" + endl +
                    tab + tab + "<ChildTwo>child two value</ChildTwo>" + endl +
                    tab + "</ParentTwo>" + endl +
                    tab + "<SymbolicLinks>" + endl +
                    tab + tab + "<BackLink>&lt;BackWard.SymLink</BackLink>" + endl +
                    tab + tab + "<RelLink>RelaTive.SymLink</RelLink>" + endl +
                    tab + tab + "<AbsLink>.AbsoLute.SymLink</AbsLink>" + endl +
                    tab + tab + "<SubLink>&gt;SubChild.SymLink</SubLink>" + endl +
                    tab + "</SymbolicLinks>" + endl +
                    tab + "<Test/>" + endl +
                    "</properties>" + endl;
    static String[] sampleXmlFragments = sampleXml.split("\t*" + endl);

    private Clock clock;

    @Inject
    public void inject(Clock clock) {
        this.clock = clock;
    }

    @Before
    public void setUp() throws Exception {
        InputStream sampleInputStream = new ByteArrayInputStream(sampleXml.getBytes());
        properties = new WikiPageProperties(sampleInputStream);
    }

    @Test
    public void testLoadingOfXmlWithoutAddedSpaces() throws Exception {
        validateLoading();
    }

    private void validateLoading() throws Exception {
        assertTrue(properties.has("Edit"));
        assertTrue(properties.has("Test"));
        assertFalse(properties.has("Suite"));

        WikiPageProperty parentOne = properties.getProperty("ParentOne");
        assertEquals(null, parentOne.getValue());
        assertEquals("child one value", parentOne.get("ChildOne"));

        WikiPageProperty parentTwo = properties.getProperty("ParentTwo");
        assertEquals("parent 2 value", parentTwo.getValue());
        assertEquals("child two value", parentTwo.get("ChildTwo"));

        WikiPageProperty symbolics = properties.getProperty("SymbolicLinks");
        assertEquals("<BackWard.SymLink", symbolics.get("BackLink"));
        assertEquals("RelaTive.SymLink", symbolics.get("RelLink"));
        assertEquals(".AbsoLute.SymLink", symbolics.get("AbsLink"));
        assertEquals(">SubChild.SymLink", symbolics.get("SubLink"));
    }

    @Test
    public void testSave() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1000);
        properties.save(os);

        String xml = os.toString();
        for (String fragment : sampleXmlFragments) {
            assertTrue(fragment, xml.contains(fragment));
        }
    }

    @Test
    public void testKeySet() throws Exception {
        properties = new WikiPageProperties();
        properties.set("one");
        properties.set("two");
        properties.set("three");
        Set<?> keys = properties.keySet();

        assertTrue(keys.contains("one"));
        assertTrue(keys.contains("two"));
        assertTrue(keys.contains("three"));
        assertFalse(keys.contains("four"));
    }

    @Test
    public void testIsSerializable() throws Exception {
        try {
            new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(properties);
        } catch (NotSerializableException e) {
            fail("its not serializable: " + e);
        }
    }

    @Test
    public void testLastModificationTime() throws Exception {
        SimpleDateFormat format = WikiPageProperty.getTimeFormat();
        WikiPageProperties props = new WikiPageProperties();
        assertEquals(format.format(clock.currentClockDate()), format.format(props.getLastModificationTime()));
        Date date = format.parse("20040101000001");
        props.setLastModificationTime(date);
        assertEquals("20040101000001", props.get(PageData.PropertyLAST_MODIFIED));
        assertEquals(date, props.getLastModificationTime());
    }

    @Test
    public void testShouldRemoveSpacesFromPropertyValues() throws Exception {
        validateLoading();
    }
}
