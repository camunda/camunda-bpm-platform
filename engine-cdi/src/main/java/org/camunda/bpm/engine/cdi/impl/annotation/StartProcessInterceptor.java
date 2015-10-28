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
package org.camunda.bpm.engine.cdi.impl.annotation;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.cdi.annotation.ProcessVariable;
import org.camunda.bpm.engine.cdi.annotation.ProcessVariableTyped;
import org.camunda.bpm.engine.cdi.annotation.StartProcess;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;

/**
 * implementation of the {@link StartProcess} annotation
 *
 * @author Daniel Meyer
 */
@Interceptor
@StartProcess("")
public class StartProcessInterceptor implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject BusinessProcess businessProcess;

  @AroundInvoke
  public Object invoke(InvocationContext ctx) throws Exception {
    try {
      Object result = ctx.proceed();

      StartProcess startProcessAnnotation = ctx.getMethod().getAnnotation(StartProcess.class);

      String key = startProcessAnnotation.value();

      Map<String, Object> variables = extractVariables(startProcessAnnotation, ctx);

      businessProcess.startProcessByKey(key, variables);

      return result;
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if(cause != null && cause instanceof Exception) {
        throw (Exception) cause;
      } else {
        throw e;
      }
    } catch (Exception e) {
      throw new ProcessEngineException("Error while starting process using @StartProcess on method  '"+ctx.getMethod()+"': " + e.getMessage(), e);
    }
  }

  private Map<String, Object> extractVariables(StartProcess startProcessAnnotation, InvocationContext ctx) throws Exception {
    VariableMap variables = new VariableMapImpl();
    for (Field field : ctx.getMethod().getDeclaringClass().getDeclaredFields()) {
      if (!field.isAnnotationPresent(ProcessVariable.class) && !field.isAnnotationPresent(ProcessVariableTyped.class)) {
        continue;
      }
      field.setAccessible(true);

      String fieldName = null;

      ProcessVariable processStartVariable = field.getAnnotation(ProcessVariable.class);
      if (processStartVariable != null) {
        fieldName = processStartVariable.value();

      } else {
        ProcessVariableTyped processStartVariableTyped = field.getAnnotation(ProcessVariableTyped.class);
        fieldName = processStartVariableTyped.value();
      }

      if (fieldName == null || fieldName.length() == 0) {
        fieldName = field.getName();
      }
      Object value = field.get(ctx.getTarget());
      variables.put(fieldName, value);
    }

    return variables;
  }

}
