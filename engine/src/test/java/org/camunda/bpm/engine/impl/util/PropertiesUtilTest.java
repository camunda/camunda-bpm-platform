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
package org.camunda.bpm.engine.impl.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.camunda.commons.testing.WatchLogger;
import org.junit.Rule;
import org.junit.Test;

public class PropertiesUtilTest {

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  @Test
  @WatchLogger(loggerNames = {"org.camunda.bpm.engine.util"}, level = "DEBUG")
  public void shouldLogMissingFile() {
    // given
    String invalidFile = "/missingProps.properties";

    // when
    PropertiesUtil.getProperties(invalidFile);

    // then
    String logMessage = String.format("Could not find the '%s' file on the classpath. " +
        "If you have removed it, please restore it.", invalidFile);
    assertThat(loggingRule.getFilteredLog(logMessage)).hasSize(1);
  }
}
