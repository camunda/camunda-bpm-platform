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

package org.camunda.bpm.dmn.engine;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.model.dmn.DmnModelInstance;

/**
 * A DMN engine which can parse DMN decision models
 * and evaluate decisions.
 *
 * <p>
 * A new DMN engine can be build with a DMN engine configuration
 * (see {@link DmnEngineConfiguration#buildEngine()}).
 * </p>
 */
public interface DmnEngine {

  /**
   * The configuration of this engine.
   *
   * @return the DMN engine configuration
   */
  DmnEngineConfiguration getConfiguration();

  /**
   * Parse all decisions in a DMN decision model.
   *
   * @param filename the filename of the DMN file
   * @return a list of the {@link DmnDecision}s of the DMN file
   *
   * @throws DmnEngineException
   *           if an error occurs during the parsing of the decision model
   */
  List<DmnDecision> parseDecisions(String filename);

  /**
   * Parse all decisions in a DMN decision model.
   *
   * @param inputStream the {@link InputStream} of the DMN file
   * @return a list of the {@link DmnDecision}s of the DMN file
   *
   * @throws DmnEngineException
   *           if an error occurs during the parsing of the decision model
   */
  List<DmnDecision> parseDecisions(InputStream inputStream);

  /**
   * Parse all decisions in a DMN decision model.
   *
   * @param dmnModelInstance the {@link DmnModelInstance} of the DMN decision model
   * @return a list of the {@link DmnDecision}s of the DMN file
   *
   * @throws DmnEngineException
   *           if an error occurs during the parsing of the decision model
   */
  List<DmnDecision> parseDecisions(DmnModelInstance dmnModelInstance);

  /**
   * Parse the first decision in a DMN decision model.
   *
   * @param filename the filename of the DMN file
   * @return the first {@link DmnDecision} of the DMN file
   *
   * @throws DmnEngineException
   *           if an error occurs during the parsing of the decision model
   */
  DmnDecision parseFirstDecision(String filename);

  /**
   * Parse the first decision in a DMN decision model.
   *
   * @param inputStream the {@link InputStream} of the DMN file
   * @return the first {@link DmnDecision} of the DMN file
   *
   * @throws DmnEngineException
   *           if an error occurs during the parsing of the decision model
   */
  DmnDecision parseFirstDecision(InputStream inputStream);

  /**
   * Parse the first decision in a DMN decision model.
   *
   * @param dmnModelInstance the {@link DmnModelInstance} of the DMN decision model
   * @return the first {@link DmnDecision} of the DMN file
   *
   * @throws DmnEngineException
   *           if an error occurs during the parsing of the decision model
   */
  DmnDecision parseFirstDecision(DmnModelInstance dmnModelInstance);

  /**
   * Parse the decision with the given key in a DMN decision model.
   * The key is the {@code id} attribute of the decision
   * in the DMN XML file.
   *
   * @param decisionKey the key of the decision to parse
   * @param filename the filename of the DMN file
   * @return the first {@link DmnDecision} of the DMN file
   *
   * @throws DmnEngineException
   *           if an error occurs during the parsing of the decision model
   */
  DmnDecision parseDecision(String decisionKey, String filename);

  /**
   * Parse the decision with the given key in a DMN decision model.
   * The key is the {@code id} attribute of the decision
   * in the DMN XML file.
   *
   * @param decisionKey the key of the decision to parse
   * @param inputStream the {@link InputStream} of the DMN file
   * @return the first {@link DmnDecision} of the DMN file
   *
   * @throws DmnEngineException
   *           if an error occurs during the parsing of the decision model
   */
  DmnDecision parseDecision(String decisionKey, InputStream inputStream);

  /**
   * Parse the decision with the given key in a DMN decision model.
   * The key is the {@code id} attribute of the decision
   * in the DMN XML file.
   *
   * @param decisionKey the key of the decision to parse
   * @param dmnModelInstance the {@link DmnModelInstance} of the DMN decision model
   * @return the first {@link DmnDecision} of the DMN file
   *
   * @throws DmnEngineException
   *           if an error occurs during the parsing of the decision model
   */
  DmnDecision parseDecision(String decisionKey, DmnModelInstance dmnModelInstance);

  /**
   * Evaluates a decision which is implemented as decision table.
   * @param decision the {@link DmnDecision} to evaluate
   * @param variables the variables which are available
   * @return the {@link DmnDecisionTableResult} of this evaluation
   *
   * @throws DmnEngineException
   *           if the decision is not implemented as decision table (see {@link DmnDecision#isDecisionTable()}
   * @throws DmnEngineException
   *           if an error occurs during the evaluation
   */
  DmnDecisionTableResult evaluateDecisionTable(DmnDecision decision, Map<String, Object> variables);

  /**
   * Evaluates a decision which is implemented as decision table.
   *
   * @param decision the {@link DmnDecision} to evaluate
   * @param variableContext the variables context which is available
   * @return the {@link DmnDecisionTableResult} of this evaluation
   *
   * @throws DmnEngineException
   *           if the decision is not implemented as decision table (see {@link DmnDecision#isDecisionTable()}
   * @throws DmnEngineException
   *           if an error occurs during the evaluation
   */
  DmnDecisionTableResult evaluateDecisionTable(DmnDecision decision, VariableContext variableContext);

  /**
   * Evaluates the first decision which is implemented as decision table of the DMN file.
   *
   * @param filename the filename of the DMN file
   * @param variables the variables which are available
   * @return the {@link DmnDecisionTableResult} of this evaluation
   *
   * @throws DmnEngineException
   *           if an error occurs during the parsing of the decision model
   * @throws DmnEngineException
   *           if no decision is found which is implemented as decision table
   * @throws DmnEngineException
   *           if an error occurs during the evaluation
   */
  DmnDecisionTableResult evaluateFirstDecisionTable(String filename, Map<String, Object> variables);

  /**
   * Evaluates the first decision which is implemented as decision table of the DMN file.
   *
   * @param filename the filename of the DMN file
   * @param variableContext the variables context which is available
   * @return the {@link DmnDecisionTableResult} of this evaluation
   *
   * @throws DmnEngineException
   *           if an error occurs during the parsing of the decision model
   * @throws DmnEngineException
   *           if no decision is found which is implemented as decision table
   * @throws DmnEngineException
   *           if an error occurs during the evaluation
   */
  DmnDecisionTableResult evaluateFirstDecisionTable(String filename, VariableContext variableContext);

  /**
   * Evaluates the first decision which is implemented as decision table of the DMN file.
   *
   * @param inputStream the {@link InputStream} of the DMN file
   * @param variables the variables which are available
   * @return the {@link DmnDecisionTableResult} of this evaluation
   *
   * @throws DmnEngineException
   *           if no decision is found which is implemented as decision table
   * @throws DmnEngineException
   *           if an error occurs during the evaluation
   */
  DmnDecisionTableResult evaluateFirstDecisionTable(InputStream inputStream, Map<String, Object> variables);

  /**
   * Evaluates the first decision which is implemented as decision table of the DMN file.
   *
   * @param inputStream the {@link InputStream} of the DMN file
   * @param variableContext the variables context which is available
   * @return the {@link DmnDecisionTableResult} of this evaluation
   *
   * @throws DmnEngineException
   *           if no decision is found which is implemented as decision table
   * @throws DmnEngineException
   *           if an error occurs during the evaluation
   */
  DmnDecisionTableResult evaluateFirstDecisionTable(InputStream inputStream, VariableContext variableContext);

  /**
   * Evaluates the first decision which is implemented as decision table of the DMN model instance.
   *
   * @param dmnModelInstance the {@link DmnModelInstance} of the DMN decision model
   * @param variables the variables which are available
   * @return the {@link DmnDecisionTableResult} of this evaluation
   *
   * @throws DmnEngineException
   *           if no decision is found which is implemented as decision table
   * @throws DmnEngineException
   *           if an error occurs during the evaluation
   */
  DmnDecisionTableResult evaluateFirstDecisionTable(DmnModelInstance dmnModelInstance, Map<String, Object> variables);

  /**
   * Evaluates the first decision which is implemented as decision table of the DMN model instance.
   *
   * @param dmnModelInstance the {@link DmnModelInstance} of the DMN decision model
   * @param variableContext the variables context which is available
   * @return the {@link DmnDecisionTableResult} of this evaluation
   *
   * @throws DmnEngineException
   *           if no decision is found which is implemented as decision table
   * @throws DmnEngineException
   *           if an error occurs during the evaluation
   */
  DmnDecisionTableResult evaluateFirstDecisionTable(DmnModelInstance dmnModelInstance, VariableContext variableContext);

  /**
   * Evaluates the decision with the given key in a DMN decision model.
   * The key is the {@code id} attribute of the decision in the DMN XML file.
   *
   * @param decisionKey the key of the decision to evaluated
   * @param filename the filename of the DMN file
   * @param variables the variables which are available
   * @return the {@link DmnDecisionTableResult} of this evaluation
   *
   * @throws DmnEngineException
   *           if no decision is found with the given key
   * @throws DmnEngineException
   *           if the decision is not implemented as decision table
   * @throws DmnEngineException
   *           if an error occurs during the evaluation
   */
  DmnDecisionTableResult evaluateDecisionTable(String decisionKey, String filename, Map<String, Object> variables);

  /**
   * Evaluates the decision with the given key in a DMN decision model.
   * The key is the {@code id} attribute of the decision in the DMN XML file.
   *
   * @param decisionKey the key of the decision to evaluated
   * @param filename the filename of the DMN file
   * @param variableContext the variables context which is available
   * @return the {@link DmnDecisionTableResult} of this evaluation
   *
   * @throws DmnEngineException
   *           if no decision is found with the given key
   * @throws DmnEngineException
   *           if the decision is not implemented as decision table
   * @throws DmnEngineException
   *           if an error occurs during the evaluation
   */
  DmnDecisionTableResult evaluateDecisionTable(String decisionKey, String filename, VariableContext variableContext);

  /**
   * Evaluates the decision with the given key in a DMN decision model.
   * The key is the {@code id} attribute of the decision in the DMN XML file.
   *
   * @param decisionKey the key of the decision to evaluated
   * @param inputStream the {@link InputStream} of the DMN file
   * @param variables the variables which are available
   * @return the {@link DmnDecisionTableResult} of this evaluation
   *
   * @throws DmnEngineException
   *           if no decision is found with the given key
   * @throws DmnEngineException
   *           if the decision is not implemented as decision table
   * @throws DmnEngineException
   *           if an error occurs during the evaluation
   */
  DmnDecisionTableResult evaluateDecisionTable(String decisionKey, InputStream inputStream, Map<String, Object> variables);

  /**
   * Evaluates the decision with the given key in a DMN decision model.
   * The key is the {@code id} attribute of the decision in the DMN XML file.
   *
   * @param decisionKey the key of the decision to evaluated
   * @param inputStream the {@link InputStream} of the DMN file
   * @param variableContext the variables context which is available
   * @return the {@link DmnDecisionTableResult} of this evaluation
   *
   * @throws DmnEngineException
   *           if no decision is found with the given key
   * @throws DmnEngineException
   *           if the decision is not implemented as decision table
   * @throws DmnEngineException
   *           if an error occurs during the evaluation
   */
  DmnDecisionTableResult evaluateDecisionTable(String decisionKey, InputStream inputStream, VariableContext variableContext);

  /**
   * Evaluates the decision with the given key in a DMN decision model.
   * The key is the {@code id} attribute of the decision in the DMN XML file.
   *
   * @param decisionKey the key of the decision to evaluated
   * @param dmnModelInstance the {@link DmnModelInstance} of the DMN decision model
   * @param variables the variables which are available
   * @return the {@link DmnDecisionTableResult} of this evaluation
   *
   * @throws DmnEngineException
   *           if no decision is found with the given key
   * @throws DmnEngineException
   *           if the decision is not implemented as decision table
   * @throws DmnEngineException
   *           if an error occurs during the evaluation
   */
  DmnDecisionTableResult evaluateDecisionTable(String decisionKey, DmnModelInstance dmnModelInstance, Map<String, Object> variables);

  /**
   * Evaluates the decision with the given key in a DMN decision model.
   * The key is the {@code id} attribute of the decision in the DMN XML file.
   *
   * @param decisionKey the key of the decision to evaluated
   * @param dmnModelInstance the {@link DmnModelInstance} of the DMN decision model
   * @param variableContext the variables context which is available
   * @return the {@link DmnDecisionTableResult} of this evaluation
   *
   * @throws DmnEngineException
   *           if an error occurs during the parsing of the decision model
   * @throws DmnEngineException
   *           if no decision is found with the given key
   * @throws DmnEngineException
   *           if the decision is not implemented as decision table
   * @throws DmnEngineException
   *           if an error occurs during the evaluation
   */
  DmnDecisionTableResult evaluateDecisionTable(String decisionKey, DmnModelInstance dmnModelInstance, VariableContext variableContext);

}
