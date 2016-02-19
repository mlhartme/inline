/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.sushi.xml;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Create SaxParser- and DocumentBuilder Factories. 
 * Kind of a FactoryFactory to encapsulates xml parser selection.
 * 
 * I have to use jaxp 1.2 (in particular: I cannot use setSchema from jaxp 1.3) because 
 * old Jaxp and Xerces Parsers from JBoss and Tariff/Castor pollute our classpath. 
 * I wasn't able to force Jdk's built-in xerces by directly instantiating the respective 
 * Factory because JBoss didn't find the getSchema method in its endorsed xpi-apis.jar. 
 */
public class Factories {
    private static final Logger LOG = Logger.getLogger(Factories.class.getName());
    
    public static SAXParser saxParser() {
        SAXParserFactory factory;
        
        factory = sax();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        try {
            return factory.newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static SAXParserFactory sax() {
        return SAXParserFactory.newInstance();
    }

    public static DocumentBuilderFactory document() {
        return DocumentBuilderFactory.newInstance(); 
    }

    //--
    
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

}
