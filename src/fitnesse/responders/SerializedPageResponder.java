// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseModule;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import org.w3c.dom.Document;
import util.XmlWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class SerializedPageResponder implements SecureResponder {
    private XmlizePageCondition xmlizePageCondition = new XmlizePageCondition() {
        public boolean canBeXmlized(WikiPage page) throws Exception {
            return !(page instanceof SymbolicPage);
        }
    };
    private final HtmlPageFactory htmlPageFactory;
    private final WikiPage root;

    @Inject
    public SerializedPageResponder(HtmlPageFactory htmlPageFactory, @Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
    }

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        WikiPage page = getRequestedPage(request, root);
        if (page == null)
            return new NotFoundResponder(htmlPageFactory).makeResponse(context, request);

        if ("pages".equals(request.getInput("type"))) {
            PageXmlizer pageXmlizer = new PageXmlizer();
            pageXmlizer.addPageCondition(xmlizePageCondition);
            Document doc = pageXmlizer.xmlize(page);
            return makeResponseWithXml(doc);
        } else if ("data".equals(request.getInput("type"))) {
            Document doc = new PageXmlizer().xmlize(page.getData());
            return makeResponseWithXml(doc);
        } else {
            Object object = getObjectToSerialize(request, page);
            byte[] bytes = serializeToBytes(object);
            return responseWith(bytes);
        }
    }

    private SimpleResponse makeResponseWithXml(Document doc) throws Exception {
        //TODO MdM Should probably use a StreamedResponse
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XmlWriter xmlWriter = new XmlWriter(output);
        xmlWriter.write(doc);
        xmlWriter.flush();
        xmlWriter.close();
        SimpleResponse response = new SimpleResponse();
        response.setContentType("text/xml");
        response.setContent(output.toByteArray());
        return response;
    }

    private Object getObjectToSerialize(Request request, WikiPage page) throws Exception {
        Object object;
        if ("bones".equals(request.getInput("type")))
            object = new ProxyPage(page, page.getInjector());
        else if ("meat".equals(request.getInput("type"))) {
            PageData originalData = page.getData();
            if (request.hasInput("version"))
                originalData = page.getDataVersion((String) request.getInput("version"));

            object = new PageData(originalData);
        } else
            throw new Exception("Improper use of proxy retrieval");
        return object;
    }

    private WikiPage getRequestedPage(Request request, WikiPage root) throws Exception {
        String resource = request.getResource();
        WikiPagePath path = PathParser.parse(resource);
        return root.getPageCrawler().getPage(root, path);
    }

    private SimpleResponse responseWith(byte[] bytes) {
        SimpleResponse response = new SimpleResponse();
        response.setContentType("application/octet-stream");
        response.setContent(bytes);
        return response;
    }

    private byte[] serializeToBytes(Object object) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(byteStream);
        os.writeObject(object);
        os.close();
        return byteStream.toByteArray();
    }

    public SecureOperation getSecureOperation() {
        return new SecureReadOperation();
    }

}
