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

import org.camunda.spin.spi.AbstractConfiguration;

import java.util.Map;

/**
 * @author Stefan Hentschel.
 */
public abstract class AbstractXmlDomDataFormatConfiguration<R extends AbstractConfiguration<R>>
  extends AbstractConfiguration<R> implements XmlDomConfigurable {

  protected XmlDomDataFormat dataFormat;

  public AbstractXmlDomDataFormatConfiguration(XmlDomDataFormat dataFormat) {
    super();
    this.dataFormat = dataFormat;
  }

  public AbstractXmlDomDataFormatConfiguration(XmlDomDataFormat dataFormat, R configuration) {
    super(configuration);
    this.dataFormat = dataFormat;
  }

  public XmlDomMapperConfiguration mapper() {
    return dataFormat.mapper();
  }

  public XmlDomDataFormat done() {
    return dataFormat;
  }

  /**
   * Adds a map of parameters to the configuration.
   * The map should contain at least one of the following properties:
   *
   * -- properties -> A {@link java.util.Map} which contains properties for the marshaller or unmarshaller
   * -- schema -> a {@link javax.xml.validation.Schema} for validating purpose
   * -- eventHandler -> a {@link javax.xml.bind.ValidationEventHandler} to
   *                    fetch all thrown events from the marshaller/unmarshaller
   *
   * If you set schema or the eventHandler to null the marshaller/unmarshaller will set them
   * to their default value.
   *
   * @param config map with configuration
   * @return current representation of this configuration
   */
  public R config(Map<String, Object> config) {
    dataFormat.invalidateCachedMarshallers();
    return super.config(config);
  }

  /**
   * Adds a parameter to the configuration.
   * The parameter should be at least one of the following properties:
   *
   * -- properties -> A {@link java.util.Map} which contains properties for the marshaller or unmarshaller
   * -- schema -> a {@link javax.xml.validation.Schema} for validating purpose
   * -- eventHandler -> a {@link javax.xml.bind.ValidationEventHandler} to
   *                    fetch all thrown events from the marshaller/unmarshaller
   *
   * If you set schema or the eventHandler to null the marshaller/unmarshaller will set them
   * to their default value.
   *
   * @param key name of the property
   * @param value value of the property
   * @return current representation of this configuration
   */
  public R config(String key, Object value) {
    dataFormat.invalidateCachedMarshallers();
    return super.config(key, value);
  }
}
