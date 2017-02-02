/*
 * Copyright 2017 camunda services GmbH.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.camunda.bpm.engine.repository;

import java.util.List;

/**
 * An extension of the deployment interface to expose the deployed definitions.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public interface DeploymentWithDefinitions extends Deployment {

  /**
   * Returns the process definitions, which are deployed with that deployment.
   *
   * @return the process definitions which are deployed
   */
  List<ProcessDefinition> getDeployedProcessDefinitions();

  /**
   * Returns the case definitions, which are deployed with that deployment.
   *
   * @return the case definitions, which are deployed
   */
  List<CaseDefinition> getDeployedCaseDefinitions();

  /**
   * Returns the decision definitions, which are deployed with that deployment
   *
   * @return the decision definitions, which are deployed
   */
  List<DecisionDefinition> getDeployedDecisionDefinitions();

  /**
   * Returns the decision requirements definitions, which are deployed with that deployment
   *
   * @return the decision definitions, which are deployed
   */
  List<DecisionRequirementsDefinition> getDeployedDecisionRequirementsDefinitions();
}
