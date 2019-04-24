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
package org.camunda.bpm.engine.impl.core.variable.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;

/**
 * Maps variables in and out of a variable scope.
 *
 * @author Daniel Meyer
 *
 */
public class IoMapping {

  protected List<InputParameter> inputParameters;

  protected List<OutputParameter> outputParameters;

  public void executeInputParameters(AbstractVariableScope variableScope) {
    for (InputParameter inputParameter : getInputParameters()) {
      inputParameter.execute(variableScope);
    }
  }

  public void executeOutputParameters(AbstractVariableScope variableScope) {
    for (OutputParameter outputParameter : getOutputParameters()) {
      outputParameter.execute(variableScope);
    }
  }

  public void addInputParameter(InputParameter param) {
    if(inputParameters == null) {
      inputParameters = new ArrayList<InputParameter>();
    }
    inputParameters.add(param);
  }

  public void addOutputParameter(OutputParameter param) {
    if(outputParameters == null) {
      outputParameters = new ArrayList<OutputParameter>();
    }
    outputParameters.add(param);
  }

  public List<InputParameter> getInputParameters() {
    if(inputParameters == null) {
      return Collections.emptyList();

    } else {
      return inputParameters;
    }
  }

  public void setInputParameters(List<InputParameter> inputParameters) {
    this.inputParameters = inputParameters;
  }

  public List<OutputParameter> getOutputParameters() {
    if(outputParameters == null) {
      return Collections.emptyList();

    } else {
      return outputParameters;
    }
  }

  public void setOuputParameters(List<OutputParameter> outputParameters) {
    this.outputParameters = outputParameters;
  }

}
