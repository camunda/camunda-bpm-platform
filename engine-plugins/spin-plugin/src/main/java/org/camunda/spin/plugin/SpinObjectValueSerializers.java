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
package org.camunda.spin.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.spin.DataFormats;
import org.camunda.spin.spi.DataFormat;

/**
 * @author Daniel Meyer
 *
 */
public class SpinObjectValueSerializers {

  public List<TypedValueSerializer<?>> getSerializers() {
    List<TypedValueSerializer<?>> resultList = new ArrayList<TypedValueSerializer<?>>();

    Set<DataFormat<?>> availableDataFormats = DataFormats.getAvailableDataFormats();
    for (DataFormat<?> dataFormat : availableDataFormats) {
      resultList.add(new SpinObjectValueSerializer("spin://"+dataFormat.getName(), dataFormat));
    }

    return resultList;
  }


}
