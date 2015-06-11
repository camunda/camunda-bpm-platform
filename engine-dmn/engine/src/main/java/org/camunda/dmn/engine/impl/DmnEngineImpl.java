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
import java.util.List;

import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelException;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.commons.utils.IoUtil;
import org.camunda.commons.utils.IoUtilException;
import org.camunda.dmn.engine.DmnDecision;
import org.camunda.dmn.engine.DmnDecisionModel;
import org.camunda.dmn.engine.DmnEngine;
import org.camunda.dmn.engine.DmnEngineConfiguration;

public class DmnEngineImpl implements DmnEngine {

  protected static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  protected DmnEngineConfiguration configuration;

  public DmnEngineImpl(DmnEngineConfiguration configuration) {
    this.configuration = configuration;
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
      DmnModelInstance modelInstance = Dmn.readModelFromStream(inputStream);
      return parseDecisionModel(modelInstance);
    }
    catch (DmnModelException e) {
      throw LOG.unableToReadInputStream(e);
    }
  }

  public DmnDecisionModel parseDecisionModel(DmnModelInstance modelInstance) {
    return configuration.getDmnTransformer().transform(modelInstance);
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

  public DmnDecision parseDecision(String filename, String decisionId) {
    DmnDecision decision = parseDecisionModel(filename).getDecision(decisionId);
    if (decision != null) {
      return decision;
    }
    else {
      throw LOG.unableToFinDecisionWithIdInFile(filename, decisionId);
    }
  }

  public DmnDecision parseDecision(InputStream inputStream, String decisionId) {
    DmnDecision decision = parseDecisionModel(inputStream).getDecision(decisionId);
    if (decision != null) {
      return decision;
    }
    else {
      throw LOG.unableToFindDecisionWithId(decisionId);
    }
  }

  public DmnDecision parseDecision(DmnModelInstance modelInstance, String decisionId) {
    DmnDecision decision = parseDecisionModel(modelInstance).getDecision(decisionId);
    if (decision != null) {
      return decision;
    }
    else {
      throw LOG.unableToFindDecisionWithId(decisionId);
    }
  }

}
