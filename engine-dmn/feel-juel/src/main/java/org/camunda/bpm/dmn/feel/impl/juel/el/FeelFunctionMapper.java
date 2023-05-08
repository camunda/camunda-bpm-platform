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

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.impl.juel.jakarta.el.FunctionMapper;

import org.camunda.bpm.dmn.feel.impl.juel.FeelEngineLogger;
import org.camunda.bpm.dmn.feel.impl.juel.FeelLogger;

public class FeelFunctionMapper extends FunctionMapper {

  public static final FeelEngineLogger LOG = FeelLogger.ENGINE_LOGGER;

  protected static final SimpleDateFormat FEEL_DATE_AND_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  public static final String JUEL_DATE_AND_TIME_METHOD = "dateAndTime";

  protected static final Map<String, Method> methods = new HashMap<String, Method>();

  static {
    methods.put(JUEL_DATE_AND_TIME_METHOD, getMethod("parseDateAndTime", String.class));
  }

  public Method resolveFunction(String prefix, String localName) {
    return methods.get(localName);
  }

  protected static Method getMethod(String name, Class<?>... parameterTypes) {
    try {
      return FeelFunctionMapper.class.getMethod(name, parameterTypes);
    } catch (NoSuchMethodException e) {
      throw LOG.unableToFindMethod(e, name, parameterTypes);
    }
  }

  public static Date parseDateAndTime(String dateAndTimeString) {
    try {
      SimpleDateFormat clonedDateFormat = (SimpleDateFormat) FEEL_DATE_AND_TIME_FORMAT.clone();
      return clonedDateFormat.parse(dateAndTimeString);
    } catch (ParseException e) {
      throw LOG.invalidDateAndTimeFormat(dateAndTimeString, e);
    }
  }

}
