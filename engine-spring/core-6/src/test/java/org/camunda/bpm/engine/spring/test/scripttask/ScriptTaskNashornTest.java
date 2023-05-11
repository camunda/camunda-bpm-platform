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
public class ScriptTaskNashornTest extends AbstractScriptTaskTest {

  private static final String NASHORN = "nashorn";

  @Test
  public void shouldFindPublicSpringBeanMethodWithJavascriptNashorn() {
    testSpringBeanVisibility(NASHORN, "execution.setVariable('foo', testbean.getName());");
  }

  @Test
  public void shouldFindPrivateSpringBeanAttributeWithJavascriptNashorn() {
    testSpringBeanVisibility(NASHORN, "execution.setVariable('foo', testbean.name);");
  }

}
