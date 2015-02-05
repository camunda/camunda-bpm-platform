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
package org.camunda.bpm.engine.impl.variable.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;

import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats;

/**
 * Uses default java serialization to serialize java objects as byte streams.
 *
 * @author Daniel Meyer
 * @author Tom Baeyens
 */
public class JavaObjectSerializer extends AbstractObjectValueSerializer {

  public static final String NAME = "serializable";

  public JavaObjectSerializer() {
    super(SerializationDataFormats.JAVA.getName());
  }

  public String getName() {
    return NAME;
  }

  protected boolean isSerializationTextBased() {
    return false;
  }

  protected Object deserializeFromByteArray(byte[] bytes, String objectTypeName) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    ObjectInputStream ois = null;
    try {
      ois = new ClassloaderAwareObjectInputStream(bais);
      return ois.readObject();
    }
    finally {
      IoUtil.closeSilently(ois);
      IoUtil.closeSilently(bais);
    }
  }

  protected byte[] serializeToByteArray(Object deserializedObject) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream ois = null;
    try {
      ois = new ObjectOutputStream(baos);
      ois.writeObject(deserializedObject);
      return baos.toByteArray();
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

  protected static class ClassloaderAwareObjectInputStream extends ObjectInputStream {

    public ClassloaderAwareObjectInputStream(InputStream in) throws IOException {
      super(in);
    }

    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
      return ReflectUtil.loadClass(desc.getName());
    }

  }
}
