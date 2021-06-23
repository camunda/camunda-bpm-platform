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
package org.camunda.bpm.engine.spring.test.scripttask;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:org/camunda/bpm/engine/spring/test/scripttask/ScriptTaskTest-applicationContext.xml" })
public class ScriptTaskTest extends AbstractScriptTaskTest {

  private static final String JAVASCRIPT = "javascript";
  private static final String PYTHON = "python";
  private static final String GROOVY = "groovy";
  private static final String JUEL = "juel";

  @Test
  public void shouldFindPublicSpringBeanMethodWithJavascriptGraalJs() {
    testSpringBeanVisibility(JAVASCRIPT, "execution.setVariable('foo', testbean.getName());");
  }

  @Test
  public void shouldFindPublicSpringBeanMethodWithGroovy() {
    testSpringBeanVisibility(GROOVY, "execution.setVariable('foo', testbean.getName())\n");
  }

  @Test
  public void shouldFindPublicSpringBeanMethodWithPython() {
    testSpringBeanVisibility(PYTHON, "execution.setVariable('foo', testbean.getName())\n");
  }

  @Test
  public void shouldFindPublicSpringBeanMethodWithJuel() {
    testSpringBeanVisibility(JUEL, "${execution.setVariable('foo', testbean.getName())}");
  }

  @Test
  public void shouldNotFindPrivateSpringBeanAttributeWithJavascriptGraalJs() {
    testSpringBeanVisibility(JAVASCRIPT, "execution.setVariable('foo', testbean.name);", false);
  }

  @Test
  public void shouldFindPrivateSpringBeanAttributeWithWithGroovy() {
    testSpringBeanVisibility(GROOVY, "execution.setVariable('foo', testbean.name)\n");
  }

  @Test
  public void shouldFindPrivateSpringBeanAttributeWithPython() {
    testSpringBeanVisibility(PYTHON, "execution.setVariable('foo', testbean.name)\n");
  }

  @Test
  public void shouldFindPrivateSpringBeanAttributeWithJuel() {
    testSpringBeanVisibility(JUEL, "${execution.setVariable('foo', testbean.name)}");
  }
}
