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
package org.camunda.bpm.model.xml.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.ModelParseException;
import org.camunda.bpm.model.xml.impl.ModelInstanceImpl;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Helper methods which abstract some gruesome DOM specifics.
 *
 * @author Daniel Meyer
 * @author Sebastian Menski
 *
 */
public class DomUtil {

  /**
   * A {@link NodeListFilter} allows to filter a {@link NodeList},
   * retaining only elements in the list which match the filter.
   *
   * @see DomUtil#filterNodeList(NodeList, NodeListFilter)
   */
  public static interface NodeListFilter<T extends Node> {

    /**
     * Test if node matches the filter
     *
     * @param node the node to match
     * @return true if the filter does match the node, false otherwise
     */
    public boolean matches(Node node);

  }

  /**
   * Filter retaining only Nodes of type {@link Node#ELEMENT_NODE}
   *
   */
  public static class ElementNodeListFilter implements NodeListFilter<Element> {

    public boolean matches(Node node) {
      return node.getNodeType() == Node.ELEMENT_NODE;
    }

  }

  /**
   * Filters {@link Element Elements} by their nodeName + namespaceUri
   *
   */
  public static class ElementByNameListFilter extends ElementNodeListFilter {

    final String localName;
    final String namespaceUri;

    /**
     * @param localName the local name to filter for
     * @param namespaceUri the namespaceUri to filter for
     */
    public ElementByNameListFilter(String localName, String namespaceUri) {
      this.localName = localName;
      this.namespaceUri = namespaceUri;
    }

    @Override
    public boolean matches(Node node) {
     return super.matches(node)
        && localName.equals(node.getLocalName())
        && namespaceUri.equals(node.getNamespaceURI());
    }

  }

  public static class ElementByTypeListFilter extends ElementNodeListFilter {

    final Class<?> type;
    final ModelInstanceImpl model;

    public ElementByTypeListFilter(Class<?> type, ModelInstanceImpl modelInstance) {
      this.type =  type;
      this.model = modelInstance;
    }

    @Override
    public boolean matches(Node node) {
      if (! super.matches(node)) {
        return false;
      }
      ModelElementInstance modelElement = ModelUtil.getModelElement((Element) node, model);
      return type.isAssignableFrom(modelElement.getClass());
    }
  }

  /**
   * Allows to apply a {@link NodeListFilter} to a {@link NodeList}. This allows to remove all elements from a node list which do not match the Filter.
   *
   * @param nodeList the {@link NodeList} to filter
   * @param filter the {@link NodeListFilter} to apply to the {@link NodeList}
   * @return the List of all Nodes which match the filter
   */
  @SuppressWarnings("unchecked")
  public static <T extends Node> List<T> filterNodeList(NodeList nodeList, NodeListFilter<T> filter) {

    List<T> filteredList = new ArrayList<T>();
    for(int i = 0; i< nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if(filter.matches(node)) {
        filteredList.add((T) node);
      }
    }

    return filteredList;

  }

  /**
   * Filter a {@link NodeList} retaining all elements with a specific name
   *
   * @param nodeList the {@link NodeList} to filter
   * @param localName the local element name to filter for
   * @param namespaceUri the namespace for the elements
   * @return the List of all Elements which match the filter
   */
  public static List<Element> filterNodeListByName(NodeList nodeList, String localName, String namespaceUri) {
    return filterNodeList(nodeList, new ElementByNameListFilter(localName, namespaceUri));
  }

  /**
   * Filter a {@link NodeList} retaining all elements with a specific type
   *
   * @param nodeList  the {@link NodeList} to filter
   * @param type  the type class to filter for
   * @param modelInstance  the model instance
   * @return the list of all Elements which match the filter
   */
  public static List<Element> filterNodeListByType(NodeList nodeList, Class<?> type, ModelInstanceImpl modelInstance) {
    return filterNodeList(nodeList, new ElementByTypeListFilter(type, modelInstance));
  }

  /**
   * Returns the Document element for a Document
   *
   * @param document the document to retrieve the element for
   * @return the Element
   */
  public static Element getDocumentElement(Document document) {
    return document.getDocumentElement();
  }

  /**
   * Set the document element of DOM document. Replace an existing if necessary
   *
   * @param document the DOM document to set the document element
   * @param domElement the new document element
   */
  public static void setDocumentElement(Document document, Element domElement) {
    Element existingDocumentElement = getDocumentElement(document);
    if(existingDocumentElement != null) {
      document.replaceChild(domElement, existingDocumentElement);
    }
    else {
      document.appendChild(domElement);
    }
  }

  /**
   * Get all child nodes of a DOM element
   *
   * @param domElement the DOM element to get the child nodes for
   * @return the list of all child nodes
   */
  public static NodeList getChildNodes(Element domElement) {
    return domElement.getChildNodes();
  }

  /**
   * Remove a child element of a DOM element
   *
   * @param domElement the DOM element to remove the child from
   * @param element the child element to remove
   * @return true if the child was removed, else false
   */
  public static boolean removeChild(Element domElement, Element element) {
    try {
      domElement.removeChild(element);
      return true;

    } catch(DOMException e) {
      return false;
    }
  }

  /**
   * Get the namespace URI of a DOM element
   *
   * @param domElement the DOM element to get the URI for
   * @return the namespace URI of the element
   */
  public static String getNamespaceUri(Element domElement) {
    return domElement.getNamespaceURI();
  }

  /**
   * Get an empty DOM document
   *
   * @param documentBuilderFactory the factory to build to DOM document
   * @return the new empty document
   * @throws ModelParseException if unable to create a new document
   */
  public static Document getEmptyDocument(DocumentBuilderFactory documentBuilderFactory) {
    DocumentBuilder documentBuilder;
    try {
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
      return documentBuilder.newDocument();
    } catch (ParserConfigurationException e) {
      throw new ModelParseException("Unable to create a new document", e);
    }
  }

  /**
   * Create a new DOM document from the input stream
   *
   * @param documentBuilderFactory the factory to build to DOM document
   * @param inputStream the input stream to parse
   * @return the new DOM document
   * @throws ModelParseException if a parsing or IO error is triggered
   */
  public static Document parseInputStream(DocumentBuilderFactory documentBuilderFactory, InputStream inputStream) {
    try {
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      return documentBuilder.parse(inputStream);

    } catch (ParserConfigurationException e) {
      throw new ModelParseException("ParserConfigurationException while parsing input stream", e);

    } catch (SAXException e) {
      throw new ModelParseException("SAXException while parsing input stream", e);

    } catch (IOException e) {
      throw new ModelParseException("IOException while parsing input stream", e);

    }
  }


  /**
   * Returns the value for an attribute or 'null' if no such attribute exists.
   *
   * @param attributeName the name of the attribute to return the value for
   * @param domElement the element to get the attribute from
   * @return the value or 'null' if no such attribute exists
   */
  public static String getAttributeValue(String attributeName, Element domElement) {
    if(domElement.hasAttribute(attributeName)) {
      return domElement.getAttribute(attributeName);
    } else {
      return null;
    }
  }

  /**
   * Returns the value for an attribute or 'null' if no such attribute exists.
   *
   * @param attributeName the name of the attribute to return the value for
   * @param namespaceUri the namespace URI of the attribute
   * @param domElement the element to get the attribute from
   * @return the value or 'null' if no such attribute exists
   */
  public static String getAttributeValueNs(String attributeName, String namespaceUri, Element domElement) {
    if(domElement.hasAttributeNS(namespaceUri, attributeName)) {
      return domElement.getAttributeNS(namespaceUri, attributeName);
    } else {
      return null;
    }
  }

  /**
   * Sets the value for an attribute
   *
   * @param attributeName the name of the attribute to set the value for
   * @param xmlValue the value to set
   * @param domElement the DOM element to set the value on
   */
  public static void setAttributeValue(String attributeName, String xmlValue, Element domElement) {
    domElement.setAttribute(attributeName, xmlValue);
  }

  /**
   * Sets the value for an attribute
   *
   * @param attributeName the name of the attribute to set the value for
   * @param namespaceUri the namespace URI
   * @param xmlValue the value to set
   * @param domElement the DOM element to set the value on
   */
  public static void setAttributeValueNs(String attributeName, String namespaceUri, String xmlValue, Element domElement) {
    domElement.setAttributeNS(namespaceUri, attributeName, xmlValue);
  }

  /**
   * Find an element by Id
   *
   * @param id the id of the element to find
   * @param document the DOM document to search
   * @return the element or null if no such element exists
   */
  public static Element findElementById(Document document, String id) {
    return document.getElementById(id);
  }

  /**
   * Get the document for a DOM element
   * @param domNode the element to get the document for
   * @return the Document for a DOM element
   */
  public static Document getDocument(Node domNode) {
    return domNode.getOwnerDocument();
  }

  /**
   * Returns the namespace URI for the given prefix.
   *
   * @param prefix
   *          the prefix to resolve
   * @param scope
   *          the node from which the prefix should be resolved. The DOM
   *          implementation will start from this element and recursively check
   *          the parents of this node for a namespace declaration. returns the
   *          namespace URI for the given prefix
   * @return the resolved namespace URI
   */
  public static String getNamespaceUriForPrefix(Node scope, String prefix) {
    return scope.lookupNamespaceURI(prefix);
  }

  /**
   * Set the id property of an attribute by name
   *
   * @param domElement the DOM element of the attribute
   * @param attributeName the attribute name which is a id attribute
   */
  public static void setIdAttribute(Element domElement, String attributeName) {
    domElement.setIdAttribute(attributeName, true);
  }

  /**
   * Set the id property of an attribute by name and namespace URI
   *
   * @param domElement the DOM element of the attribute
   * @param attributeName the attribute name which is a id attribute
   * @param namespaceUri the namespace for the element
   */
  public static void setIdAttributeNs(Element domElement, String attributeName, String namespaceUri) {
    domElement.setIdAttributeNS(namespaceUri, attributeName, true);
  }

  /**
   * Remove an attribute from a DOM element by name
   *
   * @param domElement the DOM element of the attribute
   * @param attributeName the attribute name which should be removed
   */
  public static void removeAttribute(Element domElement, String attributeName) {
    domElement.removeAttribute(attributeName);
  }

  /**
   * Remove an attribute from a DOM element by name and namespace URI
   *
   * @param domElement the DOM element of the attribute
   * @param attributeName the attribute name which should be removed
   * @param namespaceUri the namespace URI of the attribute
   */
  public static void removeAttributeNs(Element domElement, String attributeName, String namespaceUri) {
    domElement.removeAttributeNS(namespaceUri, attributeName);
  }

  /**
   * Find all elements in a DOM document by name and namespace
   *
   * @param document the DOM document to search
   * @param typeName the name of the element type
   * @param typeNamespace the namespace of the element type
   * @return the list of matching DOM elements
   */
  public static List<Element> findElementByNameNs(Document document, String typeName, String typeNamespace) {
    NodeList elementList = document.getElementsByTagNameNS(typeNamespace, typeName);
    return filterNodeList(elementList, new ElementNodeListFilter());
  }

  /**
   * Get the text content of a DOM element
   *
   * @param domElement the DOM element to get the text content for
   * @return the text content of the DOM element and its descendants
   */
  public static String getTextContent(Element domElement) {
    return domElement.getTextContent();
  }

  /**
   * Get the text content of a DOM element
   *
   * @param domElement the DOM element to set the text content for
   * @param textContent the text content to set
   */
  public static void setTextContent(Element domElement, String textContent) {
    domElement.setTextContent(textContent);
  }


  /**
   * Get parent node of DOM element
   *
   * @param domElement the DOM element to find the parent for
   * @return the parent of the DOM element
   */
  public static Element getParentElement(Element domElement) {
    return (Element) domElement.getParentNode();
  }

}
