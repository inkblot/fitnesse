// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import org.w3c.dom.*;

import java.io.*;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class XmlWriter extends Writer {
    private static String endl = System.getProperty("line.separator");

    private Writer writer;
    private boolean isNewLine;

    public XmlWriter(OutputStream os) {
        try {
            writer = new OutputStreamWriter(os, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
    }

    public void write(Document doc) throws IOException {
        write("<?xml version=\"1.0\"?>");
        write(endl);
        write(doc.getDocumentElement(), 0);
    }

    public void write(NodeList nodes) throws IOException {
        write(nodes, 0);
    }

    public void write(Element element, int tabs) throws IOException {
        if (!isNewLine)
            write(endl);
        if (!element.hasChildNodes()) {
            writeTabs(tabs);
            write("<" + element.getTagName() + writeAttributes(element) + "/>");
        } else {
            writeTabs(tabs);
            write("<" + element.getTagName() + writeAttributes(element) + ">");
            write(element.getChildNodes(), tabs + 1);
            if (isNewLine)
                writeTabs(tabs);
            write("</" + element.getTagName() + ">");
        }
        write(endl);
    }

    private String writeAttributes(Element element) {
        StringBuilder attributeString = new StringBuilder();
        NamedNodeMap attributeMap = element.getAttributes();
        int length = attributeMap.getLength();
        for (int i = 0; i < length; i++) {
            Attr attributeNode = (Attr) attributeMap.item(i);
            String name = attributeNode.getName();
            String value = attributeNode.getValue();
            attributeString.append(" ").append(name).append("=\"").append(value).append("\"");
        }
        return attributeString.toString();
    }

    private void write(NodeList nodes, int tabs) throws IOException {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            write(node, tabs);
        }
    }

    private void writeText(Text text) throws IOException {
        String nodeValue = text.getNodeValue();
        write(nodeValue.trim());
    }

    private void writeCdata(CDATASection cData) throws IOException {
        String cDataText = "<![CDATA[" + cData.getNodeValue() + "]]>";
        write(cDataText);
    }

    private void write(Node node, int tabs) throws IOException {
        if (node instanceof Element)
            write((Element) node, tabs);
        else if (node instanceof CDATASection)
            writeCdata((CDATASection) node);
        else if (node instanceof Text)
            writeText((Text) node);
        else
            throw new IOException("XmlWriter: unsupported node type: " + node.getClass());
    }

    private void writeTabs(int tabs) throws IOException {
        for (int i = 0; i < tabs; i++)
            write("\t");
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
    }

    public void write(String value) throws IOException {
        if (isEmpty(value)) {
            return;
        }
        isNewLine = endl.equals(value);
        writer.write(value);
    }

    public void flush() throws IOException {
        writer.flush();
    }

    public void close() throws IOException {
        writer.close();
    }
}
