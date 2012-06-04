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
package com.camunda.fox.platform.subsystem.impl;

import java.io.IOException;

import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;
import org.junit.Ignore;

import com.camunda.fox.platform.subsystem.impl.extension.FoxPlatformExtension;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
@Ignore
public class JBossSubsystemTest extends AbstractSubsystemBaseTest {

  public JBossSubsystemTest() {
    super(FoxPlatformExtension.SUBSYSTEM_NAME, new FoxPlatformExtension());
  }

  @Override
  protected String getSubsystemXml() throws IOException {
    try {
//      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_PROCESS_ENGINES_ELEMENT_ONLY);
      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_ENGINES);
//      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_ENGINES_AND_PROPERTIES);
//      return FileUtils.readFile(JBossSubsystemXMLTest.SUBSYSTEM_WITH_DUPLICATE_ENGINE_NAMES);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
