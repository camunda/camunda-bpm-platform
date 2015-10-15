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

package org.camunda.bpm.dmn.feel.impl;

import java.util.Properties;
import javax.el.ELException;
import javax.el.ExpressionFactory;

import org.camunda.bpm.dmn.feel.FeelEngine;
import org.camunda.bpm.dmn.feel.FeelEngineProvider;
import org.camunda.bpm.dmn.feel.impl.el.ElContextFactory;
import org.camunda.bpm.dmn.feel.impl.el.FeelElContextFactory;
import org.camunda.bpm.dmn.feel.impl.el.FeelTypeConverter;
import org.camunda.bpm.dmn.feel.impl.transform.FeelToJuelTransform;
import org.camunda.bpm.dmn.feel.impl.transform.FeelToJuelTransformImpl;

import de.odysseus.el.ExpressionFactoryImpl;

public class FeelEngineProviderImpl implements FeelEngineProvider {

  public static final FeelEngineLogger LOG = FeelLogger.ENGINE_LOGGER;

  protected FeelEngine feelEngine;

  public FeelEngineProviderImpl() {
    feelEngine = createFeelEngine();
  }

  public FeelEngine createInstance() {
    return feelEngine;
  }

  protected FeelEngine createFeelEngine() {
    FeelToJuelTransform transform = createFeelToJuelTransform();
    ExpressionFactory expressionFactory = createExpressionFactory();
    ElContextFactory elContextFactory = createElContextFactory();
    return new FeelEngineImpl(transform, expressionFactory, elContextFactory);
  }

  protected FeelToJuelTransform createFeelToJuelTransform() {
    return new FeelToJuelTransformImpl();
  }

  protected ExpressionFactory createExpressionFactory() {
    try {
      return new ExpressionFactoryImpl((Properties) null, createTypeConverter());
    }
    catch (ELException e) {
      throw LOG.unableToInitializeFeelEngine(e);
    }
  }

  protected FeelTypeConverter createTypeConverter() {
    return new FeelTypeConverter();
  }

  protected ElContextFactory createElContextFactory() {
    return new FeelElContextFactory();
  }

}
