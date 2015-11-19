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

package org.camunda.bpm.dmn.feel.impl.juel;

import java.util.Properties;
import javax.el.ELException;
import javax.el.ExpressionFactory;

import org.camunda.bpm.dmn.feel.impl.FeelEngine;
import org.camunda.bpm.dmn.feel.impl.FeelEngineFactory;
import org.camunda.bpm.dmn.feel.impl.juel.el.ElContextFactory;
import org.camunda.bpm.dmn.feel.impl.juel.el.FeelElContextFactory;
import org.camunda.bpm.dmn.feel.impl.juel.el.FeelTypeConverter;
import org.camunda.bpm.dmn.feel.impl.juel.transform.FeelToJuelTransform;
import org.camunda.bpm.dmn.feel.impl.juel.transform.FeelToJuelTransformImpl;

import de.odysseus.el.ExpressionFactoryImpl;

public class FeelEngineFactoryImpl implements FeelEngineFactory {

  public static final FeelEngineLogger LOG = FeelLogger.ENGINE_LOGGER;

  protected FeelEngine feelEngine;

  public FeelEngineFactoryImpl() {
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
