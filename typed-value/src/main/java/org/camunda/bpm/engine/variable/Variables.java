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
package org.camunda.bpm.engine.variable;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.camunda.bpm.engine.impl.core.variable.VariableMapImpl;
import org.camunda.bpm.engine.impl.core.variable.value.NullValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.BooleanValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.BytesValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.DateValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.DoubleValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.IntegerValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.LongValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.NumberValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.ShortValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.StringValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.UntypedValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.builder.FileValueBuilderImpl;
import org.camunda.bpm.engine.impl.core.variable.value.builder.ObjectVariableBuilderImpl;
import org.camunda.bpm.engine.impl.core.variable.value.builder.SerializedObjectValueBuilderImpl;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.camunda.bpm.engine.variable.value.DoubleValue;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.IntegerValue;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.NumberValue;
import org.camunda.bpm.engine.variable.value.SerializationDataFormat;
import org.camunda.bpm.engine.variable.value.ShortValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.engine.variable.value.builder.FileValueBuilder;
import org.camunda.bpm.engine.variable.value.builder.ObjectValueBuilder;
import org.camunda.bpm.engine.variable.value.builder.SerializedObjectValueBuilder;
import org.camunda.bpm.engine.variable.value.builder.TypedValueBuilder;

/**
 * <p>This class is the entry point to the process engine's typed variables API.
 * Users can import the methods provided by this class using a static import:</p>
 *
 * <code>
 * import static org.camunda.bpm.engine.variable.Variables.*;
 * </code>
 *
 * @author Daniel Meyer
 *
 */
public class Variables {

  /**
   * <p>A set of builtin serialization dataformat constants. These constants can be used to specify
   * how java object variables should be serialized by the process engine:</p>
   *
   * <pre>
   * CustomerData customerData = new CustomerData();
   * // ...
   * ObjectValue customerDataValue = Variables.objectValue(customerData)
   *   .serializationDataFormat(Variables.SerializationDataFormats.JSON)
   *   .create();
   *
   * execution.setVariable("someVariable", customerDataValue);
   * </pre>
   *
   * <p>Note that not all of the formats provided here are supported out of the box.</p>
   *
   * @author Daniel Meyer
   */
  public static enum SerializationDataFormats implements SerializationDataFormat {

    /**
     * <p>The Java Serialization Data format. If this data format is used for serializing an object,
     * the object is serialized using default Java {@link Serializable}.</p>
     *
     * <p>The process engine provides a serializer for this dataformat out of the box.</p>
     */
    JAVA("application/x-java-serialized-object"),

    /**
     * <p>The Json Serialization Data format. If this data format is used for serializing an object,
     * the object is serialized as Json text.</p>
     *
     * <p><strong>NOTE:</strong> the process does NOT provide a serializer for this dataformat out of the box.
     * If you want to serialize objects using the Json dataformat, you need to provide a serializer. The optinal
     * camunda Spin process engine plugin provides such a serializer.</p>
     */
    JSON("application/json"),

    /**
     * <p>The Xml Serialization Data format. If this data format is used for serializing an object,
     * the object is serialized as Xml text.</p>
     *
     * <p><strong>NOTE:</strong> the process does NOT provide a serializer for this dataformat out of the box.
     * If you want to serialize objects using the Xml dataformat, you need to provide a serializer. The optinal
     * camunda Spin process engine plugin provides such a serializer.</p>
     */
    XML("application/xml");

    private final String name;

    private SerializationDataFormats(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public static VariableMap createVariables() {
    return new VariableMapImpl();
  }

  public static VariableMap fromMap(Map<String, Object> map) {
    if(map instanceof VariableMap) {
      return (VariableMap) map;
    }
    else {
      return new VariableMapImpl(map);
    }
  }

  public static ObjectValueBuilder objectValue(Object value) {
    return new ObjectVariableBuilderImpl(value);
  }

  public static SerializedObjectValueBuilder serializedObjectValue() {
    return new SerializedObjectValueBuilderImpl();
  }

  public static SerializedObjectValueBuilder serializedObjectValue(String value) {
    return serializedObjectValue().serializedValue(value);
  }

  public static IntegerValue integerValue(Integer integer) {
    return new IntegerValueImpl(integer);
  }

  public static StringValue stringValue(String stringValue) {
    return new StringValueImpl(stringValue);
  }

  public static BooleanValue booleanValue(Boolean booleanValue) {
    return new BooleanValueImpl(booleanValue);
  }

  public static BytesValue byteArrayValue(byte[] bytes) {
    return new BytesValueImpl(bytes);
  }

  public static DateValue dateValue(Date date) {
    return new DateValueImpl(date);
  }

  public static LongValue longValue(Long longValue) {
    return new LongValueImpl(longValue);
  }

  public static ShortValue shortValue(Short shortValue) {
    return new ShortValueImpl(shortValue);
  }

  public static DoubleValue doubleValue(Double doubleValue) {
    return new DoubleValueImpl(doubleValue);
  }

  /**
   * Creates an abstract Number value. Note that this value cannot be used to set variables.
   * Use the specific methods {@link Variables#integerValue(Integer)}, {@link #shortValue(Short)},
   * {@link #longValue(Long)} and {@link #doubleValue(Double)} instead.
   */
  public static NumberValue numberValue(Number numberValue) {
    return new NumberValueImpl(numberValue);
  }

  public static TypedValue untypedNullValue() {
    return NullValueImpl.INSTANCE;
  }

  public static TypedValue untypedValue(Object value) {
    if(value == null) {
      return untypedNullValue();
    } else if (value instanceof TypedValueBuilder<?>) {
      return ((TypedValueBuilder<?>) value).create();
    }
    else if (value instanceof TypedValue) {
      return (TypedValue) value;
    }
    else {
      // unknown value
      return new UntypedValueImpl(value);
    }
  }

  public static FileValueBuilder fileValue(String filename) {
    return new FileValueBuilderImpl(filename);
  }

  /**
   * Shortcut for calling fileValue(name).file(file).mimeType(type).create(). The name will be taken
   * from the file and the type will be detected by {@link MimetypesFileTypeMap}.
   * @param file
   * @return
   */
  public static FileValue fileValue(File file){
    String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file);
    return new FileValueBuilderImpl(file.getName()).file(file).mimeType(contentType).create();
  }
}
