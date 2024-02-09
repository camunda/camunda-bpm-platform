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

package org.camunda.bpm.engine.test.logging;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.camunda.commons.logging.MdcAccess;

/**
 * Class that encapsulates the creation of MDC properties such as Logging Context Parameters or any other third party
 * property that might be requested by the test.
 * <p>
 * It also provides useful cleanup and assertion methods for test cases.
 */
public class TestMdcFacade {

  private final Map<String, String> keyValuePairs;

  private TestMdcFacade() {
    this.keyValuePairs = new HashMap<>();
  }

  public static TestMdcFacade empty() {
    return new TestMdcFacade();
  }

  /**
   * Creates a new Test MDC Facade to populate the MDC with all the Default Logging Context Parameters.
   */
  public static TestMdcFacade defaultLoggingContextParameters(String activityId,
                                                              String activityName,
                                                              String applicationName,
                                                              String businessKey,
                                                              String processDefinitionId,
                                                              String processDefinitionKey,
                                                              String processInstanceId,
                                                              String tenantId,
                                                              String engineName) {

    return new TestMdcFacade().withDefaultLoggingContextParameters(
        activityId,
        activityName,
        applicationName,
        businessKey,
        processDefinitionId,
        processDefinitionKey,
        processInstanceId,
        tenantId,
        engineName
    );
  }

  /**
   * Constructor using the default Logging Context Parameter values.
   */
  public TestMdcFacade withDefaultLoggingContextParameters(String activityId,
                                                           String activityName,
                                                           String applicationName,
                                                           String businessKey,
                                                           String processDefinitionId,
                                                           String processDefinitionKey,
                                                           String processInstanceId,
                                                           String tenantId,
                                                           String engineName) {

    Map<String, String> result = new HashMap<>();

    result.put("activityId", activityId);
    result.put("activityName", activityName);
    result.put("applicationName", applicationName);
    result.put("businessKey", businessKey);
    result.put("processDefinitionId", processDefinitionId);
    result.put("processDefinitionKey", processDefinitionKey);
    result.put("processInstanceId", processInstanceId);
    result.put("tenantId", tenantId);
    result.put("engineName", engineName);

    return withMDCProperties(result);
  }

  /**
   * Populate the MDC with custom key value pairs
   *
   * @param keyValuePairs value pairs that are to be inserted in the MDC that represent MDC Property pairs.
   */
  public TestMdcFacade withMDCProperties(Map<String, String> keyValuePairs) {
    this.keyValuePairs.putAll(keyValuePairs);
    this.keyValuePairs.forEach(MdcAccess::put);

    return this;
  }

  /**
   * Inserts an additional property outside the context of Logging Context Parameters.
   *
   * @param key   the key of the property
   * @param value the value of the property
   */
  public TestMdcFacade withMDCProperty(String key, String value) {
    keyValuePairs.put(key, value);
    MdcAccess.put(key, value);

    return this;
  }

  /**
   * Clears any property that was inserted in the MDC.
   */
  public void clear() {
    this.keyValuePairs.forEach((k, v) -> MdcAccess.remove(k));
  }

  /**
   * Asserts all test inserted properties in the MDC are still there.
   */
  public void assertAllInsertedPropertiesAreInMdc() {
    this.keyValuePairs.forEach((k, v) -> {
      assertThat(MdcAccess.get(k)).isNotNull();
    });
  }
}
