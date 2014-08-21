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

import javax.xml.bind.*;
import javax.xml.validation.Schema;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Hentschel.
 */
public class XmlDomMapperConfiguration extends AbstractXmlDomDataFormatConfiguration<XmlDomMapperConfiguration>
  implements XmlDomConfigurable {

  protected static final XmlDomLogger LOG = SpinLogger.XML_DOM_LOGGER;

  public XmlDomMapperConfiguration(XmlDomDataFormat dataFormat) {
    super(dataFormat);
  }

  @SuppressWarnings("unchecked")
  public XmlDomMapperConfiguration(XmlDomDataFormat dataFormat, XmlDomMapperConfiguration configuration) {
    super(dataFormat, configuration);
  }

  protected XmlDomMapperConfiguration thisConfiguration() {
    return this;
  }

  @SuppressWarnings("unchecked")
  public void applyTo(Marshaller marshaller) {
    // event handler
    try {
      marshaller.setEventHandler((ValidationEventHandler) getValue("eventHandler"));
    } catch (JAXBException e) {
      throw LOG.unableToSetEventHandler(marshaller.getClass().getCanonicalName(), e);
    }

    // schema
    marshaller.setSchema((Schema) getValue("schema"));

    // properties
    Map<String, Object> properties = (Map<String, Object>) getValue("properties", new HashMap<String, Object>());
    for(Map.Entry<String, Object> entry : properties.entrySet()) {
      try {
        marshaller.setProperty(entry.getKey(), entry.getValue());
      } catch (PropertyException e) {
        throw LOG.unableToSetProperty(entry.getKey(), marshaller.getClass().getCanonicalName(), e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void applyTo(Unmarshaller unmarshaller) {
    // event handler
    try {
      unmarshaller.setEventHandler((ValidationEventHandler) getValue("eventHandler"));
    } catch (JAXBException e) {
      throw LOG.unableToSetEventHandler(unmarshaller.getClass().getCanonicalName(), e);
    }

    // schema
    unmarshaller.setSchema((Schema) getValue("schema"));

    // properties
    Map<String, Object> properties = (Map<String, Object>) getValue("properties", new HashMap<String, Object>());
    for(Map.Entry<String, Object> entry : properties.entrySet()) {
      try {
        unmarshaller.setProperty(entry.getKey(), entry.getValue());
      } catch (PropertyException e) {
        throw LOG.unableToSetProperty(entry.getKey(), unmarshaller.getClass().getCanonicalName(), e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getProperties() {
    return (Map<String, Object>) getValue("properties");
  }

  public Schema getSchema() {
    return (Schema) getValue("schema");
  }

  public ValidationEventHandler getEventHandler() {
    return (ValidationEventHandler) getValue("eventHandler");
  }
}
