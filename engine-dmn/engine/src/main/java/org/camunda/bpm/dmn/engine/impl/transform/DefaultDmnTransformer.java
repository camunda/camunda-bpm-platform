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
package org.camunda.bpm.dmn.engine.impl.transform;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.dmn.engine.impl.hitpolicy.DefaultHitPolicyHandlerRegistry;
import org.camunda.bpm.dmn.engine.impl.spi.hitpolicy.DmnHitPolicyHandlerRegistry;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformHandlerRegistry;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnTransform;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnTransformFactory;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnTransformListener;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnTransformer;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformerRegistry;
import org.camunda.bpm.dmn.engine.impl.type.DefaultDataTypeTransformerRegistry;

public class DefaultDmnTransformer implements DmnTransformer {

  protected DmnTransformFactory transformFactory = new DefaultTransformFactory();
  protected List<DmnTransformListener> transformListeners = new ArrayList<DmnTransformListener>();
  protected DmnElementTransformHandlerRegistry elementTransformHandlerRegistry = new DefaultElementTransformHandlerRegistry();
  protected DmnDataTypeTransformerRegistry dataTypeTransformerRegistry = new DefaultDataTypeTransformerRegistry();
  protected DmnHitPolicyHandlerRegistry hitPolicyHandlerRegistry = new DefaultHitPolicyHandlerRegistry();

  public DmnTransformFactory getTransformFactory() {
    return transformFactory;
  }

  public List<DmnTransformListener> getTransformListeners() {
    return transformListeners;
  }

  public void setTransformListeners(List<DmnTransformListener> transformListeners) {
    this.transformListeners = transformListeners;
  }

  public DmnTransformer transformListeners(List<DmnTransformListener> transformListeners) {
    setTransformListeners(transformListeners);
    return this;
  }

  public DmnElementTransformHandlerRegistry getElementTransformHandlerRegistry() {
    return elementTransformHandlerRegistry;
  }

  public void setElementTransformHandlerRegistry(DmnElementTransformHandlerRegistry elementTransformHandlerRegistry) {
    this.elementTransformHandlerRegistry = elementTransformHandlerRegistry;
  }

  public DmnTransformer elementTransformHandlerRegistry(DmnElementTransformHandlerRegistry elementTransformHandlerRegistry) {
    setElementTransformHandlerRegistry(elementTransformHandlerRegistry);
    return this;
  }

  public DmnDataTypeTransformerRegistry getDataTypeTransformerRegistry() {
    return dataTypeTransformerRegistry;
  }

  public void setDataTypeTransformerRegistry(DmnDataTypeTransformerRegistry dataTypeTransformerRegistry) {
    this.dataTypeTransformerRegistry = dataTypeTransformerRegistry;
  }

  public DmnTransformer dataTypeTransformerRegistry(DmnDataTypeTransformerRegistry dataTypeTransformerRegistry) {
    setDataTypeTransformerRegistry(dataTypeTransformerRegistry);
    return this;
  }

  public DmnHitPolicyHandlerRegistry getHitPolicyHandlerRegistry() {
    return hitPolicyHandlerRegistry;
  }

  public void setHitPolicyHandlerRegistry(DmnHitPolicyHandlerRegistry hitPolicyHandlerRegistry) {
    this.hitPolicyHandlerRegistry = hitPolicyHandlerRegistry;
  }

  public DmnTransformer hitPolicyHandlerRegistry(DmnHitPolicyHandlerRegistry hitPolicyHandlerRegistry) {
    setHitPolicyHandlerRegistry(hitPolicyHandlerRegistry);
    return this;
  }

  public DmnTransform createTransform() {
    return transformFactory.createTransform(this);
  }

}
