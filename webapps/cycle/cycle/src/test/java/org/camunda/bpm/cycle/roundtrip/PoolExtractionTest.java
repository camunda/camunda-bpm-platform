/*
 * Copyright 2013 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.cycle.roundtrip;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Nico Rehwaldt
 */
public class PoolExtractionTest extends AbstractRoundtripTest {

  private BpmnProcessModelUtil roundtripUtil = new BpmnProcessModelUtil();

  @Test
  public void shouldKeepExtensionElements() throws Exception {
    String executablePool = extractExecutableModelFromFile("/org/camunda/bpm/cycle/roundtrip/repository/signavio-extension-elements.bpmn");
    Assert.assertTrue(executablePool.contains("<signavio:signavioType dataObjectType=\"ProcessParticipant\"/>"));

    Assert.assertFalse(executablePool.contains("Non Executable Pool"));
  }

  @Test
  public void shouldKeepExtensionElementAttributes() throws Exception {
    String executablePool = extractExecutableModelFromFile("/org/camunda/bpm/cycle/roundtrip/repository/signavio-extension-elements.bpmn");

    String normalizedExecutablePool = normalizeXml(executablePool);

    Assert.assertTrue(normalizedExecutablePool.contains("<signavio:signavioLabel align=\"center\" bottom=\"false\" left=\"false\" ref=\"text_name\" right=\"false\" top=\"true\" valign=\"bottom\" x=\"20.0\" y=\"-8.0\"/>"));
  }
}
