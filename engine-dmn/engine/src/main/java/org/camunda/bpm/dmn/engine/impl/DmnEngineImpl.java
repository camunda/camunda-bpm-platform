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

package org.camunda.bpm.dmn.engine.impl;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.context.DmnContextFactory;
import org.camunda.bpm.dmn.engine.context.DmnDecisionContext;
import org.camunda.bpm.dmn.engine.transform.DmnTransformer;
import org.camunda.bpm.model.dmn.DmnModelException;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.commons.utils.IoUtil;
import org.camunda.commons.utils.IoUtilException;

public class DmnEngineImpl implements DmnEngine {

  protected static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  protected DmnEngineConfiguration configuration;
  protected DmnTransformer transformer;
  protected DmnContextFactory contextFactory;

  public DmnEngineImpl(DmnEngineConfiguration configuration) {
    this.configuration = configuration;
    this.transformer = configuration.getTransformer();
    this.contextFactory = configuration.getDmnContextFactory();
  }

  public DmnEngineConfiguration getConfiguration() {
    return configuration;
  }

  public DmnDecisionModel parseDecisionModel(String filename) {
    try {
      InputStream inputStream = IoUtil.fileAsStream(filename);
      return parseDecisionModel(inputStream);
    }
    catch (IoUtilException e) {
      throw LOG.unableToReadFile(filename, e);
    }
  }

  public DmnDecisionModel parseDecisionModel(InputStream inputStream) {
    try {
      return transformer.createTransform().setModelInstance(inputStream).transform();
    }
    catch (DmnModelException e) {
      throw LOG.unableToReadInputStream(e);
    }
  }

  public DmnDecisionModel parseDecisionModel(DmnModelInstance modelInstance) {
    return transformer.createTransform().setModelInstance(modelInstance).transform();
  }

  public DmnDecision parseDecision(String filename) {
    List<DmnDecision> decisions = parseDecisionModel(filename).getDecisions();
    if (!decisions.isEmpty()) {
      return decisions.get(0);
    }
    else {
      throw LOG.unableToFindAnyDecisionInFile(filename);
    }
  }

  public DmnDecision parseDecision(InputStream inputStream) {
    List<DmnDecision> decisions = parseDecisionModel(inputStream).getDecisions();
    if (!decisions.isEmpty()) {
      return decisions.get(0);
    }
    else {
      throw LOG.unableToFindAnyDecision();
    }
  }

  public DmnDecision parseDecision(DmnModelInstance modelInstance) {
    List<DmnDecision> decisions = parseDecisionModel(modelInstance).getDecisions();
    if (!decisions.isEmpty()) {
      return decisions.get(0);
    }
    else {
      throw LOG.unableToFindAnyDecision();
    }
  }

  public DmnDecision parseDecision(String filename, String decisionKey) {
    DmnDecision decision = parseDecisionModel(filename).getDecision(decisionKey);
    if (decision != null) {
      return decision;
    }
    else {
      throw LOG.unableToFinDecisionWithKeyInFile(filename, decisionKey);
    }
  }

  public DmnDecision parseDecision(InputStream inputStream, String decisionKey) {
    DmnDecision decision = parseDecisionModel(inputStream).getDecision(decisionKey);
    if (decision != null) {
      return decision;
    }
    else {
      throw LOG.unableToFindDecisionWithKey(decisionKey);
    }
  }

  public DmnDecision parseDecision(DmnModelInstance modelInstance, String decisionKey) {
    DmnDecision decision = parseDecisionModel(modelInstance).getDecision(decisionKey);
    if (decision != null) {
      return decision;
    }
    else {
      throw LOG.unableToFindDecisionWithKey(decisionKey);
    }
  }

  public DmnDecisionResult evaluate(DmnDecision decision, Map<String, Object> variables) {
    DmnDecisionContext decisionContext = contextFactory.createDecisionContext(configuration);
    return decisionContext.evaluateDecision(decision, variables);
  }

  public DmnDecisionResult evaluate(DmnDecisionModel decisionModel, Map<String, Object> variables) {
    DmnDecision decision = decisionModel.getDecisions().get(0);
    return evaluate(decision, variables);
  }

  public DmnDecisionResult evaluate(DmnDecisionModel decisionModel, String decisionKey, Map<String, Object> variables) {
    DmnDecision decision = decisionModel.getDecision(decisionKey);
    return evaluate(decision, variables);
  }

}
