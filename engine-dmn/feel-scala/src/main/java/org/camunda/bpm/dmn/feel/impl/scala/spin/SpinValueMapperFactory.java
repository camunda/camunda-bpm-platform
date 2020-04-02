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
package org.camunda.bpm.dmn.feel.impl.scala.spin;

import org.camunda.bpm.dmn.feel.impl.scala.ScalaFeelLogger;
import org.camunda.feel.valuemapper.CustomValueMapper;

public class SpinValueMapperFactory {

  protected static final ScalaFeelLogger LOGGER = ScalaFeelLogger.LOGGER;

  public static final String SPIN_VALUE_MAPPER_CLASS_NAME =
    "org.camunda.spin.plugin.impl.feel.integration.SpinValueMapper";

  public CustomValueMapper createInstance() {
    Class<?> valueMapperClass = lookupClass();

    CustomValueMapper valueMapper = null;
    if (valueMapperClass != null) {
      valueMapper = newInstance(valueMapperClass);

      if (valueMapper != null) {
        LOGGER.logSpinValueMapperDetected();

      }
    } // else: engine plugin is not on classpath

    return valueMapper;
  }

  protected CustomValueMapper newInstance(Class<?> valueMapperClass) {
    try {
      return (CustomValueMapper) valueMapperClass.newInstance();

    } catch (InstantiationException e) {
      throw LOGGER.spinValueMapperInstantiationException(e);

    } catch (IllegalAccessException e) {
      throw LOGGER.spinValueMapperAccessException(e);

    } catch (ClassCastException e) {
      throw LOGGER.spinValueMapperCastException(e, CustomValueMapper.class.getName());

    } catch (Throwable e) {
      throw LOGGER.spinValueMapperException(e);

    }
  }

  protected Class<?> lookupClass() {
    try {
      return Class.forName(SPIN_VALUE_MAPPER_CLASS_NAME);

    } catch (ClassNotFoundException ignored) {
      // engine plugin is not on class path => ignore

    } catch (Throwable e) {
      throw LOGGER.spinValueMapperException(e);

    }

    return null;
  }

}
