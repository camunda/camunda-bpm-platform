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
package org.camunda.bpm.engine.impl.form.validator;

/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractNumericValidator implements FormFieldValidator {

  @Override
  public boolean validate(Object submittedValue, FormFieldValidatorContext validatorContext) {

    if (submittedValue == null) {
      return isNullValid();
    }

    String configurationString = validatorContext.getConfiguration();

    // Double

    if (submittedValue instanceof Double) {
      Double configuration = null;
      try {
        configuration = Double.parseDouble(configurationString);
      } catch( NumberFormatException e) {
        throw new FormFieldConfigurationException(configurationString, "Cannot validate Double value "+submittedValue +": configuration "+configurationString+" cannot be parsed as Double.");
      }
      return validate((Double) submittedValue, configuration);
    }

    // Float

    if (submittedValue instanceof Float) {
      Float configuration = null;
      try {
        configuration = Float.parseFloat(configurationString);
      } catch( NumberFormatException e) {
        throw new FormFieldConfigurationException(configurationString, "Cannot validate Float value "+submittedValue +": configuration "+configurationString+" cannot be parsed as Float.");
      }
      return validate((Float) submittedValue, configuration);
    }

    // Long

    if (submittedValue instanceof Long) {
      Long configuration = null;
      try {
        configuration = Long.parseLong(configurationString);
      } catch(NumberFormatException e) {
        throw new FormFieldConfigurationException(configurationString, "Cannot validate Long value "+submittedValue +": configuration "+configurationString+" cannot be parsed as Long.");
      }
      return validate((Long) submittedValue, configuration);
    }

    // Integer

    if (submittedValue instanceof Integer) {
      Integer configuration = null;
      try {
        configuration = Integer.parseInt(configurationString);
      } catch( NumberFormatException e) {
        throw new FormFieldConfigurationException(configurationString, "Cannot validate Integer value "+submittedValue +": configuration "+configurationString+" cannot be parsed as Integer.");
      }
      return validate((Integer) submittedValue, configuration);
    }

    // Short

    if (submittedValue instanceof Short) {
      Short configuration = null;
      try {
        configuration = Short.parseShort(configurationString);
      } catch( NumberFormatException e) {
        throw new FormFieldConfigurationException(configurationString, "Cannot validate Short value "+submittedValue +": configuration "+configurationString+" cannot be parsed as Short.");
      }
      return validate((Short) submittedValue, configuration);
    }

    throw new FormFieldValidationException("Numeric validator "+getClass().getSimpleName()+" cannot be used on non-numeric value "+submittedValue);
  }

  protected boolean isNullValid() {
    return true;
  }

  protected abstract boolean validate(Integer submittedValue, Integer configuration);

  protected abstract boolean validate(Long submittedValue, Long configuration);

  protected abstract boolean validate(Double submittedValue, Double configuration);

  protected abstract boolean validate(Float submittedValue, Float configuration);

  protected abstract boolean validate(Short submittedValue, Short configuration);

}
