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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializerFactory;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Thorben Lindhauer
 *
 */
public class SpinFallbackSerializerFactory implements VariableSerializerFactory {

  public static final Pattern SPIN_SERIALIZER_NAME_PATTERN = Pattern.compile("spin://(.*)");

  public TypedValueSerializer<?> getSerializer(String serializerName) {
    Matcher matcher = SPIN_SERIALIZER_NAME_PATTERN.matcher(serializerName);
    if (matcher.matches()) {
      String serializationFormat = matcher.group(1);
      return new FallbackSpinObjectValueSerializer(serializationFormat);
    }
    else {
      return null;
    }
  }

  public TypedValueSerializer<?> getSerializer(TypedValue value) {
    if (value instanceof ObjectValue) {
      ObjectValue objectValue = (ObjectValue) value;
      if (objectValue.getSerializationDataFormat() != null && !objectValue.isDeserialized()) {
        return new FallbackSpinObjectValueSerializer(objectValue.getSerializationDataFormat());
      }
    }
    return null;
  }
}
