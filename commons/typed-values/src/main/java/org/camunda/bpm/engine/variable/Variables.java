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
   * Returns a builder to create a new transient {@link ObjectValue} that encapsulates
   * the given {@code value}.
   */
  public static ObjectValueBuilder objectValueTransient(Object value) {
    return (ObjectValueBuilder) objectValue(value).setTransient(true);
  }

  /**
   * Returns a builder to create a new {@link ObjectValue} from a serialized
   * object representation.
   */
  public static SerializedObjectValueBuilder serializedObjectValue() {
    return new SerializedObjectValueBuilderImpl();
  }

  /**
   * Shortcut for {@code Variables.serializedObjectValue().serializedObjectValue(value)}
   */
  public static SerializedObjectValueBuilder serializedObjectValue(String value) {
    return serializedObjectValue().serializedValue(value);
  }

  /**
   * Creates a new {@link IntegerValue} that encapsulates the given <code>integer</code>
   */
  public static IntegerValue integerValue(Integer integer) {
    return new IntegerValueImpl(integer);
  }

  /**
   * Creates a new transient {@link IntegerValue} that encapsulates the given <code>integer</code>
   */
  public static IntegerValue integerValueTransient(Integer integer) {
    IntegerValue value = integerValue(integer);
    value.setTransient(true);
    return value;
  }

  /**
   * Creates a new {@link StringValue} that encapsulates the given <code>stringValue</code>
   */
  public static StringValue stringValue(String stringValue) {
    return new StringValueImpl(stringValue);
  }

  /**
   * Creates a new transient {@link StringValue} that encapsulates the given <code>stringValue</code>
   */
  public static StringValue stringValueTransient(String stringValue) {
    StringValue value = stringValue(stringValue);
    value.setTransient(true);
    return value;
  }

  /**
   * Creates a new {@link BooleanValue} that encapsulates the given <code>booleanValue</code>
   */
  public static BooleanValue booleanValue(Boolean booleanValue) {
    return new BooleanValueImpl(booleanValue);
  }

  /**
   * Creates a new transient {@link BooleanValue} that encapsulates the given <code>booleanValue</code>
   */
  public static BooleanValue booleanValueTransient(Boolean booleanValue) {
    BooleanValue value = booleanValue(booleanValue);
    value.setTransient(true);
    return value;
  }

  /**
   * Creates a new {@link BytesValue} that encapsulates the given <code>bytes</code>
   */
  public static BytesValue byteArrayValue(byte[] bytes) {
    return new BytesValueImpl(bytes);
  }

  /**
   * Creates a new transient {@link BytesValue} that encapsulates the given <code>bytes</code>
   */
  public static BytesValue byteArrayValueTransient(byte[] bytes) {
    BytesValue value = byteArrayValue(bytes);
    value.setTransient(true);
    return value;
  }

  /**
   * Creates a new {@link DateValue} that encapsulates the given <code>date</code>
   */
  public static DateValue dateValue(Date date) {
    return new DateValueImpl(date);
  }

  /**
   * Creates a new transient {@link DateValue} that encapsulates the given <code>date</code>
   */
  public static DateValue dateValueTransient(Date date) {
    DateValue value = dateValue(date);
    value.setTransient(true);
    return value;
  }

  /**
   * Creates a new {@link LongValue} that encapsulates the given <code>longValue</code>
   */
  public static LongValue longValue(Long longValue) {
    return new LongValueImpl(longValue);
  }

  /**
   * Creates a new transient {@link LongValue} that encapsulates the given <code>longValue</code>
   */
  public static LongValue longValueTransient(Long longValue) {
    LongValue value = longValue(longValue);
    value.setTransient(true);
    return value;
  }

  /**
   * Creates a new {@link ShortValue} that encapsulates the given <code>shortValue</code>
   */
  public static ShortValue shortValue(Short shortValue) {
    return new ShortValueImpl(shortValue);
  }

  /**
   * Creates a new transient {@link ShortValue} that encapsulates the given <code>shortValue</code>
   */
  public static ShortValue shortValueTransient(Short shortValue) {
    ShortValue value = shortValue(shortValue);
    value.setTransient(true);
    return value;
  }

  /**
   * Creates a new {@link DoubleValue} that encapsulates the given <code>doubleValue</code>
   */
  public static DoubleValue doubleValue(Double doubleValue) {
    return new DoubleValueImpl(doubleValue);
  }

  /**
   * Creates a new transient {@link DoubleValue} that encapsulates the given <code>doubleValue</code>
   */
  public static DoubleValue doubleValueTransient(Double doubleValue) {
    DoubleValue value = doubleValue(doubleValue);
    value.setTransient(true);
    return value;
  }

  /**
   * Creates an abstract Number value. Note that this value cannot be used to set variables.
   * Use the specific methods {@link Variables#integerValue(Integer)}, {@link #shortValue(Short)},
   * {@link #longValue(Long)} and {@link #doubleValue(Double)} instead.
   */
  public static NumberValue numberValue(Number numberValue) {
    return new NumberValueImpl(numberValue);
  }

  /**
   * Creates a {@link TypedValue} with value {@code null} and type {@link ValueType#NULL}
   */
  public static TypedValue untypedNullValue() {
    return NullValueImpl.INSTANCE;
  }

  /**
   * Creates a transient {@link TypedValue} with value {@code null} and type {@link ValueType#NULL}
   */
  public static TypedValue untypedTransientNullValue() {
    return NullValueImpl.INSTANCE_TRANSIENT;
  }

  /**
   * Creates an untyped value, i.e. {@link TypedValue#getType()} returns <code>null</code>
   * for the returned instance.
   */
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

  /**
   * Creates an transient untyped value, i.e. {@link TypedValue#getType()} returns <code>null</code>
   * for the returned instance.
   */
  public static TypedValue transientUntypedValue(Object value) {
    if(value == null) {
      return untypedTransientNullValue();
    } else if (value instanceof TypedValueBuilder<?>) {
      return ((TypedValueBuilder<?>) value).setTransient(true).create();
    } else if (value instanceof TypedValue) {
      TypedValue transientValue = (TypedValue) value;
      if (value instanceof NullValueImpl) {
        transientValue = untypedTransientNullValue();
      } else {
        transientValue.setTransient(true);
      }
      return transientValue;
    }
    else {
      // unknown value
      return new UntypedValueImpl(value, true);
    }
  }

  /**
   * Returns a builder to create a new {@link FileValue} with the given
   * {@code filename}.
   */
  public static FileValueBuilder fileValue(String filename) {
    return new FileValueBuilderImpl(filename);
  }

  /**
   * Returns a builder to create a new transient {@link FileValue} with the given
   * {@code filename}.
   */
  public static FileValueBuilder fileValueTransient(String filename) {
    return new FileValueBuilderImpl(filename).setTransient(true);
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
   * Shortcut for calling {@code Variables.fileValue(name).file(file).mimeType(type).setTransient(true).create()}.
   * The name is set to the file name and the mime type is detected via {@link MimetypesFileTypeMap}.
   */
  public static FileValue fileValueTransient(File file){
    String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file);
    return new FileValueBuilderImpl(file.getName()).file(file).mimeType(contentType).setTransient(true).create();
  }

  /**
   * @return an empty {@link VariableContext} (from which no variables can be resolved).
   */
  public static VariableContext emptyVariableContext() {
    return EmptyVariableContext.INSTANCE;
  }

}
