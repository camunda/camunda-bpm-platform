/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.dmn.engine.impl;

import java.io.InputStream;
import java.util.Collection;

import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.commons.utils.IoUtil;
import org.camunda.commons.utils.IoUtilException;
import org.camunda.dmn.engine.DmnDecision;
import org.camunda.dmn.engine.DmnEngine;
import org.camunda.dmn.engine.DmnEngineConfiguration;

public class DmnEngineImpl implements DmnEngine {

  protected static final DmnParseLogger LOG = DmnLogger.PARSE_LOGGER;

  protected DmnEngineConfiguration configuration;
  protected String filename;

  public DmnEngineImpl(DmnEngineConfiguration configuration) {
    this.configuration = configuration;
  }

  public DmnEngineConfiguration getConfiguration() {
    return configuration;
  }

  public DmnDecision parseDecision(String filename) {
    return parseDecision(filename, null);
  }

  public DmnDecision parseDecision(String filename, String decisionId) {
    this.filename = filename;
    InputStream inputStream = null;

    try {
      inputStream = IoUtil.fileAsStream(filename);
      return parseDecision(inputStream, decisionId);
    }
    catch (IoUtilException e) {
      throw LOG.unableToParseDecisionFromFile(filename, decisionId, e);
    }
    finally {
      IoUtil.closeSilently(inputStream);
    }
  }

  public DmnDecision parseDecision(InputStream inputStream) {
    return parseDecision(inputStream, null);
  }

  public DmnDecision parseDecision(InputStream inputStream, String decisionId) {
    DmnModelInstance modelInstance = Dmn.readModelFromStream(inputStream);
    return parseDecision(modelInstance, decisionId);
  }

  public DmnDecision parseDecision(DmnModelInstance modelInstance) {
    return parseDecision(modelInstance, null);
  }

  public DmnDecision parseDecision(DmnModelInstance modelInstance, String decisionId) {
    Decision decision = null;
    if (decisionId != null && !decisionId.isEmpty()) {
      decision = modelInstance.getModelElementById(decisionId);
    }
    else {
      Collection<Decision> decisions = modelInstance.getModelElementsByType(Decision.class);
      if (!decisions.isEmpty()) {
        decision = decisions.iterator().next();
      }
    }

    if (decision == null) {
      throw LOG.unableToFindDecision(decisionId, filename);
    }

    return createDecision(decision);
  }

  protected DmnDecision createDecision(Decision decision) {
    Expression decisionExpression = decision.getExpression();

    if (decisionExpression instanceof DecisionTable) {
      return new DmnDecisionTable(this, decision);
    }
    else {
      throw LOG.decisionTypeNotSupported(decision);
    }

  }

}
