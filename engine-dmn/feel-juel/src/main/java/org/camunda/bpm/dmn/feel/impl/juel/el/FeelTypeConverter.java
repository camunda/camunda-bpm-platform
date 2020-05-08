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
package org.camunda.bpm.dmn.feel.impl.juel.el;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.el.ELException;

import org.camunda.bpm.dmn.feel.impl.juel.FeelEngineLogger;
import org.camunda.bpm.dmn.feel.impl.juel.FeelLogger;

import de.odysseus.el.misc.TypeConverterImpl;

public class FeelTypeConverter extends TypeConverterImpl {

  public static final FeelEngineLogger LOG = FeelLogger.ENGINE_LOGGER;

  @Override
  protected Boolean coerceToBoolean(Object value) {
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    else {
      throw LOG.unableToConvertValue(value, Boolean.class);
    }
  }

  @Override
  protected BigDecimal coerceToBigDecimal(Object value) {
    if (value instanceof BigDecimal) {
      return (BigDecimal)value;
    }
    else if (value instanceof BigInteger) {
      return new BigDecimal((BigInteger)value);
    }
    else if (value instanceof Number) {
      return new BigDecimal(((Number)value).doubleValue());
    }
    else {
      throw LOG.unableToConvertValue(value, BigDecimal.class);
    }
  }

  @Override
  protected BigInteger coerceToBigInteger(Object value) {
    if (value instanceof BigInteger) {
      return (BigInteger)value;
    }
    else if (value instanceof BigDecimal) {
      return ((BigDecimal)value).toBigInteger();
    }
    else if (value instanceof Number) {
      return BigInteger.valueOf(((Number)value).longValue());
    }
    else {
      throw LOG.unableToConvertValue(value, BigInteger.class);
    }
  }

  @Override
  protected Double coerceToDouble(Object value) {
    if (value instanceof Double) {
      return (Double)value;
    }
    else if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    else {
      throw LOG.unableToConvertValue(value, Double.class);
    }
  }

  @Override
  protected Long coerceToLong(Object value) {
    if (value instanceof Long) {
      return (Long)value;
    }
    else if (value instanceof Number && isLong((Number) value)) {
      return ((Number) value).longValue();
    }
    else {
      throw LOG.unableToConvertValue(value, Long.class);
    }
  }

  @Override
  protected String coerceToString(Object value) {
    if (value instanceof String) {
      return (String)value;
    }
    else if (value instanceof Enum<?>) {
      return ((Enum<?>)value).name();
    }
    else {
      throw LOG.unableToConvertValue(value, String.class);
    }
  }

  @Override
  public <T> T convert(Object value, Class<T> type) throws ELException {
    try {
      return super.convert(value, type);
    }
    catch (ELException e) {
      throw LOG.unableToConvertValue(value, type, e);
    }
  }

  protected boolean isLong(Number value) {
    double doubleValue = value.doubleValue();
    return doubleValue == (long) doubleValue;
  }

}
