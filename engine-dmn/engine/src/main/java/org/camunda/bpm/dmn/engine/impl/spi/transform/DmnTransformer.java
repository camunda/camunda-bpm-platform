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

import java.util.List;

import org.camunda.bpm.dmn.engine.impl.spi.hitpolicy.DmnHitPolicyHandlerRegistry;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformerRegistry;

/**
 * DMN Transformer which creates a {@link DmnTransform} to transform a
 * DMN model instance
 */
public interface DmnTransformer {

  /**
   * @return the {@link DmnTransform} factory
   */
  DmnTransformFactory getTransformFactory();

  /**
   * @return the transform listeners
   */
  List<DmnTransformListener> getTransformListeners();

  /**
   * Set the transform listeners
   *
   * @param transformListeners the transform listeners to use
   */
  void setTransformListeners(List<DmnTransformListener> transformListeners);

  /**
   * Set the transform listeners
   *
   * @param transformListeners the transform listeners to use
   * @return this {@link DmnTransform}
   */
  DmnTransformer transformListeners(List<DmnTransformListener> transformListeners);

  /**
   * @return the {@link DmnElementTransformHandlerRegistry}
   */
  DmnElementTransformHandlerRegistry getElementTransformHandlerRegistry();

  /**
   * Set the {@link DmnElementTransformHandlerRegistry}
   *
   * @param elementTransformHandlerRegistry the registry to use
   */
  void setElementTransformHandlerRegistry(DmnElementTransformHandlerRegistry elementTransformHandlerRegistry);

  /**
   * Set the {@link DmnElementTransformHandlerRegistry}
   *
   * @param elementTransformHandlerRegistry the registry to use
   * @return this DmnTransformer
   */
  DmnTransformer elementTransformHandlerRegistry(DmnElementTransformHandlerRegistry elementTransformHandlerRegistry);

  /**
   * @return the {@link DmnDataTypeTransformerRegistry}
   */
  DmnDataTypeTransformerRegistry getDataTypeTransformerRegistry();

  /**
   * Set the {@link DmnDataTypeTransformerRegistry}
   *
   * @param dataTypeTransformerRegistry the {@link DmnDataTypeTransformerRegistry} to use
   */
  void setDataTypeTransformerRegistry(DmnDataTypeTransformerRegistry dataTypeTransformerRegistry);

  /**
   * Set the {@link DmnDataTypeTransformerRegistry}
   *
   * @param dataTypeTransformerRegistry the {@link DmnDataTypeTransformerRegistry} to use
   * @return this DmnTransformer
   */
  DmnTransformer dataTypeTransformerRegistry(DmnDataTypeTransformerRegistry dataTypeTransformerRegistry);

  /**
   * @return the {@link DmnHitPolicyHandlerRegistry}
   */
  DmnHitPolicyHandlerRegistry getHitPolicyHandlerRegistry();

  /**
   * Set the {@link DmnHitPolicyHandlerRegistry}
   *
   * @param hitPolicyHandlerRegistry the {@link DmnHitPolicyHandlerRegistry} to use
   */
  void setHitPolicyHandlerRegistry(DmnHitPolicyHandlerRegistry hitPolicyHandlerRegistry);

  /**
   * Set the {@link DmnHitPolicyHandlerRegistry}
   *
   * @param hitPolicyHandlerRegistry the {@link DmnHitPolicyHandlerRegistry} to use
   * @return this DmnTransformer
   */
  DmnTransformer hitPolicyHandlerRegistry(DmnHitPolicyHandlerRegistry hitPolicyHandlerRegistry);

  /**
   * Create a {@link DmnTransform}
   *
   * @return the {@link DmnTransform}
   */
  DmnTransform createTransform();

}
