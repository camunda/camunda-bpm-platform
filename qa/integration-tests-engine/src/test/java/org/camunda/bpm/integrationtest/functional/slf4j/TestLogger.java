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
package org.camunda.bpm.integrationtest.functional.slf4j;

import org.camunda.commons.logging.BaseLogger;
import org.slf4j.helpers.MessageFormatter;

public class TestLogger extends BaseLogger {

  public static final TestLogger INSTANCE = BaseLogger.createLogger(TestLogger.class, "QA", "org.camunda.bpm.qa", "01");

  /**
   * Verify that camunda commons log a messages with a single format parameter.
   * The return type of {@link MessageFormatter#format(String, Object)} changed with slf4j-api:1.6.0
   */
  public void testLogWithSingleFormatParameter() {
    logInfo("001", "This is a test of the SLF4J array formatter return type: {}", "Test test");
  }

  /**
   * Verify that camunda commons log a messages with two format parameters.
   * The return type of {@link MessageFormatter#format(String, Object, Object)} changed with slf4j-api:1.6.0
   */
  public void testLogWithTwoFormatParameters() {
    logInfo("002", "This is a test of the SLF4J array formatter return type: {}, {}", "Test test", 123);
  }

  /**
   * Verify that camunda commons log a messages which uses the array formatter (more than two format parameters).
   * The return type of {@link MessageFormatter#arrayFormat(String, Object[])} changed with slf4j-api:1.6.0
   */
  public void testLogWithArrayFormatter() {
    // we must used at least 3 parameters to reach the array formatter
    logInfo("003", "This is a test of the SLF4J array formatter return type: {}, {}, {}, {}", "Test test", 123, true, "it seems to work so slf4j >= 1.6.0 is used");
  }

}
