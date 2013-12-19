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


import org.camunda.bpm.cycle.util.IoUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nico.rehwaldt
 */
public class MergeTest {

  private BpmnProcessModelUtil roundtripUtil = new BpmnProcessModelUtil();

  @Test
  public void shouldMergeExtensionElements() throws Exception {

    String mergedDiagram = mergeExecutablePool(
        "org/camunda/bpm/cycle/roundtrip/repository/signavio-extension-elements-pool-extracted.bpmn",
        "org/camunda/bpm/cycle/roundtrip/repository/signavio-extension-elements.bpmn");

    Assert.assertTrue(mergedDiagram.contains("<signavio:signavioType dataObjectType=\"ProcessParticipant\"/>"));
  }

  private String mergeExecutablePool(String sourceDiagram, String targetDiagram) {

    String sourceXml = IoUtil.readFileAsString(sourceDiagram);
    String targetXml = IoUtil.readFileAsString(targetDiagram);

    return roundtripUtil.importChangesFromExecutableBpmnModel(sourceXml, targetXml);
  }
}
