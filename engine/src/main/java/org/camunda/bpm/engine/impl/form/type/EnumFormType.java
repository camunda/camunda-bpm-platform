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
package org.camunda.bpm.engine.impl.form.type;

import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;


/**
 * @author IFS RnD
 */
public class EnumFormType extends SimpleFormFieldType {

  public final static String TYPE_NAME = "enum";

  protected Map<String, String> values;

  public EnumFormType(Map<String, String> values) {
    this.values = values;
  }

  public String getName() {
    return TYPE_NAME;
  }

  @Override
  public Object getInformation(String key) {
    if (key.equals("values")) {
      return values;
    }
    return null;
  }

   public TypedValue convertValue(TypedValue propertyValue) {
      Object value = propertyValue.getValue();
      if (value == null || String.class.isInstance(value)) {
         try {
            validateValue(value);
         } catch (ProcessEngineException e) {
            try {
               String key = validateMapAndReturnKeyIfValueProvided(String.valueOf(value));
               return Variables.stringValue(key, propertyValue.isTransient());
            } catch (Exception ex) {
               throw e;
            }
         }
         return Variables.stringValue((String) value, propertyValue.isTransient());
      } else {
         throw new ProcessEngineException("Value '" + value + "' is not of type String.");
      }
   }

  protected void validateValue(Object value) {
    if(value != null) {
      if(values != null && !values.containsKey(value)) {
        throw new ProcessEngineException("Invalid value for enum form property: " + value);
      }
    }
  }

  public Map<String, String> getValues() {
    return values;
  }
  
  protected String validateMapAndReturnKeyIfValueProvided(String propertyValue) {
     // default value is a value of the enumeration
     // try to retrieve the key
     if (propertyValue != null && values.containsValue(propertyValue)) {
        Optional<String> firstOccuranceForValue = values.entrySet().stream()
                 .filter(entry -> propertyValue.equals(entry.getValue()))
                 .map(mapper -> mapper.getKey()).findFirst();
        if (firstOccuranceForValue.isPresent()) {
           return String.valueOf(firstOccuranceForValue.get());
        }
     }
     throw new ProcessEngineException("Invalid value for enum form property: " + propertyValue);
  }

  //////////////////// deprecated ////////////////////////////////////////

  @Override
  public Object convertFormValueToModelValue(Object propertyValue) {
    try {
      validateValue(propertyValue);
   } catch (ProcessEngineException e) {
      try {
         return validateMapAndReturnKeyIfValueProvided(String.valueOf(propertyValue));
      } catch (Exception ex) {
         throw e;
      }
   }
    return propertyValue;
  }

  @Override
  public String convertModelValueToFormValue(Object modelValue) {
    if(modelValue != null) {
      if(!(modelValue instanceof String)) {
        throw new ProcessEngineException("Model value should be a String");
      }
      validateValue(modelValue);
    }
    return (String) modelValue;
  }

}
