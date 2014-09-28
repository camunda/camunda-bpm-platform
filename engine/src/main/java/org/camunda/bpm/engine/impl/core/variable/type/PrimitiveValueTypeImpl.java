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
package org.camunda.bpm.engine.impl.core.variable.type;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.lang.model.type.NullType;

import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.camunda.bpm.engine.variable.value.DoubleValue;
import org.camunda.bpm.engine.variable.value.IntegerValue;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.ShortValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Implementation of the primitive variable value types
 *
 * @author Daniel Meyer
 *
 */
public abstract class PrimitiveValueTypeImpl extends AbstractValueTypeImpl implements PrimitiveValueType {

  private static final long serialVersionUID = 1L;

  protected Class<?> javaType;

  public PrimitiveValueTypeImpl(Class<?> javaType) {
    this(javaType.getSimpleName().toLowerCase(), javaType);
  }

  public PrimitiveValueTypeImpl(String name, Class<?> javaType) {
    super(name);
    this.javaType = javaType;
  }

  public Class<?> getJavaType() {
    return javaType;
  }

  public boolean isPrimitiveValueType() {
    return true;
  }

  public String toString() {
    return "PrimitiveValueType["+getName()+"]";
  }

  public Map<String, Object> getValueInfo(TypedValue typedValue) {
    return Collections.emptyMap();
  }

  // concrete types ///////////////////////////////////////////////////

  public static class BooleanTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public BooleanTypeImpl() {
      super(Boolean.class);
    }

    public BooleanValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.booleanValue((Boolean) value);
    }

  }

  public static class BytesTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public BytesTypeImpl() {
      super("bytes", byte[].class);
    }

    public BytesValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.byteArrayValue((byte[]) value);
    }

  }

  public static class DateTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public DateTypeImpl() {
      super(Date.class);
    }

    public DateValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.dateValue((Date) value);
    }

  }

  public static class DoubleTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public DoubleTypeImpl() {
      super(Double.class);
    }

    public DoubleValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.doubleValue((Double) value);
    }

  }

  public static class IntegerTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public IntegerTypeImpl() {
      super(Integer.class);
    }

    public IntegerValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.integerValue((Integer) value);
    }

  }

  public static class LongTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public LongTypeImpl() {
      super(Long.class);
    }

    public LongValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.longValue((Long) value);
    }

  }

  public static class NullTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public NullTypeImpl() {
      super("null", NullType.class);
    }

    public TypedValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.untypedNullValue();
    }

  }

  public static class ShortTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public ShortTypeImpl() {
      super(Short.class);
    }

    public ShortValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.shortValue((Short) value);
    }
  }

  public static class StringTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public StringTypeImpl() {
      super(String.class);
    }

    public StringValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.stringValue((String) value);
    }
  }

}
