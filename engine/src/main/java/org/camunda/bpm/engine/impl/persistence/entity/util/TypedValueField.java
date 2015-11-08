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

package org.camunda.bpm.engine.impl.persistence.entity.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntityLifecycleAware;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContextListener;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFieldsImpl;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * A field what provide a typed version of a value. It can
 * be used in an entity which implements {@link ValueFields}.
 *
 * @author Philipp Ossler
 */
public class TypedValueField implements DbEntityLifecycleAware, CommandContextListener {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected String serializerName;
  protected TypedValueSerializer<?> serializer;

  protected TypedValue cachedValue;

  protected String errorMessage;

  protected final ValueFields valueFields;

  protected boolean notifyOnImplicitUpdates = false;
  protected List<TypedValueUpdateListener> updateListeners;

  public TypedValueField(ValueFields valueFields, boolean notifyOnImplicitUpdates) {
    this.valueFields = valueFields;
    this.notifyOnImplicitUpdates = notifyOnImplicitUpdates;
    this.updateListeners = new ArrayList<TypedValueUpdateListener>();
  }

  public Object getValue() {
    TypedValue typedValue = getTypedValue();
    if (typedValue != null) {
      return typedValue.getValue();
    } else {
      return null;
    }
  }

  public TypedValue getTypedValue() {
    return getTypedValue(true);
  }

  public TypedValue getTypedValue(boolean deserializeValue) {
    if (cachedValue != null && cachedValue instanceof SerializableValue && Context.getCommandContext() != null) {
      SerializableValue serializableValue = (SerializableValue) cachedValue;
      if(deserializeValue && !serializableValue.isDeserialized()) {
        // clear cached value in case it is not deserialized and user requests deserialized value
        cachedValue = null;
      }
    }

    if (cachedValue == null && errorMessage == null) {
      try {
        cachedValue = getSerializer().readValue(valueFields, deserializeValue);

        if (notifyOnImplicitUpdates && isMutableValue(cachedValue)) {
          Context.getCommandContext().registerCommandContextListener(this);
        }

      } catch (RuntimeException e) {
        // intercept the error message
        this.errorMessage = e.getMessage();
        throw e;
      }
    }
    return cachedValue;
  }

  public TypedValue setValue(TypedValue value) {
    // determine serializer to use
    serializer = getSerializers().findSerializerForValue(value);
    serializerName = serializer.getName();

    if(value instanceof UntypedValueImpl) {
      // type has been detected
      value = serializer.convertToTypedValue((UntypedValueImpl) value);
    }

    // set new value
    writeValue(value, valueFields);

    // cache the value
    cachedValue = value;

    // ensure that we serialize the object on command context flush
    // if it can be implicitly changed
    if (notifyOnImplicitUpdates && isMutableValue(cachedValue)) {
      Context.getCommandContext().registerCommandContextListener(this);
    }

    return value;
  }

  public boolean isMutable() {
    return isMutableValue(cachedValue);
  }

  @SuppressWarnings("unchecked")
  protected boolean isMutableValue(TypedValue value) {
    return((TypedValueSerializer<TypedValue>) serializer).isMutableValue(value);
  }

  protected boolean isValuedImplicitlyUpdated() {
    if (cachedValue != null && isMutableValue(cachedValue)) {
      byte[] byteArray = valueFields.getByteArrayValue();

      ValueFieldsImpl tempValueFields = new ValueFieldsImpl();
      writeValue(cachedValue, tempValueFields);

      byte[] byteArrayAfter = tempValueFields.getByteArrayValue();

      return !Arrays.equals(byteArray, byteArrayAfter);
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  protected void writeValue(TypedValue value, ValueFields valueFields) {
    ((TypedValueSerializer<TypedValue>) serializer).writeValue(value, valueFields);
  }

  public void onCommandContextClose(CommandContext commandContext) {
    if (isValuedImplicitlyUpdated()) {
      for (TypedValueUpdateListener typedValueImplicitUpdateListener : updateListeners) {
        typedValueImplicitUpdateListener.onImplicitValueUpdate(cachedValue);
      }
    }
  }

  public void onCommandFailed(CommandContext commandContext, Throwable t) {
    // ignore
  }

  public TypedValueSerializer<?> getSerializer() {
    ensureSerializerInitialized();
    return serializer;
  }

  protected void ensureSerializerInitialized() {
    if (serializerName != null && serializer == null) {
      serializer = getSerializers().getSerializerByName(serializerName);
      if (serializer == null) {
        throw LOG.serializerNotDefinedException(this);
      }
    }
  }

  public static VariableSerializers getSerializers() {
    if (Context.getCommandContext() != null) {
      return Context.getProcessEngineConfiguration().getVariableSerializers();
    } else {
      throw LOG.serializerOutOfContextException();
    }
  }

  public String getSerializerName() {
    return serializerName;
  }

  public void setSerializerName(String serializerName) {
    this.serializerName = serializerName;
  }

  public void addImplicitUpdateListener(TypedValueUpdateListener listener) {
    updateListeners.add(listener);
  }

  /**
   * @return the type name of the value
   */
  public String getTypeName() {
    if (serializerName == null) {
      return ValueType.NULL.getName();
    } else {
      return getSerializer().getType().getName();
    }
  }

  /**
   * If the variable value could not be loaded, this returns the error message.
   *
   * @return an error message indicating why the variable value could not be loaded.
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public void postLoad() {
    // make sure the serializer is initialized
    ensureSerializerInitialized();
  }

  public void clear() {
    cachedValue = null;
  }
}
