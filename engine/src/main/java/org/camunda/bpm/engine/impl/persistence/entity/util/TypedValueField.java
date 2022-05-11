/*
 *                 IFS Research & Development
 *
 *  This program is protected by copyright law and by international
 *  conventions. All licensing, renting, lending or copying (including
 *  for private use), and all other use of the program, which is not
 *  expressively permitted by IFS Research & Development (IFS), is a
 *  violation of the rights of IFS. Such violations will be reported to the
 *  appropriate authorities.
 *
 *  VIOLATIONS OF ANY COPYRIGHT IS PUNISHABLE BY LAW AND CAN LEAD
 *  TO UP TO TWO YEARS OF IMPRISONMENT AND LIABILITY TO PAY DAMAGES.
 */

/**
 *
 */
package org.camunda.bpm.engine.impl.persistence.entity.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
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
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl.DateValueImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

import com.ifsworld.fnd.common.logging.Marker;

/**
 * @author IFS RnD
 *
 */
public class TypedValueField implements DbEntityLifecycleAware, CommandContextListener {
   
   /*
    * This is Camunda class (version 7.15.0) which moved to web project and
    * introduced additional method getDateValue().
    * 
    * The method introduced to create correct DateTypeValue TEWF-937
    */

   protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

   protected String serializerName;
   protected TypedValueSerializer<?> serializer;

   protected TypedValue cachedValue;

   protected String errorMessage;

   protected final ValueFields valueFields;

   protected boolean notifyOnImplicitUpdates = false;
   protected List<TypedValueUpdateListener> updateListeners;
   
   public static final String DATE_VALUE_PREFIX = "[Date";
   private static final String DATE_FILTER_REGEX = "(\\[|Date |\\])";
   
   private static final Logger LOGGER = LogManager.getLogger(TypedValueField.class.getName());

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
     return getTypedValue(true, asTransientValue, false);
   }

   public TypedValue getTypedValueWithImplicitUpdatesSkipped(boolean asTransientValue) {
     return getTypedValue(true, asTransientValue, true);
   }

   public TypedValue getTypedValue(boolean deserializeValue, boolean asTransientValue) {
     return getTypedValue(deserializeValue, asTransientValue, false);
   }

   public TypedValue getTypedValue(boolean deserializeValue,
                                   boolean asTransientValue,
                                   boolean skipImplicitUpdates) {
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

         if (!skipImplicitUpdates && notifyOnImplicitUpdates && isMutableValue(cachedValue)) {
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
   
  
   
   /**
    * If type value is date convert it to DateValueImpl
    * 
    * @param TypedValue
    * @return
    */
   public TypedValue getDateValue(TypedValue value) {

      if (value != null && value.getValue() != null
               && value.getValue().toString().startsWith(DATE_VALUE_PREFIX)) {

         String dateTimeString = value.getValue().toString().replaceAll(DATE_FILTER_REGEX, "");

         LOGGER.info(Marker.BPA, "Date defined in script task: {}", dateTimeString);

         Instant instant = Instant.parse(dateTimeString);
         return new DateValueImpl(Date.from(instant), value.isTransient());

      }

      return value;
   }
   
   public TypedValue setValue(TypedValue value) {

      // get DateValueImpl, if Date value is exist in value
      value = getDateValue(value);

      // determine serializer to use
      serializer = getSerializers().findSerializerForValue(value,
               Context.getProcessEngineConfiguration().getFallbackSerializerFactory());
      serializerName = serializer.getName();

      if (value instanceof UntypedValueImpl) {
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
     notifyImplicitValueUpdate();
   }

   public void notifyImplicitValueUpdate() {
     if (isValuedImplicitlyUpdated()) {
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
