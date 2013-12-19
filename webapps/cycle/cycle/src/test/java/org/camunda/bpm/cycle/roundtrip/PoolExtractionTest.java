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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.camunda.bpm.cycle.util.IoUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nico.rehwaldt
 */
public class PoolExtractionTest {

  private BpmnProcessModelUtil roundtripUtil = new BpmnProcessModelUtil();

  @Test
  public void shouldKeepExtensionElements() throws Exception {
    String executablePool = extractExecutablePool("org/camunda/bpm/cycle/roundtrip/repository/signavio-extension-elements.bpmn");
    Assert.assertTrue(executablePool.contains("<signavio:signavioType dataObjectType=\"ProcessParticipant\"/>"));
  }

  public String extractExecutablePool(String diagramFile) throws IOException {
    FileInputStream is = new FileInputStream(IoUtil.getFile(diagramFile));
    InputStream resultStream = roundtripUtil.extractExecutablePool(is);

    return new String(IoUtil.readInputStream(resultStream, "extracted executable pool"), Charset.forName("UTF-8"));
  }
}
