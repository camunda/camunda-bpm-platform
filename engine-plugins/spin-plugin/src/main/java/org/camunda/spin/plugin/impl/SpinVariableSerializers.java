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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.spin.DataFormats;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.xml.SpinXmlElement;

/**
 * @author Thorben Lindhauer
 *
 */
public class SpinVariableSerializers {

  public static List<TypedValueSerializer<?>> createObjectValueSerializers(DataFormats dataFormats) {
    List<TypedValueSerializer<?>> serializers = new ArrayList<TypedValueSerializer<?>>();

    Set<DataFormat<?>> availableDataFormats = dataFormats.getAllAvailableDataFormats();
    for (DataFormat<?> dataFormat : availableDataFormats) {
      serializers.add(new SpinObjectValueSerializer("spin://"+dataFormat.getName(), dataFormat));
    }

    return serializers;
  }

  public static List<TypedValueSerializer<?>> createSpinValueSerializers(DataFormats dataFormats) {
    List<TypedValueSerializer<?>> serializers = new ArrayList<TypedValueSerializer<?>>();

    if(dataFormats.getDataFormatByName(DataFormats.JSON_DATAFORMAT_NAME) != null) {
      DataFormat<SpinJsonNode> jsonDataFormat =
          (DataFormat<SpinJsonNode>) dataFormats.getDataFormatByName(DataFormats.JSON_DATAFORMAT_NAME);
      serializers.add(new JsonValueSerializer(jsonDataFormat));
    }
    if(dataFormats.getDataFormatByName(DataFormats.XML_DATAFORMAT_NAME) != null){
      DataFormat<SpinXmlElement> xmlDataFormat =
          (DataFormat<SpinXmlElement>) dataFormats.getDataFormatByName(DataFormats.XML_DATAFORMAT_NAME);
      serializers.add(new XmlValueSerializer(xmlDataFormat));
    }

    return serializers;
  }
}
