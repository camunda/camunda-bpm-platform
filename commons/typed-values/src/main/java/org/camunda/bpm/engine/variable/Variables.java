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

import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.engine.variable.impl.context.EmptyVariableContext;
import org.camunda.bpm.engine.variable.impl.value.AbstractTypedValue;
import org.camunda.bpm.engine.variable.impl.value.FileValueImpl;
import org.camunda.bpm.engine.variable.impl.value.NullValueImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl.BooleanValueImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl.BytesValueImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl.DateValueImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl.DoubleValueImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl.IntegerValueImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl.LongValueImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl.NumberValueImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl.ShortValueImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl.StringValueImpl;
import org.camunda.bpm.engine.variable.impl.value.builder.FileValueBuilderImpl;
import org.camunda.bpm.engine.variable.impl.value.builder.ObjectVariableBuilderImpl;
import org.camunda.bpm.engine.variable.impl.value.builder.SerializedObjectValueBuilderImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.camunda.bpm.engine.variable.value.DoubleValue;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.IntegerValue;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.NumberValue;
import org.camunda.bpm.engine.variable.value.ObjectValue;
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
  public enum SerializationDataFormats implements SerializationDataFormat {

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

    SerializationDataFormats(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  /**
   * Returns a new {@link VariableMap} instance.
   */
  public static VariableMap createVariables() {
    return new VariableMapImpl();
  }

  /**
   * If the given map is not a variable map, adds all its entries as untyped
   * values to a new {@link VariableMap}. If the given map is a {@link VariableMap},
   * it is returned as is.
   */
  public static VariableMap fromMap(Map<String, Object> map) {
    if(map instanceof VariableMap) {
      return (VariableMap) map;
    }
    else {
      return new VariableMapImpl(map);
    }
  }

  /**
   * Shortcut for {@code Variables.createVariables().putValue(name, value)}
   */
  public static VariableMap putValue(String name, Object value) {
    return createVariables().putValue(name, value);
  }

  /**
   * Shortcut for {@code Variables.createVariables().putValueTyped(name, value)}
   */
  public static VariableMap putValueTyped(String name, TypedValue value) {
    return createVariables().putValueTyped(name, value);
  }

  /**
   * Returns a builder to create a new {@link ObjectValue} that encapsulates
   * the given {@code value}.
   */
  public static ObjectValueBuilder objectValue(Object value) {
    return new ObjectVariableBuilderImpl(value);
  }

  /**
   * Returns a builder to create a new {@link ObjectValue} that encapsulates
   * the given {@code value}.
   */
  public static ObjectValueBuilder objectValue(Object value, boolean isTransient) {
    return (ObjectValueBuilder) objectValue(value).setTransient(isTransient);
  }

  /**
   * Returns a builder to create a new {@link ObjectValue} from a serialized
   * object representation.
   */
  public static SerializedObjectValueBuilder serializedObjectValue() {
    return new SerializedObjectValueBuilderImpl();
  }

  /**
   * Shortcut for {@code Variables.serializedObjectValue().serializedValue(value)}
   */
  public static SerializedObjectValueBuilder serializedObjectValue(String value) {
    return serializedObjectValue().serializedValue(value);
  }

  /**
   * Shortcut for {@code Variables.serializedObjectValue().serializedValue(value).setTransient(isTransient)}
   */
  public static SerializedObjectValueBuilder serializedObjectValue(String value, boolean isTransient) {
    return (SerializedObjectValueBuilder) serializedObjectValue().serializedValue(value).setTransient(isTransient);
  }

  /**
   * Creates a new {@link IntegerValue} that encapsulates the given <code>integer</code>
   */
  public static IntegerValue integerValue(Integer integer) {
    return integerValue(integer, false);
  }

  /**
   * Creates a new {@link IntegerValue} that encapsulates the given <code>integer</code>
   */
  public static IntegerValue integerValue(Integer integer, boolean isTransient) {
    return new IntegerValueImpl(integer, isTransient);
  }

  /**
   * Creates a new {@link StringValue} that encapsulates the given <code>stringValue</code>
   */
  public static StringValue stringValue(String stringValue) {
    return stringValue(stringValue, false);
  }

  /**
   * Creates a new {@link StringValue} that encapsulates the given <code>stringValue</code>
   */
  public static StringValue stringValue(String stringValue, boolean isTransient) {
    return new StringValueImpl(stringValue, isTransient);
  }

  /**
   * Creates a new {@link BooleanValue} that encapsulates the given <code>booleanValue</code>
   */
  public static BooleanValue booleanValue(Boolean booleanValue) {
    return booleanValue(booleanValue, false);
  }

  /**
   * Creates a new {@link BooleanValue} that encapsulates the given <code>booleanValue</code>
   */
  public static BooleanValue booleanValue(Boolean booleanValue, boolean isTransient) {
    return new BooleanValueImpl(booleanValue, isTransient);
  }

  /**
   * Creates a new {@link BytesValue} that encapsulates the given <code>bytes</code>
   */
  public static BytesValue byteArrayValue(byte[] bytes) {
    return byteArrayValue(bytes, false);
  }

  /**
   * Creates a new {@link BytesValue} that encapsulates the given <code>bytes</code>
   */
  public static BytesValue byteArrayValue(byte[] bytes, boolean isTransient) {
    return new BytesValueImpl(bytes, isTransient);
  }

  /**
   * Creates a new {@link DateValue} that encapsulates the given <code>date</code>
   */
  public static DateValue dateValue(Date date) {
    return dateValue(date, false);
  }

  /**
   * Creates a new {@link DateValue} that encapsulates the given <code>date</code>
   */
  public static DateValue dateValue(Date date, boolean isTransient) {
    return new DateValueImpl(date, isTransient);
  }

  /**
   * Creates a new {@link LongValue} that encapsulates the given <code>longValue</code>
   */
  public static LongValue longValue(Long longValue) {
    return longValue(longValue, false);
  }

  /**
   * Creates a new {@link LongValue} that encapsulates the given <code>longValue</code>
   */
  public static LongValue longValue(Long longValue, boolean isTransient) {
    return new LongValueImpl(longValue, isTransient);
  }

  /**
   * Creates a new {@link ShortValue} that encapsulates the given <code>shortValue</code>
   */
  public static ShortValue shortValue(Short shortValue) {
    return shortValue(shortValue, false);
  }

  /**
   * Creates a new {@link ShortValue} that encapsulates the given <code>shortValue</code>
   */
  public static ShortValue shortValue(Short shortValue, boolean isTransient) {
    return new ShortValueImpl(shortValue, isTransient);
  }

  /**
   * Creates a new {@link DoubleValue} that encapsulates the given <code>doubleValue</code>
   */
  public static DoubleValue doubleValue(Double doubleValue) {
    return doubleValue(doubleValue, false);
  }

  /**
   * Creates a new {@link DoubleValue} that encapsulates the given <code>doubleValue</code>
   */
  public static DoubleValue doubleValue(Double doubleValue, boolean isTransient) {
    return new DoubleValueImpl(doubleValue, isTransient);
  }

  /**
   * Creates an abstract Number value. Note that this value cannot be used to set variables.
   * Use the specific methods {@link Variables#integerValue(Integer)}, {@link #shortValue(Short)},
   * {@link #longValue(Long)} and {@link #doubleValue(Double)} instead.
   */
  public static NumberValue numberValue(Number numberValue) {
    return numberValue(numberValue, false);
  }

  /**
   * Creates an abstract Number value. Note that this value cannot be used to set variables.
   * Use the specific methods {@link Variables#integerValue(Integer)}, {@link #shortValue(Short)},
   * {@link #longValue(Long)} and {@link #doubleValue(Double)} instead.
   */
  public static NumberValue numberValue(Number numberValue, boolean isTransient) {
    return new NumberValueImpl(numberValue, isTransient);
  }

  /**
   * Creates a {@link TypedValue} with value {@code null} and type {@link ValueType#NULL}
   */
  public static TypedValue untypedNullValue() {
    return untypedNullValue(false);
  }

  /**
   * Creates a {@link TypedValue} with value {@code null} and type {@link ValueType#NULL}
   */
  public static TypedValue untypedNullValue(boolean isTransient) {
    if (isTransient) {
      return NullValueImpl.INSTANCE_TRANSIENT;
    }
    else {
      return NullValueImpl.INSTANCE;
    }
  }

  /**
   * Creates an untyped value, i.e. {@link TypedValue#getType()} returns <code>null</code>
   * for the returned instance.
   */
  public static TypedValue untypedValue(Object value) {
    if (value instanceof TypedValue) {
      return untypedValue(value, ((TypedValue) value).isTransient());
    }
    else return untypedValue(value, false);
  }

  /**
   * Creates an untyped value, i.e. {@link TypedValue#getType()} returns <code>null</code>
   * for the returned instance.
   */
  public static TypedValue untypedValue(Object value, boolean isTransient) {
    if(value == null) {
      return untypedNullValue(isTransient);
    } else if (value instanceof TypedValueBuilder<?>) {
      return ((TypedValueBuilder<?>) value).setTransient(isTransient).create();
    } else if (value instanceof TypedValue) {
      TypedValue transientValue = (TypedValue) value;
      if (value instanceof NullValueImpl) {
        transientValue = untypedNullValue(isTransient);
      } else if (value instanceof FileValue) {
        ((FileValueImpl) transientValue).setTransient(isTransient);
      } else if (value instanceof AbstractTypedValue<?>) {
        ((AbstractTypedValue<?>) transientValue).setTransient(isTransient);
      }
      return transientValue;
    }
    else {
      // unknown value
      return new UntypedValueImpl(value, isTransient);
    }
  }

  /**
   * Returns a builder to create a new {@link FileValue} with the given
   * {@code filename}.
   */
  public static FileValueBuilder fileValue(String filename) {
    return fileValue(filename, false);
  }

  /**
   * Returns a builder to create a new {@link FileValue} with the given
   * {@code filename}.
   */
  public static FileValueBuilder fileValue(String filename, boolean isTransient) {
    return new FileValueBuilderImpl(filename).setTransient(isTransient);
  }

  /**
   * Shortcut for calling {@code Variables.fileValue(name).file(file).mimeType(type).create()}.
   * The name is set to the file name and the mime type is detected via {@link MimetypesFileTypeMap}.
   */
  public static FileValue fileValue(File file){
    String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file);
    return new FileValueBuilderImpl(file.getName()).file(file).mimeType(contentType).create();
  }

  /**
   * Shortcut for calling {@code Variables.fileValue(name).file(file).mimeType(type).setTransient(isTransient).create()}.
   * The name is set to the file name and the mime type is detected via {@link MimetypesFileTypeMap}.
   */
  public static FileValue fileValue(File file, boolean isTransient){
    String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file);
    return new FileValueBuilderImpl(file.getName()).file(file).mimeType(contentType).setTransient(isTransient).create();
  }

  /**
   * @return an empty {@link VariableContext} (from which no variables can be resolved).
   */
  public static VariableContext emptyVariableContext() {
    return EmptyVariableContext.INSTANCE;
  }

}
