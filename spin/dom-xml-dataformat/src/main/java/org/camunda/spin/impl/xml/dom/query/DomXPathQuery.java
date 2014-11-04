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

package org.camunda.spin.impl.xml.dom.query;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.camunda.spin.SpinList;
import org.camunda.spin.impl.SpinListImpl;
import org.camunda.spin.impl.xml.dom.DomXmlAttributeIterable;
import org.camunda.spin.impl.xml.dom.DomXmlElement;
import org.camunda.spin.impl.xml.dom.DomXmlElementIterable;
import org.camunda.spin.impl.xml.dom.DomXmlLogger;
import org.camunda.spin.impl.xml.dom.format.DomXmlDataFormat;
import org.camunda.spin.xml.SpinXPathQuery;
import org.camunda.spin.xml.SpinXmlAttribute;
import org.camunda.spin.xml.SpinXmlElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Sebastian Menski
 */
public class DomXPathQuery extends SpinXPathQuery {

  private static final DomXmlLogger LOG = DomXmlLogger.XML_DOM_LOGGER;

  protected final DomXmlElement domElement;
  protected final XPathExpression query;
  private final DomXmlDataFormat dataFormat;

  public DomXPathQuery(DomXmlElement domElement, XPathExpression query, DomXmlDataFormat dataFormat) {
    this.domElement = domElement;
    this.query = query;
    this.dataFormat = dataFormat;
  }

  public SpinXmlElement element() {
    try {
      Element element = (Element) query.evaluate(domElement.unwrap(), XPathConstants.NODE);
      return dataFormat.createElementWrapper(element);
    } catch (XPathExpressionException e) {
      throw LOG.unableToEvaluateXPathExpressionOnElement(domElement, e);
    } catch (ClassCastException e) {
      throw LOG.unableToCastXPathResultTo(Element.class, e);
    }
  }

  public SpinList<SpinXmlElement> elementList() {
    try {
      NodeList nodeList = (NodeList) query.evaluate(domElement.unwrap(), XPathConstants.NODESET);
      return new SpinListImpl<SpinXmlElement>(new DomXmlElementIterable(nodeList, dataFormat));
    } catch (XPathExpressionException e) {
      throw LOG.unableToEvaluateXPathExpressionOnElement(domElement, e);
    } catch (ClassCastException e) {
      throw LOG.unableToCastXPathResultTo(NodeList.class, e);
    }
  }

  public SpinXmlAttribute attribute() {
    try {
      Attr attribute = (Attr) query.evaluate(domElement.unwrap(), XPathConstants.NODE);
      return dataFormat.createAttributeWrapper(attribute);
    } catch (XPathExpressionException e) {
      throw LOG.unableToEvaluateXPathExpressionOnElement(domElement, e);
    } catch (ClassCastException e) {
      throw LOG.unableToCastXPathResultTo(Attr.class, e);
    }
  }

  public SpinList<SpinXmlAttribute> attributeList() {
    try {
      NodeList nodeList = (NodeList) query.evaluate(domElement.unwrap(), XPathConstants.NODESET);
      return new SpinListImpl<SpinXmlAttribute>(new DomXmlAttributeIterable(nodeList, dataFormat));
    } catch (XPathExpressionException e) {
      throw LOG.unableToEvaluateXPathExpressionOnElement(domElement, e);
    } catch (ClassCastException e) {
      throw LOG.unableToCastXPathResultTo(NodeList.class, e);
    }
  }

  public String string() {
    try {
      return (String) query.evaluate(domElement.unwrap(), XPathConstants.STRING);
    } catch (XPathExpressionException e) {
      throw LOG.unableToEvaluateXPathExpressionOnElement(domElement, e);
    } catch (ClassCastException e) {
      throw LOG.unableToCastXPathResultTo(String.class, e);
    }
  }

  public Double number() {
    try {
      return (Double) query.evaluate(domElement.unwrap(), XPathConstants.NUMBER);
    } catch (XPathExpressionException e) {
      throw LOG.unableToEvaluateXPathExpressionOnElement(domElement, e);
    } catch (ClassCastException e) {
      throw LOG.unableToCastXPathResultTo(Double.class, e);
    }
  }

  public Boolean bool() {
    try {
      return (Boolean) query.evaluate(domElement.unwrap(), XPathConstants.BOOLEAN);
    } catch (XPathExpressionException e) {
      throw LOG.unableToEvaluateXPathExpressionOnElement(domElement, e);
    } catch (ClassCastException e) {
      throw LOG.unableToCastXPathResultTo(Boolean.class, e);
    }
  }

}
