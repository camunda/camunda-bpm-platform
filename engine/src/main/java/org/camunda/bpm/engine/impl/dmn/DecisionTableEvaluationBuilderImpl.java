/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.dmn;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureOnlyOneNotNull;

import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.dmn.DecisionEvaluationBuilder;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.exception.dmn.DecisionDefinitionNotFoundException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.dmn.cmd.EvaluateDecisionTableCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * @author Kristin Polenz
 */
public class DecisionTableEvaluationBuilderImpl implements DecisionEvaluationBuilder {

  private final static DecisionLogger LOG = ProcessEngineLogger.DECISION_LOGGER;

  protected CommandExecutor commandExecutor;

  protected String decisionDefinitionKey;
  protected String decisionDefinitionId;

  protected Integer version;
  protected Map<String, Object> variables;

  protected String decisionDefinitionTenantId;
  protected boolean isTenantIdSet = false;


  public DecisionTableEvaluationBuilderImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public DecisionEvaluationBuilder variables(Map<String, Object> variables) {
    this.variables = variables;
    return this;
  }

  public DecisionEvaluationBuilder version(Integer version) {
    this.version = version;
    return this;
  }

  public DecisionEvaluationBuilder decisionDefinitionTenantId(String tenantId) {
    this.decisionDefinitionTenantId = tenantId;
    isTenantIdSet = true;
    return this;
  }

  public DecisionEvaluationBuilder decisionDefinitionWithoutTenantId() {
    this.decisionDefinitionTenantId = null;
    isTenantIdSet = true;
    return this;
  }

  public DmnDecisionTableResult evaluate() {
     ensureOnlyOneNotNull(NotValidException.class, "either decision definition id or key must be set", decisionDefinitionId, decisionDefinitionKey);

     if (isTenantIdSet && decisionDefinitionId != null) {
       throw LOG.exceptionEvaluateDecisionDefinitionByIdAndTenantId();
     }

    try {
      return commandExecutor.execute(new EvaluateDecisionTableCmd(this));
    }
    catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);
    }
    catch (DecisionDefinitionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);
    }
  }

  public static DecisionEvaluationBuilder evaluateDecisionTableByKey(CommandExecutor commandExecutor, String decisionDefinitionKey) {
    DecisionTableEvaluationBuilderImpl builder = new DecisionTableEvaluationBuilderImpl(commandExecutor);
    builder.decisionDefinitionKey = decisionDefinitionKey;
    return builder;
  }

  public static DecisionEvaluationBuilder evaluateDecisionTableById(CommandExecutor commandExecutor, String decisionDefinitionId) {
    DecisionTableEvaluationBuilderImpl builder = new DecisionTableEvaluationBuilderImpl(commandExecutor);
    builder.decisionDefinitionId = decisionDefinitionId;
    return builder;
  }

  // getters ////////////////////////////////////

  public String getDecisionDefinitionKey() {
    return decisionDefinitionKey;
  }

  public String getDecisionDefinitionId() {
    return decisionDefinitionId;
  }

  public Integer getVersion() {
    return version;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public String getDecisionDefinitionTenantId() {
    return decisionDefinitionTenantId;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

}
