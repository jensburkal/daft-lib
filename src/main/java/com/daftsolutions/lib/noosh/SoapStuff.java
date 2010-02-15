/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.noosh;

import com.daftsolutions.lib.xml.Attribute;
import com.daftsolutions.lib.xml.Document;
import com.daftsolutions.lib.xml.Element;
import java.util.HashMap;

/**
 *
 * @author colin
 */
public class SoapStuff {

    public static enum Operations {

        Create
    };
    public final static String NS_SOAP_ENV = "SOAP-ENV";
    public final static String NS_SOAP_ENC = "SOAP-ENC";
    public final static String NS_SOAP_XML = "SOAP-XML";
    public final static String NS_XSD = "xsd";
    public final static String NS_XSI = "xsi";
    public final static String NS_NOOSH = "noosh";
    public final static String NS_PROJECTSERVICE = "ProjectService";
    public final static String URI_SOAP_ENV = "http://schemas.xmlsoap.org/soap/envelope/";
    public final static String URI_SOAP_ENC = "http://schemas.xmlsoap.org/soap/encoding/";
    public final static String URI_SOAP_XML = "http://xml.apache.org/xml-soap";
    public final static String URI_XSD = "http://www.w3.org/2001/XMLSchema";
    public final static String URI_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    public final static String URI_NOOSH = "Noosh";
    public final static String URI_ENCODINGSTYLE = "http://schemas.xmlsoap.org/soap/encoding/";
    public final static String TAG_ENVELOPE = NS_SOAP_ENV + ":Envelope";
    public final static String TAG_HEADER = NS_SOAP_ENV + ":Header";
    public final static String TAG_BODY = NS_SOAP_ENV + ":Body";
    public final static String TAG_AUTHENTICATION = "authentication";
    public final static String TAG_CREATE = "create";
    public final static String TAG_CREATOR = "creator";
    public final static String TAG_PARAM = "param";
    public final static String TAG_DOMAIN = "domain";
    public final static String TAG_IDENTITY = "identity";
    public final static String TAG_MULTIREF = "multiRef";
    public final static String TAG_ITEM = "item";
    public final static String TAG_KEY = "key";
    public final static String TAG_VALUE = "value";
    public final static String TAG_ID = "id";
    public final static String TAG_NAME = "name";
    public final static String TAG_SECONDARYDATA = "secondaryData";
    public final static String TAG_CUSTOMVALUES = "customValues";
    public final static String TAG_DESCRIPTION = "description";
    public final static String ATT_HREF = "href";
    public final static String ATT_ENCODINGSTYLE = "encodingStyle";
    public final static String ATT_DOMAIN = "domain";
    public final static String ATT_IDENTITY = "identity";
    public final static String ATT_SHAREDPASSWORD = "sharedPassword";
    public final static String ATT_TYPE = NS_XSI + ":type";
    public final static String ATT_ROOT = NS_SOAP_ENC + ":root";
    public final static String ATT_ID = "id";
    public final static String ATT_XMLNS = "xmlns";
    public final static String TYPE_MAP = NS_SOAP_XML + ":Map";
    public final static String TYPE_STRING = NS_XSD + ":string";
    public final static String TYPE_LONG = NS_XSD + ":long";
    public final static String TYPE_NOOSH_SERVICEENTITY = NS_NOOSH + ":ServiceEntity";
    public final static String TYPE_NOOSH_PROJECT = NS_NOOSH + ":Project";
    public final static String TYPE_NOOSH_PROJECTDATA = NS_NOOSH + ":ProjectData";

    public SoapStuff() {
        init();
    }

    public void init() {
    }

    public Element makeTypedElement(Element parent, String name, String type, String content) throws Exception {
        Element result = (content == null) ? new Element(parent, name) : new Element(parent, name, content);
        if (type != null) {
            new Attribute(result, ATT_TYPE, type);
        }
        return result;
    }

    /**
     * Build a SOAP message to send to a Noosh server
     * 
     * @param operation
     * @param project
     * @param admin
     * @return
     */
    public Document buildSoapMessage(Operations operation, Project project, NooshAdmin admin) {
        Document result = null;
        try {
            Element e_envelope = createEnvelope();
            Element e_header = createHeader();
            addAuthentication(e_header, admin);
            Element e_body = createBody();
            switch (operation) {
                case Create:
                    addCreateProject(e_body, project, admin);
                    addSecondaryData(e_body, project, admin);
                    addCustomValues(e_body, project, admin);
                    break;
                default:
                    break;
            }

            result = new Document("UTF-8", e_envelope);
            e_envelope.addElement(e_header);
            e_envelope.addElement(e_body);
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /*
     *
    <SOAP-ENV:Envelope SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"
    xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:SOAP-ENC="http://schemas.xmlsoap.org/soap/encoding/"
    xmlns:SOAP-XML="http://xml.apache.org/xml-soap"
    xmlns:noosh="Noosh">
     */
    protected Element createEnvelope() throws Exception {
        Element result = new Element(TAG_ENVELOPE);
        new Attribute(result, NS_SOAP_ENV + ":" + ATT_ENCODINGSTYLE, URI_ENCODINGSTYLE);
        new Attribute(result, ATT_XMLNS + ":" + NS_SOAP_ENV, URI_SOAP_ENV);
        new Attribute(result, ATT_XMLNS + ":" + NS_SOAP_ENC, URI_SOAP_ENC);
        new Attribute(result, ATT_XMLNS + ":" + NS_SOAP_XML, URI_SOAP_XML);
        new Attribute(result, ATT_XMLNS + ":" + NS_XSD, URI_XSD);
        new Attribute(result, ATT_XMLNS + ":" + NS_XSI, URI_XSI);
        new Attribute(result, ATT_XMLNS + ":" + NS_NOOSH, URI_NOOSH);
        return result;
    }

    protected Element createHeader() throws Exception {
        Element result = new Element(TAG_HEADER);
        return result;
    }

    protected Element createBody() throws Exception {
        Element result = new Element(TAG_BODY);
        return result;
    }

    protected Element addAuthentication(Element parent, NooshAdmin admin) throws Exception {
        Element result = new Element(parent, TAG_AUTHENTICATION);
        new Attribute(result, ATT_DOMAIN, admin.getDomain());
        new Attribute(result, ATT_IDENTITY, admin.getIdentity());
        new Attribute(result, ATT_SHAREDPASSWORD, admin.getSharedPassword());
        return result;
    }

    protected Element addCreateProject(Element parent, Project project, NooshAdmin admin) throws Exception {
        // the create tag
        Element result = new Element(parent, TAG_CREATE);
        new Attribute(result, ATT_XMLNS, NS_PROJECTSERVICE);

        // the creator
        Element e_creator = new Element(result, TAG_CREATOR);
        new Attribute(e_creator, ATT_TYPE, TYPE_NOOSH_SERVICEENTITY);
        makeTypedElement(e_creator, TAG_DOMAIN, TYPE_STRING, admin.getDomain());
        makeTypedElement(e_creator, TAG_IDENTITY, TYPE_STRING, project.getIdentity());

        // the params
        Element e_param = new Element(result, TAG_PARAM);
        new Attribute(e_param, ATT_TYPE, TYPE_NOOSH_PROJECT);
        makeTypedElement(e_param, TAG_ID, TYPE_LONG, "-1");
        makeTypedElement(e_param, TAG_NAME, TYPE_STRING, project.getName());
        Element e = new Element(e_param, TAG_SECONDARYDATA);
        new Attribute(e, ATT_HREF, "#id1");
        e = new Element(e_param, TAG_CUSTOMVALUES);
        new Attribute(e, ATT_HREF, "#id2");

        // ok, done
        return result;
    }

    protected Element addSecondaryData(Element parent, Project project, NooshAdmin admin) throws Exception {
        Element result = new Element(parent, TAG_MULTIREF);
        new Attribute(result, ATT_ID, "id1");
        new Attribute(result, ATT_ROOT, "0");
        new Attribute(result, ATT_TYPE, TYPE_NOOSH_PROJECTDATA);
        makeTypedElement(result, TAG_ID, TYPE_LONG, "-1");
        makeTypedElement(result, TAG_DESCRIPTION, TYPE_STRING, project.getDescription());

        // ok, done
        return result;
    }

    protected Element addCustomValues(Element parent, Project project, NooshAdmin admin) throws Exception {
        Element result = new Element(parent, TAG_MULTIREF);
        new Attribute(result, ATT_ID, "id2");
        new Attribute(result, ATT_ROOT, "0");
        new Attribute(result, ATT_TYPE, TYPE_MAP);
        for (SoapItem item : project.getCustomValues()) {
            Element e_item = new Element(result, TAG_ITEM);
            makeTypedElement(e_item, TAG_KEY, TYPE_STRING, item.getKey());
            makeTypedElement(e_item, TAG_VALUE, TYPE_STRING, item.getValue());
        }

        // ok, done
        return result;
    }
}
