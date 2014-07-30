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
package org.camunda.bpm.engine.impl.spin;

import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.engine.impl.variable.SerializationVariableTypeResolver;
import org.camunda.bpm.engine.impl.variable.VariableType;
import org.camunda.spin.DataFormats;
import org.camunda.spin.spi.DataFormat;

/**
 * Creates {@link VariableType}s that use Spin dataformats for serialization and deserialization.
 *
 * @author Thorben Lindhauer
 */
public class SpinVariableTypeResolver implements SerializationVariableTypeResolver {

  protected static Set<DataFormat<?>> availableDataFormats = new HashSet<DataFormat<?>>();
  static {
    availableDataFormats.add(DataFormats.jsonTree());
  }

  public VariableType getTypeForSerializationFormat(String serializationFormat) {
    for (DataFormat<?> format : availableDataFormats) {
      if (serializationFormat.equals(format.getName())) {
        return new SpinSerializationType(format);
      }
    }
    return null;

  }

}
