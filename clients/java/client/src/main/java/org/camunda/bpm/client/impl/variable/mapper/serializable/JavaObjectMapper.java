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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats;
import org.camunda.commons.utils.IoUtil;

public class JavaObjectMapper extends AbstractObjectMapper {

  public static final String NAME = "serializable";

  public JavaObjectMapper() {
    super(SerializationDataFormats.JAVA.getName());
  }

  public String getName() {
    return NAME;
  }

  @Override
  protected Object deserializeFromString(String serializedValue, String objectTypeName) throws Exception {
    Decoder decoder = Base64.getDecoder();
    byte[] base64DecodedSerializedValue = decoder.decode(serializedValue);

    InputStream is = new ByteArrayInputStream(base64DecodedSerializedValue);
    ObjectInputStream ois = new ObjectInputStream(is);

    try {
      return ois.readObject();
    }
    finally {
      IoUtil.closeSilently(ois);
      IoUtil.closeSilently(is);
    }
  }

  protected String serializeToString(Object deserializedObject) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream ois = new ObjectOutputStream(baos);

    try {
      ois.writeObject(deserializedObject);
      byte[] deserializedObjectByteArray = baos.toByteArray();

      Encoder encoder = Base64.getEncoder();
      return encoder.encodeToString(deserializedObjectByteArray);
    }
    finally {
      IoUtil.closeSilently(ois);
      IoUtil.closeSilently(baos);
    }
  }

  protected String getTypeNameForDeserialized(Object deserializedObject) {
    return deserializedObject.getClass().getName();
  }

  protected boolean canSerializeValue(Object value) {
    return value instanceof Serializable;
  }

}
