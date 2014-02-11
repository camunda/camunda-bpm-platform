/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.model.xml.impl.util;

import org.camunda.bpm.model.xml.instance.DomDocument;
import org.camunda.bpm.model.xml.instance.DomElement;

import java.util.HashMap;
import java.util.Map;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

/**
 * @author Sebastian Menski
 */
public class XmlQName {

  private static final Map<String, String> KNOWN_PREFIXES;
  static
  {
    KNOWN_PREFIXES = new HashMap<String, String>();
    KNOWN_PREFIXES.put("http://www.camunda.com/fox", "fox");
    KNOWN_PREFIXES.put("http://activiti.org/bpmn", "camunda");
    KNOWN_PREFIXES.put("http://www.omg.org/spec/BPMN/20100524/MODEL", "");
    KNOWN_PREFIXES.put("http://www.omg.org/spec/BPMN/20100524/DI", "bpmndi");
    KNOWN_PREFIXES.put(XMLNS_ATTRIBUTE_NS_URI, "");
  }

  private final DomElement rootElement;
  private final DomElement element;

  private final String localName;
  private final String namespaceUri;
  private final String prefix;

  public XmlQName(DomDocument document, String namespaceUri, String localName) {
    this(document, null, namespaceUri, localName);
  }

  public XmlQName(DomElement element, String namespaceUri, String localName) {
    this(element.getDocument(), element, namespaceUri, localName);
  }

  public XmlQName(DomDocument document, DomElement element, String namespaceUri, String localName) {
    this.rootElement = document.getRootElement();
    this.element = element;
    this.localName = localName;
    this.namespaceUri = namespaceUri;
    this.prefix = determinePrefixAndNamespaceUri();
  }

  public String getNamespaceUri() {
    return namespaceUri;
  }

  public String getLocalName() {
    return localName;
  }

  public String getPrefixedName() {
    return QName.combine(prefix, localName);
  }

  public boolean hasGlobalNamespace() {
    if (rootElement != null) {
      return rootElement.getNamespaceURI().equals(namespaceUri);
    }
    else if (element != null) {
      return element.getNamespaceURI().equals(namespaceUri);
    }
    else {
      return false;
    }
  }

  private String determinePrefixAndNamespaceUri() {
    if (namespaceUri != null) {
      if (rootElement != null && namespaceUri.equals(rootElement.getNamespaceURI())) {
        // global namespaces do not have a prefix or namespace URI
        return null;
      }
      else {
        // lookup for prefix
        String lookupPrefix = lookupPrefix();
        if (lookupPrefix == null && rootElement != null) {
          // if no prefix is found we generate a new one
          return rootElement.registerNamespace(namespaceUri);
        }
        else {
          return lookupPrefix;
        }
      }
    }
    else {
      // no namespace so no prefix
      return null;
    }
  }

  private String lookupPrefix() {
    if (namespaceUri != null) {
      String lookupPrefix = null;
      if (element != null) {
        lookupPrefix = element.lookupPrefix(namespaceUri);
      }
      else if (rootElement != null) {
        lookupPrefix = rootElement.lookupPrefix(namespaceUri);
      }
      if (lookupPrefix == null) {
        return KNOWN_PREFIXES.get(namespaceUri);
      }
      else {
        return lookupPrefix;
      }
    }
    else {
      return null;
    }
  }

}
