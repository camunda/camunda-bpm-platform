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
package org.camunda.bpm.engine.impl.el;

import java.util.List;

import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;
import org.camunda.bpm.engine.impl.javax.el.FunctionMapper;
import org.camunda.bpm.engine.impl.javax.el.VariableMapper;


/**
 * {@link ELContext} used by the process engine.
 *
 * @author Joram Barrez
 * @author Daniel Meyer
 */
public class ProcessEngineElContext extends ELContext {

  protected ELResolver elResolver;

  protected FunctionMapper functionMapper;

  public ProcessEngineElContext(List<FunctionMapper> functionMappers, ELResolver elResolver) {
    this(functionMappers);
    this.elResolver = elResolver;
  }


  public ProcessEngineElContext(List<FunctionMapper> functionMappers) {
    this.functionMapper = new CompositeFunctionMapper(functionMappers);
  }

  public ELResolver getELResolver() {
    return elResolver;
  }

  public FunctionMapper getFunctionMapper() {
    return functionMapper;
  }

  public VariableMapper getVariableMapper() {
    return null;
  }

}
