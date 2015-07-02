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

package org.camunda.dmn.engine.impl;

import org.camunda.dmn.engine.DmnEngine;
import org.camunda.dmn.engine.DmnEngineConfiguration;
import org.camunda.dmn.engine.context.DmnContextFactory;
import org.camunda.dmn.engine.impl.context.DmnContextFactoryImpl;
import org.camunda.dmn.engine.impl.transform.DmnElementHandlerRegistryImpl;
import org.camunda.dmn.engine.impl.transform.DmnTransformerImpl;
import org.camunda.dmn.engine.transform.DmnElementHandlerRegistry;
import org.camunda.dmn.engine.transform.DmnTransformer;
import org.camunda.dmn.juel.JuelScriptEngineFactory;

public class DmnEngineConfigurationImpl implements DmnEngineConfiguration {

  protected DmnTransformer transformer;
  protected String defaultExpressionLanguage;
  protected DmnContextFactory contextFactory;

  public DmnEngineConfigurationImpl() {
    transformer = new DmnTransformerImpl(new DmnElementHandlerRegistryImpl());
    defaultExpressionLanguage = JuelScriptEngineFactory.NAME;
    contextFactory = new DmnContextFactoryImpl();
  }

  public void setDmnTransformer(DmnTransformer transformer) {
    this.transformer = transformer;
  }

  public DmnTransformer getDmnTransformer() {
    return transformer;
  }

  public void setDmnElementHandlerRegistry(DmnElementHandlerRegistry elementHandlerRegistry) {
    this.transformer.setElementHandlerRegistry(elementHandlerRegistry);
  }

  public DmnElementHandlerRegistry getDmnElementHandlerRegistry() {
    return transformer.getElementHandlerRegistry();
  }

  public void setDefaultExpressionLanguage(String expressionLanguage) {
    this.defaultExpressionLanguage = expressionLanguage;
  }

  public String getDefaultExpressionLanguage() {
    return defaultExpressionLanguage;
  }

  public void setDmnContextFactory(DmnContextFactory contextFactory) {
    this.contextFactory = contextFactory;
  }

  public DmnContextFactory getDmnContextFactory() {
    return contextFactory;
  }

  public DmnEngine buildEngine() {
    return new DmnEngineImpl(this);
  }

}
