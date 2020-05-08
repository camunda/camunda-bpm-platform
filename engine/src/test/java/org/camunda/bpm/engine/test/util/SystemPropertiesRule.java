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
package org.camunda.bpm.engine.test.util;

import java.util.Properties;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Restores the system properties after a test
 */
public class SystemPropertiesRule extends TestWatcher {

  protected Properties originalProperties;

  private SystemPropertiesRule() {
  }

  /**
   * Use the static method so the test is more readable
   */
  public static SystemPropertiesRule resetPropsAfterTest() {
    return new SystemPropertiesRule();
  }

  @Override
  protected void starting(Description description) {
    originalProperties = System.getProperties();
    System.setProperties(new Properties(originalProperties));
  }

  @Override
  protected void finished(Description description) {
    System.setProperties(originalProperties);
  }
}
