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
package org.camunda.spin.impl.xml.dom;

import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.spi.Configurable;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatMapper;
import org.camunda.spin.spi.DataFormatReader;
import org.camunda.spin.spi.TypeDetector;
import org.camunda.spin.xml.tree.SpinXmlTreeAttribute;
import org.camunda.spin.xml.tree.SpinXmlTreeElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.util.List;

import static org.camunda.spin.impl.util.SpinEnsure.ensureNotNull;

/**
 * @author Daniel Meyer
 *
 */
public class XmlDomDataFormat implements DataFormat<SpinXmlTreeElement> {

  public static final XmlDomDataFormat INSTANCE = new XmlDomDataFormat();
  protected static final XmlDomLogger LOG = SpinLogger.XML_DOM_LOGGER;

  protected XmlDomMapperConfiguration mapperConfiguration;
  protected Unmarshaller cachedUnmarshaller;
  protected Marshaller cachedMarshaller;
  protected JAXBContext context;

  protected List<TypeDetector> typeDetectors;

  public XmlDomDataFormat() {
    this.mapperConfiguration = new XmlDomMapperConfiguration(this);
  }

  public Class<? extends SpinXmlTreeElement> getWrapperType() {
    return SpinXmlDomElement.class;
  }

  public SpinXmlTreeElement createWrapperInstance(Object parameter) {
    return createElementWrapper((Element) parameter);
  }

  public String getName() {
    return "application/xml; implementation=dom";
  }

  public XmlDomDataFormat newInstance() {
    XmlDomDataFormat instance = new XmlDomDataFormat();

    instance.cachedUnmarshaller = cachedUnmarshaller;
    instance.cachedMarshaller = cachedMarshaller;

    instance.mapperConfiguration = new XmlDomMapperConfiguration(instance, mapperConfiguration);

    return instance;
  }

  public XmlDomMapperConfiguration mapper() {
    return mapperConfiguration;
  }

  public SpinXmlTreeElement createElementWrapper(Element element) {
    return new SpinXmlDomElement(element, this);
  }

  public SpinXmlTreeAttribute createAttributeWrapper(Attr attr) {
    return new SpinXmlDomAttribute(attr, this);
  }

  public Configurable<?> getConfiguration() {
    return null;
  }

  public DataFormatReader getReader() {
    return new XmlDomDataFormatReader();
  }
  
  public DataFormatMapper getMapper() {
    return new XmlDomDataFormatMapper(this);
  }

  public XmlDomDataFormat done() {
    return this;
  }

  public void applyTo(Marshaller marshaller) {
    mapperConfiguration.applyTo(marshaller);
  }

  public void applyTo(Unmarshaller unmarshaller) {
    mapperConfiguration.applyTo(unmarshaller);
  }

  public XmlDomDataFormat configureContext(Class<?> clazz) {
    try {
      context = JAXBContext.newInstance(clazz);
    } catch (JAXBException e) {
      throw LOG.unableToCreateContext(e);
    }

    return this;
  }

  public Marshaller getConfiguredMarshaller(Class<?> clazz) {
    configureContext(clazz);
    if(cachedMarshaller == null) {
      synchronized(this) {
        if(cachedMarshaller == null) {
          try {
            cachedMarshaller = context.createMarshaller();
            applyTo(cachedMarshaller);
          } catch (JAXBException e) {
            throw LOG.unableToCreateMarshaller(e);
          }
        }
      }
    }

    return cachedMarshaller;
  }

  public Unmarshaller getConfiguredUnmarshaller(Class<?> clazz) {
    configureContext(clazz);
    if(cachedUnmarshaller == null) {
      synchronized(this) {
        if(cachedUnmarshaller == null) {
          try {
            cachedUnmarshaller = context.createUnmarshaller();
            applyTo(cachedUnmarshaller);
          } catch (JAXBException e) {
            throw LOG.unableToCreateUnmarshaller(e);
          }
        }
      }
    }

    return cachedUnmarshaller;
  }

  public String getCanonicalTypeName(Object object) {
    ensureNotNull("object", object);

    for (TypeDetector typeDetector : typeDetectors) {
      if (typeDetector.appliesTo(this) && typeDetector.canHandle(object)) {
        return typeDetector.detectType(object);
      }
    }

    throw LOG.unableToDetectCanonicalType(object);
  }

  public void addTypeDetector(TypeDetector typeDetector) {
    ensureNotNull("TypeDetector", typeDetector);
    typeDetectors.add(0, typeDetector);
  }

  public synchronized void invalidateCachedMarshallers() {
    cachedMarshaller = null;
    cachedUnmarshaller = null;
  }
}
