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
package org.camunda.bpm.client.impl.variable.mapper.serializable;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.camunda.commons.utils.IoUtil;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatMapper;
import org.camunda.spin.spi.DataFormatReader;
import org.camunda.spin.spi.DataFormatWriter;

public class SpinObjectValueMapper extends AbstractObjectMapper {

  protected String name;
  protected DataFormat<?> dataFormat;

  public SpinObjectValueMapper(String name, DataFormat<?> dataFormat) {
    super(dataFormat.getName());
    this.name = name;
    this.dataFormat = dataFormat;
  }

  public String getName() {
    return name;
  }

  protected String getTypeNameForDeserialized(Object deserializedObject) {
    return dataFormat.getMapper().getCanonicalTypeName(deserializedObject);
  }

  protected boolean canSerializeValue(Object value) {
    return dataFormat.getMapper().canMap(value);
  }

  protected String serializeToString(Object deserializedObject) throws Exception {
    DataFormatMapper mapper = dataFormat.getMapper();
    DataFormatWriter writer = dataFormat.getWriter();

    StringWriter stringWriter = new StringWriter();

    try {
      Object mappedObject = mapper.mapJavaToInternal(deserializedObject);
      writer.writeToWriter(stringWriter, mappedObject);
      return stringWriter.toString();
    }
    finally {
      IoUtil.closeSilently(stringWriter);
    }
  }

  protected Object deserializeFromString(String serializedValue, String objectTypeName) throws Exception {
    DataFormatMapper mapper = dataFormat.getMapper();
    DataFormatReader reader = dataFormat.getReader();

    StringReader stringReader = new StringReader(serializedValue);
    BufferedReader bufferedReader = new BufferedReader(stringReader);

    try {
      Object mappedObject = reader.readInput(bufferedReader);
      return mapper.mapInternalToJava(mappedObject, objectTypeName);
    }
    finally{
      IoUtil.closeSilently(bufferedReader);
      IoUtil.closeSilently(stringReader);
    }
  }

}
