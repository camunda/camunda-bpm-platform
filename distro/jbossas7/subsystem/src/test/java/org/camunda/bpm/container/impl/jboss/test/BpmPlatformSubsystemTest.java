/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jboss.test;

import org.camunda.bpm.container.impl.jboss.extension.BpmPlatformExtension;
import org.camunda.bpm.container.impl.jboss.extension.ModelConstants;
import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;

import java.io.IOException;


/**
 *
 * @author christian.lipphardt@camunda.com
 */
public class BpmPlatformSubsystemTest extends AbstractSubsystemBaseTest {

  public BpmPlatformSubsystemTest() {
    super(ModelConstants.SUBSYSTEM_NAME, new BpmPlatformExtension());
  }

  @Override
  protected String getSubsystemXml() throws IOException {
    try {
//      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY);
//      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_ENGINES);
      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_ENGINES_PROPERTIES_PLUGINS);
//      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_DUPLICATE_ENGINE_NAMES);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

}
