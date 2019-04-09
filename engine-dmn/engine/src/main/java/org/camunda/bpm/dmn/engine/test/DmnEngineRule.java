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
package org.camunda.bpm.dmn.engine.test;

import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * JUnit rule for {@link DmnEngine} initialization.
 * <p>
 * Usage:
 * </p>
 *
 * <pre>
 * public class YourDmnTest {
 *
 *   &#64;Rule
 *   public DmnEngineRule dmnEngineRule = new DmnEngineRule();
 *
 *   ...
 * }
 * </pre>
 *
 * <p>
 * The DMN engine will be made available to the test class
 * through the getters of the {@code dmnEngineRule} (see {@link #getDmnEngine()}).
 * The DMN engine will be initialized with the default DMN engine configuration.
 * To specify a different configuration, pass the configuration to the
 * {@link #DmnEngineRule(DmnEngineConfiguration)} constructor.
 * </p>
 */
public class DmnEngineRule extends TestWatcher {

  protected DmnEngine dmnEngine;
  protected DmnEngineConfiguration dmnEngineConfiguration;

  /**
   * Creates a {@link DmnEngine} with the default {@link DmnEngineConfiguration}
   */
  public DmnEngineRule() {
    this(null);
  }

  /**
   * Creates a {@link DmnEngine} with the given {@link DmnEngineConfiguration}
   */
  public DmnEngineRule(DmnEngineConfiguration dmnEngineConfiguration) {
    if (dmnEngineConfiguration != null) {
      this.dmnEngineConfiguration = dmnEngineConfiguration;
    }
    else {
      this.dmnEngineConfiguration = DmnEngineConfiguration.createDefaultDmnEngineConfiguration();
    }
  }

  /**
   * @return the {@link DmnEngine}
   */
  public DmnEngine getDmnEngine() {
    return dmnEngine;
  }

  @Override
  protected void starting(Description description) {
    if (dmnEngine == null) {
      dmnEngine = dmnEngineConfiguration.buildEngine();
    }
  }

}
