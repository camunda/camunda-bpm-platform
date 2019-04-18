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
package org.camunda.bpm.engine.impl.form.handler;

import java.util.concurrent.Callable;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Roman Smirnov
 *
 */
public abstract class DelegateFormHandler {

  protected String deploymentId;
  protected FormHandler formHandler;

  public DelegateFormHandler(FormHandler formHandler, String deploymentId) {
    this.formHandler = formHandler;
    this.deploymentId = deploymentId;
  }

  public void parseConfiguration(Element activityElement, DeploymentEntity deployment, ProcessDefinitionEntity processDefinition, BpmnParse bpmnParse) {
    // should not be called!
  }

  protected <T> T performContextSwitch(final Callable<T> callable) {

    ProcessApplicationReference targetProcessApplication = ProcessApplicationContextUtil.getTargetProcessApplication(deploymentId);

    if(targetProcessApplication != null) {

      return Context.executeWithinProcessApplication(new Callable<T>() {
        public T call() throws Exception {
          return doCall(callable);
        }

      }, targetProcessApplication);

    } else {
      return doCall(callable);
    }
  }

  protected <T> T doCall(Callable<T> callable) {
    try {
      return callable.call();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new ProcessEngineException(e);
    }
  }

  public void submitFormVariables(final VariableMap properties, final VariableScope variableScope) {
    performContextSwitch(new Callable<Void> () {
      public Void call() throws Exception {
        Context.getProcessEngineConfiguration()
            .getDelegateInterceptor()
            .handleInvocation(new SubmitFormVariablesInvocation(formHandler, properties, variableScope));

        return null;
      }
    });
  }

  public abstract FormHandler getFormHandler();

}
