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
import org.camunda.bpm.engine.cdi.annotation.ProcessVariableLocalTyped;
import org.camunda.bpm.engine.cdi.annotation.ProcessVariableTyped;
import org.camunda.bpm.engine.cdi.impl.ProcessVariableLocalMap;
import org.camunda.bpm.engine.cdi.impl.ProcessVariableMap;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;

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

  protected String getVariableTypedName(InjectionPoint ip) {
    String variableName = ip.getAnnotated().getAnnotation(ProcessVariableTyped.class).value();
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

  /**
   * @since 7.3
   */
  @Produces
  @ProcessVariableTyped
  protected TypedValue getProcessVariableTyped(InjectionPoint ip) {
    String processVariableName = getVariableTypedName(ip);

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Getting typed process variable '" + processVariableName + "' from ProcessInstance[" + businessProcess.getProcessInstanceId() + "].");
    }

    return businessProcess.getVariableTyped(processVariableName);
  }

  @Produces
  @Named
  protected Map<String, Object> processVariables() {
    return processVariableMap;
  }

  /**
   * @since 7.3
   */
  @Produces
  @Named
  protected VariableMap processVariableMap() {
    return processVariableMap;
  }

  protected String getVariableLocalName(InjectionPoint ip) {
    String variableName = ip.getAnnotated().getAnnotation(ProcessVariableLocal.class).value();
    if (variableName.length() == 0) {
      variableName = ip.getMember().getName();
    }
    return variableName;
  }

  protected String getVariableLocalTypedName(InjectionPoint ip) {
    String variableName = ip.getAnnotated().getAnnotation(ProcessVariableLocalTyped.class).value();
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

  /**
   * @since 7.3
   */
  @Produces
  @ProcessVariableLocalTyped
  protected TypedValue getProcessVariableLocalTyped(InjectionPoint ip) {
    String processVariableName = getVariableLocalTypedName(ip);

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Getting local typed process variable '" + processVariableName + "' from ProcessInstance[" + businessProcess.getProcessInstanceId() + "].");
    }

    return businessProcess.getVariableLocalTyped(processVariableName);
  }

  @Produces
  @Named
  protected Map<String, Object> processVariablesLocal() {
    return processVariableLocalMap;
  }

  /**
   * @since 7.3
   */
  @Produces
  @Named
  protected VariableMap processVariableMapLocal() {
    return processVariableLocalMap;
  }


}
