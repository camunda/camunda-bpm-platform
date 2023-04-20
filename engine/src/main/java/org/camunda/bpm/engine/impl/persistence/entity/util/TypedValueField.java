/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntityLifecycleAware;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContextListener;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFieldsImpl;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializerFactory;
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
    this.updateListeners = new ArrayList<>();
  }

  public Object getValue() {
    TypedValue typedValue = getTypedValue(false);
    if (typedValue != null) {
      return typedValue.getValue();
    } else {
      return null;
    }
  }

  public TypedValue getTypedValue(boolean asTransientValue) {
    return getTypedValue(true, asTransientValue);
  }

  public TypedValue getTypedValue(boolean deserializeValue, boolean asTransientValue) {
    if (Context.getCommandContext() != null) {
      // in some circumstances we must invalidate the cached value instead of returning it

      if (cachedValue != null && cachedValue instanceof SerializableValue) {
        SerializableValue serializableValue = (SerializableValue) cachedValue;
        if(deserializeValue && !serializableValue.isDeserialized()) {
          // clear cached value in case it is not deserialized and user requests deserialized value
          cachedValue = null;
        }
      }

      if (cachedValue != null && (asTransientValue ^ cachedValue.isTransient())) {
        // clear cached value if the value is not transient, but a transient value is requested
        cachedValue = null;
      }
    }

    if (cachedValue == null && errorMessage == null) {
      try {
        cachedValue = getSerializer().readValue(valueFields, deserializeValue, asTransientValue);

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
    serializer = getSerializers().findSerializerForValue(value,
        Context.getProcessEngineConfiguration().getFallbackSerializerFactory());
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
    return((TypedValueSerializer<TypedValue>) getSerializer()).isMutableValue(value);
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
    ((TypedValueSerializer<TypedValue>) getSerializer()).writeValue(value, valueFields);
  }

  @Override
  public void onCommandContextClose(CommandContext commandContext) {
    notifyImplicitValueUpdateIfEnabled();
  }

  public void notifyImplicitValueUpdateIfEnabled() {
    if (isImplicitVariableUpdateDetectionEnabled() && isValuedImplicitlyUpdated()) {
      for (TypedValueUpdateListener typedValueImplicitUpdateListener : updateListeners) {
        typedValueImplicitUpdateListener.onImplicitValueUpdate(cachedValue);
      }
    }
  }

  @Override
  public void onCommandFailed(CommandContext commandContext, Throwable t) {
    // ignore
  }

  public TypedValueSerializer<?> getSerializer() {
    ensureSerializerInitialized();
    return serializer;
  }

  private static boolean isImplicitVariableUpdateDetectionEnabled() {
    return Context.getProcessEngineConfiguration().isImplicitVariableUpdateDetectionEnabled();
  }

  protected void ensureSerializerInitialized() {
    if (serializerName != null && serializer == null) {
      serializer = getSerializers().getSerializerByName(serializerName);

      if (serializer == null) {
        serializer = getFallbackSerializer(serializerName);
      }

      if (serializer == null) {
        throw LOG.serializerNotDefinedException(this);
      }
    }
  }

  public static VariableSerializers getSerializers() {
    if (Context.getCommandContext() != null) {
      VariableSerializers variableSerializers = Context.getProcessEngineConfiguration().getVariableSerializers();
      VariableSerializers paSerializers = getCurrentPaSerializers();

      if (paSerializers != null) {
        return variableSerializers.join(paSerializers);
      }
      else {
        return variableSerializers;
      }
    } else {
      throw LOG.serializerOutOfContextException();
    }
  }

  public static TypedValueSerializer<?> getFallbackSerializer(String serializerName) {
    if (Context.getProcessEngineConfiguration() != null) {
      VariableSerializerFactory fallbackSerializerFactory = Context.getProcessEngineConfiguration().getFallbackSerializerFactory();
      if (fallbackSerializerFactory != null) {
        return fallbackSerializerFactory.getSerializer(serializerName);
      }
      else {
        return null;
      }
    }
    else {
      throw LOG.serializerOutOfContextException();
    }
  }

  protected static VariableSerializers getCurrentPaSerializers() {
    if (Context.getCurrentProcessApplication() != null) {
      ProcessApplicationReference processApplicationReference = Context.getCurrentProcessApplication();
      try {
        ProcessApplicationInterface processApplicationInterface = processApplicationReference.getProcessApplication();

        ProcessApplicationInterface rawPa = processApplicationInterface.getRawObject();
        if (rawPa instanceof AbstractProcessApplication) {
          return ((AbstractProcessApplication) rawPa).getVariableSerializers();
        }
        else {
          return null;
        }
      } catch (ProcessApplicationUnavailableException e) {
        throw LOG.cannotDeterminePaDataformats(e);
      }
    }
    else {
      return null;
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
  }

  public void clear() {
    cachedValue = null;
  }
}
