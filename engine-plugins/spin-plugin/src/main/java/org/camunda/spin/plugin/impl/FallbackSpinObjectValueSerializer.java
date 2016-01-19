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

import org.camunda.bpm.engine.impl.variable.serializer.AbstractObjectValueSerializer;

/**
 * @author Thorben Lindhauer
 */
public class FallbackSpinObjectValueSerializer extends AbstractObjectValueSerializer {

  private final static SpinPluginLogger LOG = SpinPluginLogger.LOGGER;

  public static final String DESERIALIZED_OBJECTS_EXCEPTION_MESSAGE = "Fallback serializer cannot handle deserialized objects";

  protected String serializationFormat;

  public FallbackSpinObjectValueSerializer(String serializationFormat) {
    super(serializationFormat);
    this.serializationFormat = serializationFormat;
  }

  @Override
  public String getName() {
    return "spin://" + serializationFormat;
  }

  @Override
  protected String getTypeNameForDeserialized(Object deserializedObject) {
    throw LOG.fallbackSerializerCannotDeserializeObjects();
  }

  @Override
  protected byte[] serializeToByteArray(Object deserializedObject) throws Exception {
    throw LOG.fallbackSerializerCannotDeserializeObjects();
  }

  @Override
  protected Object deserializeFromByteArray(byte[] object, String objectTypeName) throws Exception {
    throw LOG.fallbackSerializerCannotDeserializeObjects();
  }

  @Override
  protected boolean isSerializationTextBased() {
    return true;
  }

  @Override
  protected boolean canSerializeValue(Object value) {
    throw LOG.fallbackSerializerCannotDeserializeObjects();
  }

}
