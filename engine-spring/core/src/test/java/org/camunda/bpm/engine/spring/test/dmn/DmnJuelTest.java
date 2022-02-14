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
package org.camunda.bpm.engine.spring.test.dmn;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.RepositoryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:org/camunda/bpm/engine/spring/test/dmn/DmnJuelTest-applicationContext.xml" })
public class DmnJuelTest {

  @Autowired
  protected RepositoryService repositoryService;

  @Autowired
  protected DecisionService decisionService;

  protected String deploymentId;

  @Before
  public void deploy() {
    deploymentId = repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/spring/test/dmn/JuelTest.dmn")
        .deploy()
        .getId();
  }

  @After
  public void clean() {
    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Test
  public void shouldResolveBean() {
    // given

    // when
    DmnDecisionResult result = decisionService.evaluateDecisionByKey("drg-with-bean-expression")
        .evaluate();

    // then
    assertThat((String)result.getSingleEntry()).isEqualTo("bar");
  }

}
