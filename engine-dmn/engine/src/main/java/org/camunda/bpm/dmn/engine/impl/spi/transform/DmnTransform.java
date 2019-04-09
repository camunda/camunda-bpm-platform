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
package org.camunda.bpm.dmn.engine.impl.spi.transform;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionRequirementsGraph;
import org.camunda.bpm.model.dmn.DmnModelInstance;

/**
 * A transform of a DMN model instance
 */
public interface DmnTransform {

  /**
   * Set the DMN model instance to transform as file.
   *
   * @param file the file of the DMN model instance
   */
  void setModelInstance(File file);

  /**
   * Set the DMN model instance to transform as file.
   *
   * @param file the file of the DMN model instance
   * @return this DmnTransform
   */
  DmnTransform modelInstance(File file);

  /**
   * Set the DMN model instance to transform as input stream.
   *
   * @param inputStream the input stream of the DMN model instance
   */
  void setModelInstance(InputStream inputStream);

  /**
   * Set the DMN model instance to transform as input stream.
   *
   * @param inputStream the input stream of the DMN model instance
   * @return this DmnTransform
   */
  DmnTransform modelInstance(InputStream inputStream);

  /**
   * Set the DMN model instance to transform.
   *
   * @param modelInstance the DMN model instance
   */
  void setModelInstance(DmnModelInstance modelInstance);

  /**
   * Set the DMN model instance to transform.
   *
   * @param modelInstance the DMN model instance
   * @return this DmnTransform
   */
  DmnTransform modelInstance(DmnModelInstance modelInstance);

  /**
   * Transform all decisions of the DMN model instance.
   */
  <T extends DmnDecision> List<T> transformDecisions();

  /**
   * Transform the decision requirements graph and all containing decisions of
   * the DMN model instance.
   */
  <T extends DmnDecisionRequirementsGraph> T transformDecisionRequirementsGraph();

}
