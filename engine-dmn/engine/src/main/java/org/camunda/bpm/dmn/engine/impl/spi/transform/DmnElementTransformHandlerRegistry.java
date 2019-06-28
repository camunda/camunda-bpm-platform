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

import org.camunda.bpm.model.dmn.instance.DmnModelElementInstance;

/**
 * Registry of DMN model element transformers
 */
public interface DmnElementTransformHandlerRegistry {

  /**
   * Get the transformer for a source type
   *
   * @param sourceClass the class of the source type
   * @param <Source> the type of the transformation input
   * @param <Target> the type of the transformation output
   * @return the {@link DmnElementTransformHandler} or null if none is registered for this source type
   */
  <Source extends DmnModelElementInstance, Target> DmnElementTransformHandler<Source, Target> getHandler(Class<Source> sourceClass);

  /**
   * Register a {@link DmnElementTransformHandler} for a source type
   *
   * @param sourceClass the class of the source type
   * @param handler the handler to register
   * @param <Source> the type of the transformation input
   * @param <Target> the type of the transformation output
   */
  <Source extends DmnModelElementInstance, Target> void addHandler(Class<Source> sourceClass, DmnElementTransformHandler<Source, Target> handler);

}
