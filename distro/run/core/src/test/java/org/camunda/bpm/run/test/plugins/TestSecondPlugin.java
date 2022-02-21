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
package org.camunda.bpm.run.test.plugins;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;

public class TestSecondPlugin implements ProcessEnginePlugin {

  protected Double parameterOne;
  protected Boolean parameterTwo;
  protected Integer parameterThree;

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {

  }

  public Double getParameterOne() {
    return parameterOne;
  }

  public void setParameterOne(Double parameterOne) {
    this.parameterOne = parameterOne;
  }

  public Boolean getParameterTwo() {
    return parameterTwo;
  }

  public void setParameterTwo(Boolean parameterTwo) {
    this.parameterTwo = parameterTwo;
  }

  public Integer getParameterThree() {
    return parameterThree;
  }

  public void setParameterThree(Integer parameterThree) {
    this.parameterThree = parameterThree;
  }

}