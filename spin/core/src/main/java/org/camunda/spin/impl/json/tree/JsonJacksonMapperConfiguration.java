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
package org.camunda.spin.impl.json.tree;

import java.text.DateFormat;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides methods to configure Jackson serialization and deserialization features.
 * 
 * @author Thorben Lindhauer
 */
public class JsonJacksonMapperConfiguration 
  extends AbstractJsonJacksonDataFormatConfiguration<JsonJacksonMapperConfiguration>  
  implements JsonJacksonTreeConfigurable {

  protected ObjectMapper.DefaultTyping typing;
  protected JsonTypeInfo.As as;
  
  protected DateFormat dateFormat;
  
  public JsonJacksonMapperConfiguration(JsonJacksonTreeDataFormat dataFormat) {
    super(dataFormat);
  }
  
  public JsonJacksonMapperConfiguration(JsonJacksonTreeDataFormat dataFormat, 
      JsonJacksonMapperConfiguration mapperConfiguration) {
    super(dataFormat, mapperConfiguration);
  }
  
  public JsonJacksonMapperConfiguration enableDefaultTyping(ObjectMapper.DefaultTyping typing, JsonTypeInfo.As as) {
    dataFormat.invalidateCachedObjectMapper();
    
    this.typing = typing;
    this.as = as;
    return this;
  }

  public void applyTo(ObjectMapper mapper) {
    if (typing != null && as != null) {
      mapper.enableDefaultTyping(typing, as);
    }
    
    for (DeserializationFeature deserializationFeature : DeserializationFeature.values()) {
      mapper.configure(deserializationFeature, getValue(deserializationFeature));
    }
    
    mapper.setDateFormat(dateFormat);
  }
  
  public JsonJacksonMapperConfiguration config(DeserializationFeature feature, Object value) {
    return config(feature.name(), value);
  }
  
  public Boolean getValue(DeserializationFeature feature) {
    Boolean value = (Boolean) configuration.get(feature.name());
    if (value == null) {
      return feature.enabledByDefault();
    }
    else {
      return value;
    }
  }

  protected JsonJacksonMapperConfiguration thisConfiguration() {
    return this;
  }
  
  public JsonJacksonMapperConfiguration dateFormat(DateFormat dateFormat) {
    dataFormat.invalidateCachedObjectMapper();
    
    this.dateFormat = dateFormat;
    return this;
  }

}
