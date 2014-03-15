/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.cdi;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Named;

import org.camunda.bpm.engine.cdi.annotation.ProcessVariable;
import org.camunda.bpm.engine.cdi.annotation.ProcessVariableLocal;
import org.camunda.bpm.engine.cdi.impl.ProcessVariableLocalMap;
import org.camunda.bpm.engine.cdi.impl.ProcessVariableMap;

/**
 * Allows to access the process variables of a managed process instance.
 * A process instance can be managed, using the {@link BusinessProcess}-bean.
 * 
 * @author Daniel Meyer
 */
public class ProcessVariables {

  private Logger logger = Logger.getLogger(ProcessVariables.class.getName());

  @Inject private BusinessProcess businessProcess;
  @Inject private ProcessVariableMap processVariableMap;
  @Inject private ProcessVariableLocalMap processVariableLocalMap;

  protected String getVariableName(InjectionPoint ip) {
    String variableName = ip.getAnnotated().getAnnotation(ProcessVariable.class).value();
    if (variableName.length() == 0) {
      variableName = ip.getMember().getName();
    }
    return variableName;
  }

  @Produces
  @ProcessVariable
  protected Object getProcessVariable(InjectionPoint ip) {
    String processVariableName = getVariableName(ip);

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Getting process variable '" + processVariableName + "' from ProcessInstance[" + businessProcess.getProcessInstanceId() + "].");
    }

    return businessProcess.getVariable(processVariableName);
  }

  @Produces
  @Named
  protected Map<String, Object> processVariables() {
    return processVariableMap;     
  }
  
  protected String getVariableLocalName(InjectionPoint ip) {
    String variableName = ip.getAnnotated().getAnnotation(ProcessVariableLocal.class).value();
    if (variableName.length() == 0) {
      variableName = ip.getMember().getName();
    }
    return variableName;
  }

  @Produces
  @ProcessVariableLocal
  protected Object getProcessVariableLocal(InjectionPoint ip) {
    String processVariableName = getVariableLocalName(ip);

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Getting local process variable '" + processVariableName + "' from ProcessInstance[" + businessProcess.getProcessInstanceId() + "].");
    }

    return businessProcess.getVariableLocal(processVariableName);
  }

  @Produces
  @Named
  protected Map<String, Object> processVariablesLocal() {
    return processVariableLocalMap;     
  }


}
