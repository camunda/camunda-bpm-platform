/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.dmn.juel;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.VariableMapper;
import javax.script.ScriptContext;

import de.odysseus.el.util.SimpleResolver;

public class JuelScriptElContext extends ELContext {

  protected ScriptContext context;
  protected ExpressionFactory expressionFactory;

  public JuelScriptElContext(ScriptContext context, ExpressionFactory expressionFactory) {
    this.context = context;
    this.expressionFactory = expressionFactory;
  }

  public ELResolver getELResolver() {
    CompositeELResolver resolver = new CompositeELResolver();
    resolver.add(new ArrayELResolver());
    resolver.add(new ListELResolver());
    resolver.add(new MapELResolver());
    resolver.add(new ResourceBundleELResolver());
    resolver.add(new BeanELResolver());
    return new SimpleResolver(resolver);
  }

  public FunctionMapper getFunctionMapper() {
    return new JuelScriptContextFunctionMapper(context);
  }

  public VariableMapper getVariableMapper() {
    return new JuelScriptContextVariableMapper(context, expressionFactory);
  }

}
