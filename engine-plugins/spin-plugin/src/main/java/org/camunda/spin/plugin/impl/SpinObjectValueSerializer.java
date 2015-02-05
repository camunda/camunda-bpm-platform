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
package org.camunda.spin.plugin.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.variable.serializer.AbstractObjectValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatMapper;
import org.camunda.spin.spi.DataFormatReader;
import org.camunda.spin.spi.DataFormatWriter;

/**
 * Implementation of a {@link TypedValueSerializer} for {@link ObjectValue ObjectValues} using a
 * Spin-provided {@link DataFormat} to serialize and deserialize java objects.
 *
 * @author Daniel Meyer
 *
 */
public class SpinObjectValueSerializer extends AbstractObjectValueSerializer {

  protected String name;
  protected DataFormat<?> dataFormat;

  public SpinObjectValueSerializer(String name, DataFormat<?> dataFormat) {
    super(dataFormat.getName());
    this.name = name;
    this.dataFormat = dataFormat;
  }

  public String getName() {
    return name;
  }

  protected boolean isSerializationTextBased() {
    // for the moment we assume that all spin data formats are text based.
    return true;
  }

  protected String getTypeNameForDeserialized(Object deserializedObject) {
    return dataFormat.getMapper().getCanonicalTypeName(deserializedObject);
  }

  protected byte[] serializeToByteArray(Object deserializedObject) throws Exception {
    DataFormatMapper mapper = dataFormat.getMapper();
    DataFormatWriter writer = dataFormat.getWriter();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OutputStreamWriter outWriter = new OutputStreamWriter(out, Context.getProcessEngineConfiguration().getDefaultCharset());
    BufferedWriter bufferedWriter = new BufferedWriter(outWriter);

    try {
      Object mappedObject = mapper.mapJavaToInternal(deserializedObject);
      writer.writeToWriter(bufferedWriter, mappedObject);
      return out.toByteArray();
    }
    finally {
      IoUtil.closeSilently(out);
      IoUtil.closeSilently(outWriter);
      IoUtil.closeSilently(bufferedWriter);
    }
  }

  protected Object deserializeFromByteArray(byte[] bytes, String objectTypeName) throws Exception {
    DataFormatMapper mapper = dataFormat.getMapper();
    DataFormatReader reader = dataFormat.getReader();

    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    InputStreamReader inReader = new InputStreamReader(bais, Context.getProcessEngineConfiguration().getDefaultCharset());
    BufferedReader bufferedReader = new BufferedReader(inReader);

    try {
      Object mappedObject = reader.readInput(bufferedReader);
      return mapper.mapInternalToJava(mappedObject, objectTypeName);
    }
    finally{
      IoUtil.closeSilently(bais);
      IoUtil.closeSilently(inReader);
      IoUtil.closeSilently(bufferedReader);
    }
  }

  protected boolean canSerializeValue(Object value) {
    return dataFormat.getMapper().canMap(value);
  }

}
