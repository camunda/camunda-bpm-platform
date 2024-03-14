/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.client.variable.impl.format.xml;

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import java.beans.Introspector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.spi.DataFormat;
import org.camunda.commons.utils.IoUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DomXmlDataFormat implements DataFormat {

  protected static final DomXmlLogger LOG = ExternalTaskClientLogger.XML_FORMAT_LOGGER;

  protected String name;

  /** the DocumentBuilderFactory used by the reader */
  protected DocumentBuilderFactory documentBuilderFactory;

  /** the TransformerFactory instance used by the writer */
  protected TransformerFactory transformerFactory;

  public DomXmlDataFormat(String name) {
    this(name, defaultDocumentBuilderFactory());
  }

  public DomXmlDataFormat(String name, DocumentBuilderFactory documentBuilderFactory) {
    this(name, documentBuilderFactory, defaultTransformerFactory());
  }

  public DomXmlDataFormat(String name, DocumentBuilderFactory documentBuilderFactory, TransformerFactory transformerFactory) {
    this.name = name;
    this.documentBuilderFactory = documentBuilderFactory;
    LOG.usingDocumentBuilderFactory(documentBuilderFactory.getClass().getName());

    this.transformerFactory = transformerFactory;
  }

  public String getName() {
    return name;
  }

  public DocumentBuilderFactory getDocumentBuilderFactory() {
    return documentBuilderFactory;
  }

  public TransformerFactory getTransformerFactory() {
    return transformerFactory;
  }

  public boolean canMap(Object value) {
    return value != null;
  }

  public String writeValue(Object value) {
    Element mappedObject = writeAsElement(value);

    StringWriter writer = null;

    try {
      writer = new StringWriter();
      StreamResult streamResult = new StreamResult(writer);
      writeResult(streamResult, mappedObject);
      return writer.toString();
    }
    finally {
      IoUtil.closeSilently(writer);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T readValue(String value, String typeIdentifier) {
    ensureNotNull("value", value);
    ensureNotNull("typeIdentifier", typeIdentifier);

    try {
      Class<?> javaClass = loadClass(typeIdentifier, this);
      return (T) readValue(value, javaClass);
    }
    catch (Exception e) {
      throw LOG.unableToDeserialize(value, typeIdentifier, e);
    }
  }

  public <T> T readValue(String value, Class<T> cls) {
    ensureNotNull("value", value);
    ensureNotNull("class", cls);

    try {
      Element xmlNode = readAsElement(value);
      Unmarshaller unmarshaller = createUnmarshaller(cls);
      DOMSource domSource = new DOMSource(xmlNode);
      JAXBElement<T> root = unmarshaller.unmarshal(domSource, cls);
      return root.getValue();
    }
    catch (JAXBException e) {
      throw LOG.unableToDeserialize(value, cls.getCanonicalName(), e);
    }
  }

  public String getCanonicalTypeName(Object value) {
    return value.getClass().getName();
  }

  protected void writeResult(StreamResult streamResult, Object input) {
    Node node = (Node) input;
    DOMSource domSource = new DOMSource(node);
    try {
      getTransformer().transform(domSource, streamResult);
    }
    catch (TransformerException e) {
      throw LOG.unableToTransformElement(node, e);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected Element writeAsElement(Object value) {
    ensureNotNull("value", value);

    final Class<?> valueClass = value.getClass();
    final DOMResult domResult = new DOMResult();

    try {
      Marshaller marshaller = createMarshaller(valueClass);

      boolean isRootElement = valueClass.getAnnotation(XmlRootElement.class) != null;
      if(isRootElement) {
        marshaller.marshal(value, domResult);
      }
      else {
        String simpleName = Introspector.decapitalize(valueClass.getSimpleName());
        JAXBElement<?> root = new JAXBElement(new QName(simpleName), valueClass, value);
        marshaller.marshal(root, domResult);
      }

      Node node = domResult.getNode();
      return ((Document)node).getDocumentElement();
    }
    catch (JAXBException e) {
      throw LOG.unableToWriteInput(value, e);
    }
  }

  protected Transformer getTransformer() {
    try {
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      return transformer;
    }
    catch (TransformerConfigurationException e) {
      throw LOG.unableToCreateTransformer(e);
    }
  }

  public Element readAsElement(String value) {
    DocumentBuilder documentBuilder = getDocumentBuilder();
    StringReader stringReader = null;
    BufferedReader bufferedReader = null;

    try {
      stringReader = new StringReader(value);
      bufferedReader = new BufferedReader(stringReader);
      InputSource inputSource = new InputSource(bufferedReader);

      LOG.parsingInput();
      Document document = documentBuilder.parse(inputSource);
      return document.getDocumentElement();
    }
    catch (SAXException e) {
      throw LOG.unableToParseInput(e);
    }
    catch (IOException e) {
      throw LOG.unableToParseInput(e);

    }
    finally{
      IoUtil.closeSilently(bufferedReader);
      IoUtil.closeSilently(stringReader);
    }
  }

  protected DocumentBuilder getDocumentBuilder() {
    try {
      DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
      LOG.createdDocumentBuilder();

      return docBuilder;
    }
    catch (ParserConfigurationException e) {
      throw LOG.unableToCreateParser(e);

    }
  }

  protected JAXBContext getContext(Class<?>... types) {
    try {
      return JAXBContext.newInstance(types);
    }
    catch (JAXBException e) {
      throw LOG.unableToCreateContext(e);
    }
  }

  protected Marshaller createMarshaller(Class<?>... types) {
    try {
      return getContext(types).createMarshaller();
    }
    catch (JAXBException e) {
      throw LOG.unableToCreateMarshaller(e);
    }
  }

  protected Unmarshaller createUnmarshaller(Class<?>... types) {
    try {
      return getContext(types).createUnmarshaller();
    }
    catch (JAXBException e) {
      throw LOG.unableToCreateUnmarshaller(e);
    }
  }

  public static TransformerFactory defaultTransformerFactory() {
    return TransformerFactory.newInstance();
  }

  public static DocumentBuilderFactory defaultDocumentBuilderFactory() {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    documentBuilderFactory.setNamespaceAware(true);
    LOG.documentBuilderFactoryConfiguration("namespaceAware", "true");

    documentBuilderFactory.setValidating(false);
    LOG.documentBuilderFactoryConfiguration("validating", "false");

    documentBuilderFactory.setIgnoringComments(true);
    LOG.documentBuilderFactoryConfiguration("ignoringComments", "true");

    documentBuilderFactory.setIgnoringElementContentWhitespace(false);
    LOG.documentBuilderFactoryConfiguration("ignoringElementContentWhitespace", "false");

    return documentBuilderFactory;
  }

  public static Class<?> loadClass(String classname, DataFormat dataFormat) {

    // first try context classoader
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if(cl != null) {
      try {
        return cl.loadClass(classname);
      }
      catch(Exception e) {
        // ignore
      }
    }

    // else try the classloader which loaded the dataformat
    cl = dataFormat.getClass().getClassLoader();
    try {
      return cl.loadClass(classname);
    }
    catch (ClassNotFoundException e) {
      throw LOG.classNotFound(classname, e);
    }
  }

}
