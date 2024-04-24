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
package org.camunda.spin.impl.xml.dom.format;

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import java.beans.Introspector;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.camunda.spin.DeserializationTypeValidator;
import org.camunda.spin.SpinRuntimeException;
import org.camunda.spin.impl.util.SpinReflectUtil;
import org.camunda.spin.impl.xml.dom.DomXmlLogger;
import org.camunda.spin.impl.xml.dom.format.spi.JaxBContextProvider;
import org.camunda.spin.spi.DataFormatMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * {@link DataFormatMapper} using JAXB for mapping Java Objects to XML and vice-versa.
 *
 * @author Stefan Hentschel.
 * @author Daniel Meyer
 */
public class DomXmlDataFormatMapper implements DataFormatMapper {

  protected DomXmlDataFormat dataFormat;

  private static final DomXmlLogger LOG = DomXmlLogger.XML_DOM_LOGGER;

  public DomXmlDataFormatMapper(DomXmlDataFormat format) {
    this.dataFormat = format;
  }

  @Override
  public boolean canMap(Object parameter) {
    // TODO: can JAX-B tell us whether it is capable of mapping a class?
    return parameter != null;
  }

  @Override
  public String getCanonicalTypeName(Object object) {
    // simply returns the name of the class
    return object.getClass().getName();
  }

  @Override
  public Object mapJavaToInternal(Object parameter) {
    ensureNotNull("Parameter", parameter);

    final Class<?> parameterClass = parameter.getClass();
    final DOMResult domResult = new DOMResult();

    try {
      Marshaller marshaller = getMarshaller(parameterClass);

      boolean isRootElement = parameterClass.getAnnotation(XmlRootElement.class) != null;
      if(isRootElement) {
        marshalRootElement(parameter, marshaller, domResult);
      }
      else {
        marshalNonRootElement(parameter, marshaller, domResult);
      }

      Node node = domResult.getNode();
      return ((Document)node).getDocumentElement();

    } catch (JAXBException e) {
      throw LOG.unableToMapInput(parameter, e);
    }
  }

  protected void marshalRootElement(Object parameter, Marshaller marshaller, DOMResult domResult) throws JAXBException {
    marshaller.marshal(parameter, domResult);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void marshalNonRootElement(Object parameter,  Marshaller marshaller, DOMResult domResult) throws JAXBException {
    Class<?> parameterClass = parameter.getClass();
    String simpleName = Introspector.decapitalize(parameterClass.getSimpleName());
    JAXBElement<?> root = new JAXBElement(new QName(simpleName), parameterClass, parameter);
    marshaller.marshal(root, domResult);
  }

  @Override
  public <T> T mapInternalToJava(Object parameter, Class<T> javaClass) {
    return mapInternalToJava(parameter, javaClass, null);
  }

  @Override
  public <T> T mapInternalToJava(Object parameter, Class<T> javaClass, DeserializationTypeValidator validator) {
    ensureNotNull("Parameter", parameter);
    ensureNotNull("Type", javaClass);

    Node xmlNode = (Node) parameter;
    try {
      validateType(javaClass, validator);
      Unmarshaller unmarshaller = getUnmarshaller(javaClass);
      JAXBElement<T> root = unmarshaller.unmarshal(new DOMSource(xmlNode), javaClass);
      return root.getValue();

    } catch (JAXBException e) {
      throw LOG.unableToDeserialize(parameter, javaClass.getCanonicalName(), e);
    }
  }

  protected void validateType(Class<?> type, DeserializationTypeValidator validator) {
    if (validator != null) {
      // validate the outer class
      if (!type.isPrimitive()) {
        Class<?> typeToValidate = type;
        if (type.isArray()) {
          typeToValidate = type.getComponentType();
        }
        String className = typeToValidate.getName();
        if (!validator.validate(className)) {
          throw new SpinRuntimeException("The class '" + className + "' is not whitelisted for deserialization.");
        }
      }
    }
  }

  @Override
  public <T> T mapInternalToJava(Object parameter, String classIdentifier) {
    return mapInternalToJava(parameter, classIdentifier, null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T mapInternalToJava(Object parameter, String classIdentifier, DeserializationTypeValidator validator) {
    ensureNotNull("Parameter", parameter);
    ensureNotNull("classIdentifier", classIdentifier);

    try {
      Class<?> javaClass = SpinReflectUtil.loadClass(classIdentifier, dataFormat);
      return (T) mapInternalToJava(parameter, javaClass, validator);
    }
    catch (Exception e) {
      throw LOG.unableToDeserialize(parameter, classIdentifier, e);
    }
  }

  protected Marshaller getMarshaller(Class<?> parameter) throws JAXBException {
    JaxBContextProvider jaxBContextProvider = dataFormat.getJaxBContextProvider();
    return jaxBContextProvider.createMarshaller(parameter);
  }

  protected Unmarshaller getUnmarshaller(Class<?> parameter) throws JAXBException {
    JaxBContextProvider jaxBContextProvider = dataFormat.getJaxBContextProvider();
    return jaxBContextProvider.createUnmarshaller(parameter);
  }
}
