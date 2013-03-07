/**
 * Copyright (C) 2011 camunda services GmbH (www.camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.camunda.bpm.cycle.util;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.camunda.bpm.cycle.exception.CycleException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class XmlUtil {
	
	public static final String SAXON_SF_NET_JAXP_XPATH_OM = "http://saxon.sf.net/jaxp/xpath/om";
	public static final String SAXON_XPATH_FACTORY = "net.sf.saxon.xpath.XPathFactoryImpl";
	
	public static String getXPathResult(String expression, InputSource source) {
		return getXPathResult(expression, (Object) source);
	}

	public static String getXPathResult(String expression, Node contextItem) {
		if (contextItem == null) {
			throw new NullPointerException();
		}
		return getXPathResult(expression, (Object) contextItem);
	}

	public static String getXPathResult(String expression, String sourceXml) {
		try {
			return getXPathResult(expression, new InputSource(new ByteArrayInputStream(sourceXml.getBytes("UTF-8"))));
		} catch (UnsupportedEncodingException e) {
			throw new CycleException(e);
		}
	}

	private static String getXPathResult(String expression, Object item) {
		try {
			XPathExpression xPathExpression = initXPathFactoryAndCompileExpression(expression);
			if (item instanceof InputSource) {
				return xPathExpression.evaluate((InputSource) item);
			} else {
				return xPathExpression.evaluate(item);
			}
		} catch (XPathExpressionException e) {
			throw new CycleException(e);
		}
	}
	
	public static boolean containsElementByValue(NodeList elements, String value) {
	  for (int i = 0; i < elements.getLength(); i++) {
      Node node = elements.item(i);
      if (node.getTextContent().equals(value)) {
        return true;
      }
    }
	  return false;
	}
	
	public static Node getSingleElementByXPath(Object searchContext, String expression) {
    XPathExpression xPathExpression = initXPathFactoryAndCompileExpression(expression);
    NodeList elementsWithThatId;
    try {
      elementsWithThatId = (NodeList) xPathExpression.evaluate(searchContext, XPathConstants.NODESET);
      if (elementsWithThatId == null) {
        return null;
      }
    } catch (XPathExpressionException e) {
      throw new CycleException("Error during evaluation of XPath expression '" + expression + "'.", e);
    }
    if (elementsWithThatId.getLength() == 0) {
      return null;
    } else if (elementsWithThatId.getLength() == 1) {
      return elementsWithThatId.item(0);
    } else {
      throw new CycleException("There are multiple elements matching'" +  expression + "'.");
    }
  }
	
	public static NodeList getListOfElementsByXPath(Object searchContext, String expression) {
    XPathExpression xPathExpression = initXPathFactoryAndCompileExpression(expression);
    try {
      return (NodeList) xPathExpression.evaluate(searchContext, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      throw new CycleException("Error during evaluation of XPath expression '" + expression + "'.", e);
    }
  }
	
	private static XPathExpression initXPathFactoryAndCompileExpression(String expression) {
    try {
      XPathFactory xPathFactory = XPathFactory.newInstance(SAXON_SF_NET_JAXP_XPATH_OM, SAXON_XPATH_FACTORY, null);
      XPath xPath = xPathFactory.newXPath();
      xPath.setNamespaceContext(new BpmnNamespaceContext());
      XPathExpression xPathExpression = xPath.compile(expression);
      return xPathExpression;
    } catch (XPathExpressionException e) {
      throw new CycleException("Error during evaluation of XPath expression '" + expression + "'.", e);
    } catch (XPathFactoryConfigurationException e) {
      throw new CycleException(e);
    }
  }
}
