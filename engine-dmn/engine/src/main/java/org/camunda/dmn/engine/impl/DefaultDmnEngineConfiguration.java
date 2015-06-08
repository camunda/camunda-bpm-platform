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

public class DefaultDmnEngineConfiguration implements DmnEngineConfiguration {

  public static final String JUEL_EXPRESSION_LANGUAGE = "JUEL";

  protected String defaultExpressionLanguage;

  public DefaultDmnEngineConfiguration() {
    this(JUEL_EXPRESSION_LANGUAGE);
  }

  public DefaultDmnEngineConfiguration(String defaultExpressionLanguage) {
    this.defaultExpressionLanguage = defaultExpressionLanguage;
  }

  public String getDefaultExpressionLanguage() {
    return defaultExpressionLanguage;
  }

  public void setDefaultExpressionLanguage(String expressionLanguage) {
    defaultExpressionLanguage = expressionLanguage;
  }

  public DmnEngine buildEngine() {
    return new DmnEngineImpl(this);
  }

}
