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
package org.camunda.bpm.engine.test.assertions.cmmn;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.test.assertions.bpmn.AbstractProcessAssert;

public class CaseDefinitionAssert extends AbstractProcessAssert<CaseDefinitionAssert, CaseDefinition> {

  protected CaseDefinitionAssert(ProcessEngine engine, CaseDefinition actual) {
    super(engine, actual, CaseDefinitionAssert.class);
  }

  @Override
  protected CaseDefinition getCurrent() {
    return caseDefinitionQuery().singleResult();
  }


  protected static CaseDefinitionAssert assertThat(ProcessEngine engine, CaseDefinition actual) {
    return new CaseDefinitionAssert(engine, actual);
  }

  @Override
  protected String toString(CaseDefinition caseDefinition) {
    return caseDefinition != null ?
        String.format("actual %s {" +
          "id='%s', " +
          "name='%s', " +
          "resourcename='%s', " +
          "deploymentId='%s'" +
          "}",
          CaseDefinition.class.getSimpleName(),
          caseDefinition.getId(),
          caseDefinition.getName(),
          caseDefinition.getResourceName(),
          caseDefinition.getDeploymentId())
        : null;
  }

}
