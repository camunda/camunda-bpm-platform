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
package org.camunda.bpm.dmn.engine.impl.hitpolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedDecisionRule;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedOutput;
import org.camunda.bpm.dmn.engine.impl.DmnLogger;
import org.camunda.bpm.dmn.engine.impl.delegate.DmnDecisionTableEvaluationEventImpl;
import org.camunda.bpm.dmn.engine.impl.spi.hitpolicy.DmnHitPolicyHandler;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.dmn.BuiltinAggregator;

public abstract class AbstractCollectNumberHitPolicyHandler implements DmnHitPolicyHandler {

  public static final DmnHitPolicyLogger LOG = DmnLogger.HIT_POLICY_LOGGER;

  protected abstract BuiltinAggregator getAggregator();

  public DmnDecisionTableEvaluationEvent apply(DmnDecisionTableEvaluationEvent decisionTableEvaluationEvent) {
    String resultName = getResultName(decisionTableEvaluationEvent);
    TypedValue resultValue = getResultValue(decisionTableEvaluationEvent);

    DmnDecisionTableEvaluationEventImpl evaluationEvent = (DmnDecisionTableEvaluationEventImpl) decisionTableEvaluationEvent;
    evaluationEvent.setCollectResultName(resultName);
    evaluationEvent.setCollectResultValue(resultValue);

    return evaluationEvent;
  }

  protected String getResultName(DmnDecisionTableEvaluationEvent decisionTableEvaluationEvent) {
    for (DmnEvaluatedDecisionRule matchingRule : decisionTableEvaluationEvent.getMatchingRules()) {
      Map<String, DmnEvaluatedOutput> outputEntries = matchingRule.getOutputEntries();
      if (!outputEntries.isEmpty()) {
        return outputEntries.values().iterator().next().getOutputName();
      }
    }
    return null;
  }

  protected TypedValue getResultValue(DmnDecisionTableEvaluationEvent decisionTableEvaluationEvent) {
    List<TypedValue> values = collectSingleValues(decisionTableEvaluationEvent);
    return aggregateValues(values);
  }

  protected List<TypedValue> collectSingleValues(DmnDecisionTableEvaluationEvent decisionTableEvaluationEvent) {
    List<TypedValue> values = new ArrayList<TypedValue>();
    for (DmnEvaluatedDecisionRule matchingRule : decisionTableEvaluationEvent.getMatchingRules()) {
      Map<String, DmnEvaluatedOutput> outputEntries = matchingRule.getOutputEntries();
      if (outputEntries.size() > 1) {
        throw LOG.aggregationNotApplicableOnCompoundOutput(getAggregator(), outputEntries);
      }
      else if (outputEntries.size() == 1) {
        TypedValue typedValue = outputEntries.values().iterator().next().getValue();
        values.add(typedValue);
      }
      // ignore empty output entries
    }
    return values;
  }

  protected TypedValue aggregateValues(List<TypedValue> values) {
    if (!values.isEmpty()) {
      return aggregateNumberValues(values);
    }
    else {
      // return null if no values to aggregate
      return null;
    }

  }

  protected TypedValue aggregateNumberValues(List<TypedValue> values) {
    try {
      List<Integer> intValues = convertValuesToInteger(values);
      return Variables.integerValue(aggregateIntegerValues(intValues));
    }
    catch (IllegalArgumentException e) {
      // ignore
    }

    try {
      List<Long> longValues = convertValuesToLong(values);
      return Variables.longValue(aggregateLongValues(longValues));
    }
    catch (IllegalArgumentException e) {
      // ignore
    }

    try {
      List<Double> doubleValues = convertValuesToDouble(values);
      return Variables.doubleValue(aggregateDoubleValues(doubleValues));
    }
    catch (IllegalArgumentException e) {
      // ignore
    }

    throw LOG.unableToConvertValuesToAggregatableTypes(values, Integer.class, Long.class, Double.class);
  }

  protected abstract Integer aggregateIntegerValues(List<Integer> intValues);

  protected abstract Long aggregateLongValues(List<Long> longValues);

  protected abstract Double aggregateDoubleValues(List<Double> doubleValues);

  protected List<Integer> convertValuesToInteger(List<TypedValue> typedValues) throws IllegalArgumentException {
    List<Integer> intValues = new ArrayList<Integer>();
    for (TypedValue typedValue : typedValues) {

      if (ValueType.INTEGER.equals(typedValue.getType())) {
        intValues.add((Integer) typedValue.getValue());

      } else if (typedValue.getType() == null) {
        // check if it is an integer

        Object value = typedValue.getValue();
        if (value instanceof Integer) {
          intValues.add((Integer) value);

        } else {
          throw new IllegalArgumentException();
        }

      } else {
        // reject other typed values
        throw new IllegalArgumentException();
      }

    }
    return intValues;
  }

  protected List<Long> convertValuesToLong(List<TypedValue> typedValues) throws IllegalArgumentException {
    List<Long> longValues = new ArrayList<Long>();
    for (TypedValue typedValue : typedValues) {

      if (ValueType.LONG.equals(typedValue.getType())) {
        longValues.add((Long) typedValue.getValue());

      } else if (typedValue.getType() == null) {
        // check if it is a long or a string of a number

        Object value = typedValue.getValue();
        if (value instanceof Long) {
          longValues.add((Long) value);

        } else {
          Long longValue = Long.valueOf(value.toString());
          longValues.add(longValue);
        }

      } else {
        // reject other typed values
        throw new IllegalArgumentException();
      }

    }
    return longValues;
  }


  protected List<Double> convertValuesToDouble(List<TypedValue> typedValues) throws IllegalArgumentException {
    List<Double> doubleValues = new ArrayList<Double>();
    for (TypedValue typedValue : typedValues) {

      if (ValueType.DOUBLE.equals(typedValue.getType())) {
        doubleValues.add((Double) typedValue.getValue());

      } else if (typedValue.getType() == null) {
        // check if it is a double or a string of a decimal number

        Object value = typedValue.getValue();
        if (value instanceof Double) {
          doubleValues.add((Double) value);

        } else {
          Double doubleValue = Double.valueOf(value.toString());
          doubleValues.add(doubleValue);
        }

      } else {
        // reject other typed values
        throw new IllegalArgumentException();
      }

    }
    return doubleValues;
  }

}
