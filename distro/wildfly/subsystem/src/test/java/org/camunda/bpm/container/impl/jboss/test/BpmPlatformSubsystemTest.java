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
package org.camunda.bpm.container.impl.jboss.test;

import java.io.IOException;
import org.camunda.bpm.container.impl.jboss.extension.BpmPlatformExtension;
import org.camunda.bpm.container.impl.jboss.extension.ModelConstants;
import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;


/**
 * @author Christian Lipphardt
 */
public class BpmPlatformSubsystemTest extends AbstractSubsystemBaseTest {

  public BpmPlatformSubsystemTest() {
    super(ModelConstants.SUBSYSTEM_NAME, new BpmPlatformExtension());
  }

  @Override
  protected String getSubsystemXml() throws IOException {
    try {
      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_ALL_OPTIONS);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Method is only used by WF-10 to validate the subsystem schema. Isn't executed when running WF8.
   */
  @Override
  protected String getSubsystemXsdPath() throws Exception {
    return "schema/camundaPlatformSubsystem.xsd";
  }
}
