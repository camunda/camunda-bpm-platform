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

import static com.ifsworld.fnd.bpa.Constants.ATTR_DATE_MASK;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;


/**
 * @author IFS RnD
 */
public class DateFormType extends AbstractFormFieldType {

  public final static String TYPE_NAME = "date";

  protected String datePattern;
  protected DateFormat dateFormat;
  protected DateFormat odataDateFormat;

  public DateFormType(String datePattern) {
    this.datePattern = datePattern;
    this.dateFormat = new SimpleDateFormat(datePattern);
    this.odataDateFormat = new SimpleDateFormat(ATTR_DATE_MASK);
  }

  public String getName() {
    return TYPE_NAME;
  }

  public Object getInformation(String key) {
    if ("datePattern".equals(key)) {
      return datePattern;
    }
    return null;
  }

  public TypedValue convertToModelValue(TypedValue propertyValue) {
    Object value = propertyValue.getValue();
    if(value == null) {
      return Variables.dateValue(null, propertyValue.isTransient());
    }
    else if(value instanceof Date) {
      return Variables.dateValue((Date) value, propertyValue.isTransient());
    }
    else if(value instanceof String) {
      String strValue = ((String) value).trim();
      if (strValue.isEmpty()) {
        return Variables.dateValue(null, propertyValue.isTransient());
      }
      try {
        return Variables.dateValue((Date) dateFormat.parseObject(strValue), propertyValue.isTransient());
      } catch (ParseException e) {
         try {
            return Variables.dateValue((Date) odataDateFormat.parseObject(strValue), propertyValue.isTransient());
         } catch (ParseException ex) {
            // Throw original error if this fails
         }
        throw new ProcessEngineException("Could not parse value '"+value+"' as date using date format '"+datePattern+"'.");
      }
    }
    else {
      throw new ProcessEngineException("Value '"+value+"' cannot be transformed into a Date.");
    }
  }

  public TypedValue convertToFormValue(TypedValue modelValue) {
    if(modelValue.getValue() == null) {
      return Variables.stringValue(null, modelValue.isTransient());
    } else if(modelValue.getType() == ValueType.DATE) {
       return Variables.stringValue(dateFormat.format(modelValue.getValue()), modelValue.isTransient());
     } else if(modelValue.getType() == ValueType.STRING) {
        // added this option for untyped Date data
        String value = modelValue.getValue().toString();
        
        try {
           // validate parsing .. if it fails send a consistent message as the previous implementation
           dateFormat.parse(value);
        } catch (ParseException e) {
           throw new ProcessEngineException("Expected value to be of type '"+ValueType.DATE+"' but got '"+modelValue.getType()+"'.");
        }
        
      return Variables.stringValue(value, modelValue.isTransient());
    }
    else {
      throw new ProcessEngineException("Expected value to be of type '"+ValueType.DATE+"' but got '"+modelValue.getType()+"'.");
    }
  }

  // deprecated //////////////////////////////////////////////////////////

  public Object convertFormValueToModelValue(Object propertyValue) {
    if (propertyValue==null || "".equals(propertyValue)) {
      return null;
    }
    
    // added this check to handle both typed and untyped data
    if(propertyValue instanceof Date) {
       return propertyValue;
    }
    
    try {
      return dateFormat.parseObject(propertyValue.toString());
    } catch (ParseException e) {
      try {
        return odataDateFormat.parseObject(propertyValue.toString());
      } catch (ParseException ex) {
        // Throw original error if this fails
      }
      throw new ProcessEngineException("invalid date value "+propertyValue);
    }
  }

  public String convertModelValueToFormValue(Object modelValue) {
    if (modelValue==null) {
      return null;
    }
    return dateFormat.format(modelValue);
  }

}
