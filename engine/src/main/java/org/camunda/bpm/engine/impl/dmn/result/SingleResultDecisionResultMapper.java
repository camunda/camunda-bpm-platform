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
package org.camunda.bpm.engine.impl.dmn.result;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries;
import org.camunda.bpm.dmn.engine.DmnEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.dmn.DecisionLogger;
import org.camunda.bpm.engine.variable.Variables;

/**
 * Maps the decision result to pairs of output name and untyped entries.
 *
 * @author Philipp Ossler
 */
public class SingleResultDecisionResultMapper implements DecisionResultMapper {

  protected static final DecisionLogger LOG = ProcessEngineLogger.DECISION_LOGGER;

  @Override
  public Object mapDecisionResult(DmnDecisionResult decisionResult) {
    try {
      DmnDecisionResultEntries singleResult = decisionResult.getSingleResult();
      if (singleResult != null) {
        return singleResult.getEntryMap();
      } else
        return Variables.untypedNullValue();
    } catch (DmnEngineException e) {
      throw LOG.decisionResultMappingException(decisionResult, this, e);
    }
  }

  @Override
  public String toString() {
    return "SingleResultDecisionResultMapper{}";
  }

}
