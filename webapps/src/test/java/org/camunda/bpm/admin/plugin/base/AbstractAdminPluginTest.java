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
package org.camunda.bpm.admin.plugin.base;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.admin.Admin;
import org.camunda.bpm.admin.impl.DefaultAdminRuntimeDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.util.LogUtil;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

public abstract class AbstractAdminPluginTest {

  protected static final TestAdminRuntimeDelegate RUNTIME_DELEGATE = new TestAdminRuntimeDelegate();

  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
    LogFactory.useJdkLogging();
  }

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule(true);

  @BeforeClass
  public static void beforeClass() {
    Admin.setAdminRuntimeDelegate(RUNTIME_DELEGATE);
  }

  @AfterClass
  public static void afterClass() {
    Admin.setAdminRuntimeDelegate(null);
  }

  @Before
  public void before() {
    RUNTIME_DELEGATE.ENGINE = getProcessEngine();
  }

  @After
  public void after() {
    RUNTIME_DELEGATE.ENGINE = null;
    getProcessEngine().getIdentityService().clearAuthentication();
  }

  public ProcessEngine getProcessEngine() {
    return processEngineRule.getProcessEngine();
  }

  private static class TestAdminRuntimeDelegate extends DefaultAdminRuntimeDelegate {

    public ProcessEngine ENGINE;

    @Override
    public ProcessEngine getProcessEngine(String processEngineName) {

      // always return default engine for plugin tests
      return ENGINE;
    }
  }
}
