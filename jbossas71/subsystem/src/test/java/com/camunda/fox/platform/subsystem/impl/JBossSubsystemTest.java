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

import com.camunda.fox.platform.subsystem.impl.extension.FoxPlatformExtension;
import java.util.List;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class JBossSubsystemTest extends AbstractSubsystemTest {

  public JBossSubsystemTest() {
    super(FoxPlatformExtension.SUBSYSTEM_NAME, new FoxPlatformExtension());
  }

  @Test
  public void testParseSubsystemXml() throws Exception {
    String subsystemXml = FileUtils.readFile("subsystem.xml");
    System.out.println(subsystemXml);
    
    List<ModelNode> operations = parse(subsystemXml);
    System.out.println(operations);
  }
  
  @Test
  @Ignore(value="Won't work")
  public void testSubsystemBoot() throws Exception {
    
    String subsystemXml = FileUtils.readFile("subsystem.xml");
    
    KernelServices services = installInController(subsystemXml);
    
    ModelNode model = services.readWholeModel();
  }
}
