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
public class MergeTest extends AbstractRoundtripTest {

  @Test
  public void shouldMergeExtensionElements() throws Exception {

    String mergedDiagram = importExecutableModelFileBased(
        "org/camunda/bpm/cycle/roundtrip/repository/signavio-extension-elements-pool-extracted.bpmn",
        "org/camunda/bpm/cycle/roundtrip/repository/signavio-extension-elements.bpmn");

    Assert.assertTrue(mergedDiagram.contains("<signavio:signavioType dataObjectType=\"ProcessParticipant\"/>"));

    Assert.assertTrue(mergedDiagram.contains("Non Executable Pool"));
    Assert.assertTrue(mergedDiagram.contains("Executable Pool"));
  }

  @Test
  public void shouldMergeExtensionElementAttributes() throws Exception {
    String mergedDiagram = importExecutableModelFileBased(
        "org/camunda/bpm/cycle/roundtrip/repository/signavio-extension-elements-pool-extracted.bpmn",
        "org/camunda/bpm/cycle/roundtrip/repository/signavio-extension-elements.bpmn");

    String normalizedMergedDiagram = normalizeXml(mergedDiagram);

    Assert.assertTrue(normalizedMergedDiagram.contains("<signavio:signavioLabel align=\"center\" bottom=\"false\" left=\"false\" ref=\"text_name\" right=\"false\" top=\"true\" valign=\"bottom\" x=\"20.0\" y=\"-8.0\"/>"));
  }
}
