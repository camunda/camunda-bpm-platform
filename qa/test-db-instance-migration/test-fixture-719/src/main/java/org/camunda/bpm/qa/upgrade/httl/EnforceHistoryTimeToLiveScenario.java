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

package org.camunda.bpm.qa.upgrade.httl;

import org.camunda.bpm.engine.test.Deployment;

public class EnforceHistoryTimeToLiveScenario {

  @Deployment
  public static String processWithoutHTTL() {
    return "org/camunda/bpm/qa/upgrade/httl/process_without_httl.bpmn20.xml";
  }

  @Deployment
  public static String decisionWithoutHTTL() {
    return "org/camunda/bpm/qa/upgrade/httl/decision_without_httl.dmn";
  }

  @Deployment
  public static String caseWithoutHTTL() {
    return "org/camunda/bpm/qa/upgrade/httl/case_without_httl.cmmn";
  }
}
