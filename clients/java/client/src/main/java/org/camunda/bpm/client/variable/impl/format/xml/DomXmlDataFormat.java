/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.camunda.bpm.client.spi.DataFormat;
import org.camunda.commons.utils.IoUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class DomXmlDataFormat implements DataFormat {

  /** the DocumentBuilderFactory used by the reader */
  protected DocumentBuilderFactory documentBuilderFactory;

  /** the TransformerFactory instance used by the writer */
  protected TransformerFactory transformerFactory;

  public DomXmlDataFormat() {
    this(defaultDocumentBuilderFactory());
  }

  public DomXmlDataFormat(DocumentBuilderFactory documentBuilderFactory) {
    this(documentBuilderFactory, defaultTransformerFactory());
  }

  public DomXmlDataFormat(DocumentBuilderFactory documentBuilderFactory, TransformerFactory transformerFactory) {
    this.documentBuilderFactory = documentBuilderFactory;
    this.transformerFactory = transformerFactory;
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

  public String writeValue(Object value) throws Exception {
    Element mappedObject = writeAsElement(value);

    StringWriter writer = null;

    try {
      writer = new StringWriter();
      StreamResult streamReseult = new StreamResult(writer);
      writeResult(streamReseult, mappedObject);
      return writer.toString();
    }
    finally {
      IoUtil.closeSilently(writer);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T readValue(String value, String typeIdentifier) throws Exception {
    ensureNotNull("value", value);
    ensureNotNull("typeIdentifier", typeIdentifier);

    Class<?> javaClass = loadClass(typeIdentifier, this);
    return (T) readValue(value, javaClass);
  }

  public <T> T readValue(String value, Class<T> cls) throws Exception {
    ensureNotNull("value", value);
    ensureNotNull("class", cls);

    Element xmlNode = readInput(value);
    Unmarshaller unmarshaller = createUnmarshaller(cls);
    DOMSource domSource = new DOMSource(xmlNode);
    JAXBElement<T> root = unmarshaller.unmarshal(domSource, cls);
    return root.getValue();
  }

  public String getCanonicalTypeName(Object value) {
    return value.getClass().getName();
  }

  protected void writeResult(StreamResult streamResult, Object input) throws Exception{
    Node node = (Node) input;
    DOMSource domSource = new DOMSource(node);
    getTransformer().transform(domSource, streamResult);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected Element writeAsElement(Object value) throws Exception {
    ensureNotNull("value", value);

    final Class<?> valueClass = value.getClass();
    final DOMResult domResult = new DOMResult();

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

  protected Transformer getTransformer() {
    try {
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      return transformer;
    }
    catch (TransformerConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public Element readInput(String value) throws Exception {
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    StringReader stringReader = null;
    BufferedReader bufferedReader = null;

    try {
      stringReader = new StringReader(value);
      bufferedReader = new BufferedReader(stringReader);
      InputSource inputSource = new InputSource(bufferedReader);

      Document document = documentBuilder.parse(inputSource);
      return document.getDocumentElement();
    }
    finally{
      IoUtil.closeSilently(bufferedReader);
      IoUtil.closeSilently(stringReader);
    }
  }

  protected JAXBContext getContext(Class<?>... types) {
    try {
      return JAXBContext.newInstance(types);
    }
    catch (JAXBException e) {
//      throw LOG.unableToCreateContext(e);
      throw new RuntimeException(e);
    }
  }

  protected Marshaller createMarshaller(Class<?>... types) {
    try {
      return getContext(types).createMarshaller();
    } catch (JAXBException e) {
//      throw LOG.unableToCreateMarshaller(e);
      throw new RuntimeException(e);
    }
  }

  protected Unmarshaller createUnmarshaller(Class<?>... types) {
    try {
      return getContext(types).createUnmarshaller();
    } catch (JAXBException e) {
//      throw LOG.unableToCreateUnmarshaller(e);
      throw new RuntimeException(e);
    }
  }

  public static TransformerFactory defaultTransformerFactory() {
    return TransformerFactory.newInstance();
  }

  public static DocumentBuilderFactory defaultDocumentBuilderFactory() {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    documentBuilderFactory.setValidating(false);
    documentBuilderFactory.setIgnoringComments(true);
    documentBuilderFactory.setIgnoringElementContentWhitespace(false);

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
      throw new RuntimeException(e);
    }
  }

}
