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
package org.camunda.spin.xml;

import java.io.Writer;
import java.util.Collection;
import java.util.List;

import org.camunda.spin.SpinList;

/**
 * An element in a tree-oriented XML data format.
 *
 * @author Sebastian Menski
 * @author Daniel Meyer
 *
 */
public abstract class SpinXmlElement extends SpinXmlNode<SpinXmlElement> {

  /**
   * Checks whether this element has a attribute with an empty namespace and the given name.
   *
   * @param attributeName the name of the attribute
   * @return true if the element has an attribute with this name under the local namespace, false otherwise
   * @throws IllegalArgumentException if the attributeName is null
   */
  public abstract boolean hasAttr(String attributeName);

  /**
   * Returns the wrapped attribute for an empty namespace and the given name.
   *
   * @param attributeName the name of the attribute
   * @return the wrapped {@link SpinXmlAttribute attribute}
   * @throws IllegalArgumentException if the attributeName is null
   * @throws SpinXmlAttributeException if the attribute is not found
   */
  public abstract SpinXmlAttribute attr(String attributeName);

  /**
   * Sets the attribute value in the local namespace of this element.
   *
   * @param attributeName the name of the attribute
   * @param value the value to set
   * @return the wrapped {@link SpinXmlElement element}
   * @throws IllegalArgumentException if the attributeName or value is null
   */
  public abstract SpinXmlElement attr(String attributeName, String value);

  /**
   * Removes the attribute with an empty namespace.
   *
   * @param attributeName the name of the attribute
   * @return the wrapped {@link SpinXmlElement element}
   * @throws IllegalArgumentException if the attributeName is null
   */
  public abstract SpinXmlElement removeAttr(String attributeName);

  /**
   * Checks whether this element has a attribute with the given namespace and name.
   *
   * @param namespace the namespace of the attribute
   * @param attributeName the name of the attribute
   * @return true if the element has an attribute with this name under given namespace, false otherwise
   * @throws IllegalArgumentException if the attributeName is null
   */
  public abstract boolean hasAttrNs(String namespace, String attributeName);

  /**
   * Returns the wrapped attribute for the given namespace and name.
   *
   * @param namespace the namespace of the attribute
   * @param attributeName the name of the attribute
   * @return the wrapped {@link SpinXmlAttribute attribute}
   * @throws IllegalArgumentException if attributeName or value is null
   * @throws SpinXmlElementImplementationException if the attribute cannot be set in the underlying implementation
   */
  public abstract SpinXmlAttribute attrNs(String namespace, String attributeName);

  /**
   * Sets the attribute value in the given namespace.
   *
   * @param namespace the namespace of the attribute
   * @param attributeName the name of the attribute
   * @param value the value to set
   * @return the wrapped {@link SpinXmlElement element}
   * @throws IllegalArgumentException if attributeName or value is null
   * @throws SpinXmlElementImplementationException if the attribute cannot be set in the underlying implementation
   */
  public abstract SpinXmlElement attrNs(String namespace, String attributeName, String value);

  /**
   * Removes the attribute under the given namespace.
   *
   * @param namespace the namespace of the attribute
   * @param attributeName the name of the attribute
   * @return the wrapped {@link SpinXmlElement element}
   * @throws IllegalArgumentException if the attributeName is null
   */
  public abstract SpinXmlElement removeAttrNs(String namespace, String attributeName);

  /**
   * Returns all wrapped attributes of this element.
   *
   * @return the wrapped attributes or an empty list of no attributes are found
   */
  public abstract SpinList<SpinXmlAttribute> attrs();

  /**
   * Returns all wrapped attributes for the given namespace.
   *
   * @param namespace the namespace of the attributes
   * @return the wrapped attributes or an empty list of no attributes are found
   */
  public abstract SpinList<SpinXmlAttribute> attrs(String namespace);

  /**
   * Returns all names of the attributes of this element.
   *
   * @return the names of the attributes
   */
  public abstract List<String> attrNames();

  /**
   * Returns all names of the attributes in the given namespace.
   *
   * @return the names of the attributes
   */
  public abstract List<String> attrNames(String namespace);

  /**
   * Returns the text content of an element.
   *
   * @return the text content or an empty string if non exists
   */
  public abstract String textContent();

  /**
   * Sets the text content of an element.
   * @param textContent the text content to set
   * @return the wrapped {@link SpinXmlElement element}
   * @throws IllegalArgumentException if the textContent is null
   */
  public abstract SpinXmlElement textContent(String textContent);

  /**
   * Returns a single wrapped child element for the given name
   * in the local namespace of this element.
   *
   * @param elementName the element name
   * @return the wrapped child {@link SpinXmlElement element}
   * @throws IllegalArgumentException if the elementName is null
   * @throws SpinXmlElementException if none or more than one child element is found
   */
  public abstract SpinXmlElement childElement(String elementName);

  /**
   * Returns a single wrapped child {@link SpinXmlElement element} for the given namespace
   * and name.
   *
   * @param namespace the namespace of the element
   * @param elementName the element name
   * @return the wrapped child {@link SpinXmlElement element}
   * @throws IllegalArgumentException if the elementName is null
   * @throws SpinXmlElementException if none or more than one child element is found
   */
  public abstract SpinXmlElement childElement(String namespace, String elementName);

  /**
   * Returns all child elements of this {@link SpinXmlElement elements}.
   *
   * @return list of wrapped child {@link SpinXmlElement elements}
   */
  public abstract SpinList<SpinXmlElement> childElements();

  /**
   * Returns all child {@link SpinXmlElement elements} with a given name in the local namespace
   * of this element.
   *
   * @param elementName the element name
   * @return a collection of wrapped {@link SpinXmlElement elements}
   * @throws IllegalArgumentException if the element name is null
   * @throws SpinXmlElementException if no child element was found
   */
  public abstract SpinList<SpinXmlElement> childElements(String elementName);

  /**
   * Returns all child {@link SpinXmlElement elements} with a given namespace and name.
   *
   * @param namespace the namespace of the element
   * @param elementName the element name
   * @return a collection of wrapped {@link SpinXmlElement elements}
   * @throws IllegalArgumentException if the element name is null
   * @throws SpinXmlElementException if no child element was found
   */
  public abstract SpinList<SpinXmlElement> childElements(String namespace, String elementName);

  /**
   * Appends child elements to this {@link SpinXmlElement element}.
   *
   * @param childElements the child elements to append
   * @return the wrapped {@link SpinXmlElement element}
   * @throws IllegalArgumentException if the childElements is null or one of them
   * @throws SpinXmlElementImplementationException if a child element cannot be appended in the underlying implementation
   */
  public abstract SpinXmlElement append(SpinXmlElement... childElements);

  /**
   * Appends child elements to this {@link SpinXmlElement element}.
   *
   * @param childElements the child elements to append
   * @return the wrapped {@link SpinXmlElement element}
   * @throws IllegalArgumentException if the childElements is null or one of them
   * @throws SpinXmlElementImplementationException if a child element cannot be appended in the underlying implementation
   */
  public abstract SpinXmlElement append(Collection<SpinXmlElement> childElements);

  /**
   * Appends a child element to this element before the existing child element.
   *
   * @param childElement the child element to append
   * @param existingChildElement the child element to append before
   * @return the wrapped {@link SpinXmlElement element}
   * @throws IllegalArgumentException if the child element or existing child element is null
   * @throws SpinXmlElementException if the existing child element is not a child of this element
   * @throws SpinXmlElementImplementationException if the new child element cannot be inserted in the underlying implementation
   */
  public abstract SpinXmlElement appendBefore(SpinXmlElement childElement, SpinXmlElement existingChildElement);

  /**
   * Appends a child element to this {@link SpinXmlElement element} after the existing child {@link SpinXmlElement element}.
   *
   * @param childElement the child element to append
   * @param existingChildElement the child element to append after
   * @return the wrapped {@link SpinXmlElement element}
   * @throws IllegalArgumentException if the child element or existing child element is null
   * @throws SpinXmlElementException if the existing child element is not a child of this element
   * @throws SpinXmlElementImplementationException if the new child element cannot be inserted in the underlying implementation
   */
  public abstract SpinXmlElement appendAfter(SpinXmlElement childElement, SpinXmlElement existingChildElement);

  /**
   * Removes all child elements from this element.
   *
   * @param childElements the child elements to remove
   * @return the wrapped {@link SpinXmlElement element}
   * @throws IllegalArgumentException if child elements is null or any of them
   * @throws SpinXmlElementException if one of the child elements does not exist
   * @throws SpinXmlElementImplementationException if the child element cannot be removed in the underlying implementation
   */
  public abstract SpinXmlElement remove(SpinXmlElement... childElements);

  /**
   * Removes all child elements from this element.
   *
   * @param childElements the child elements to remove
   * @return the wrapped {@link SpinXmlElement element}
   * @throws IllegalArgumentException if child elements is null or any of them
   * @throws SpinXmlElementException if one of the child elements does not exist
   * @throws SpinXmlElementImplementationException if the child element cannot be removed in the underlying implementation
   */
  public abstract SpinXmlElement remove(Collection<SpinXmlElement> childElements);

  /**
   * Replaces this element by an new one.
   *
   * @param newChildElement the new element
   * @return the new wrapped {@link SpinXmlElement element}
   * @throws IllegalArgumentException if the new element is null or has the wrong type
   * @throws SpinXmlElementException if this element has no parent element
   * @throws SpinXmlElementImplementationException if the element cannot be replaced in the underlying implementation
   */
  public abstract SpinXmlElement replace(SpinXmlElement newChildElement);

  /**
   * Replaces an existing child element with a new one.
   *
   * @param existingChildElement the existing child element to replace
   * @param newChildElement the new child element
   * @return the wrapped {@link SpinXmlElement element}
   * @throws IllegalArgumentException if any of the child elements is null
   * @throws SpinXmlElementException if the existing element is not a child element of this
   * @throws SpinXmlElementImplementationException if the child cannot be replaced in the underlying implementation
   */
  public abstract SpinXmlElement replaceChild(SpinXmlElement existingChildElement, SpinXmlElement newChildElement);

  /**
   * Creates a XPath query on this element.
   *
   * @param expression the XPath expression
   * @return the XPath query
   */
  public abstract SpinXPathQuery xPath(String expression);

  /**
   * Returns the wrapped XML element as string representation.
   *
   * @return the string representation
   * @throws SpinXmlElementException if the element cannot be transformed or no new transformer can be created
   */
  public abstract String toString();

  /**
   * Writes the wrapped XML element to a existing writer.
   *
   * @param writer the writer to write to
   * @throws SpinXmlElementException if the element cannot be transformed or no new transformer can be created
   */
  public abstract void writeToWriter(Writer writer);
}
