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
package org.camunda.bpm.dmn.feel.impl.scala;

import org.camunda.bpm.dmn.feel.impl.FeelException;
import org.camunda.bpm.dmn.feel.impl.scala.spin.SpinValueMapperFactory;
import org.camunda.commons.logging.BaseLogger;

public class ScalaFeelLogger extends BaseLogger {

  public static final String PROJECT_CODE = "FEEL/SCALA";
  public static final String PROJECT_LOGGER = "org.camunda.bpm.dmn.feel.scala";

  public static final ScalaFeelLogger LOGGER = createLogger(ScalaFeelLogger.class,
    PROJECT_CODE, PROJECT_LOGGER, "01");

  protected void logError(String id, String messageTemplate, Throwable t) {
    super.logError(id, messageTemplate, t);
  }

  protected void logInfo(String id, String messageTemplate, Throwable t) {
    super.logInfo(id, messageTemplate, t);
  }

  public void logSpinValueMapperDetected() {
    logInfo("001", "Spin value mapper detected");
  }

  public FeelException spinValueMapperInstantiationException(Throwable cause) {
    return new FeelException(exceptionMessage(
      "002", SpinValueMapperFactory.SPIN_VALUE_MAPPER_CLASS_NAME + " class found " +
        "on class path but cannot be instantiated."), cause);
  }

  public FeelException spinValueMapperAccessException(Throwable cause) {
    return new FeelException(exceptionMessage(
      "003", SpinValueMapperFactory.SPIN_VALUE_MAPPER_CLASS_NAME + " class found " +
        "on class path but cannot be accessed."), cause);
  }

  public FeelException spinValueMapperCastException(Throwable cause, String className) {
    return new FeelException(exceptionMessage(
      "004", SpinValueMapperFactory.SPIN_VALUE_MAPPER_CLASS_NAME + " class found " +
        "on class path but cannot be cast to " + className), cause);
  }

  public FeelException spinValueMapperException(Throwable cause) {
    return new FeelException(exceptionMessage(
      "005", "Error while looking up or registering Spin value mapper", cause));
  }

  public FeelException functionCountExceededException() {
    return new FeelException(exceptionMessage(
      "006", "Only set one return value or a function."));
  }

  public FeelException customFunctionNotFoundException() {
    return new FeelException(exceptionMessage(
      "007", "Custom function not available."));
  }

  public FeelException evaluationException(String message) {
    return new FeelException(exceptionMessage(
      "008", "Error while evaluating expression: {}", message));
  }

}
