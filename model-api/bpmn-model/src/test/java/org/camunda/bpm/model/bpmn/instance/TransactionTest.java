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
package org.camunda.bpm.model.bpmn.instance;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.TransactionMethod;
import org.camunda.bpm.model.xml.impl.util.ReflectUtil;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Thorben Lindhauer
 */
public class TransactionTest extends BpmnModelElementInstanceTest {

  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(SubProcess.class, false);
  }

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return Collections.emptyList();
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("method", false, false, TransactionMethod.Compensate)
    );
  }

  @Test
  public void shouldReadTransaction() {
    InputStream inputStream = ReflectUtil.getResourceAsStream("org/camunda/bpm/model/bpmn/TransactionTest.xml");
    Transaction transaction = Bpmn.readModelFromStream(inputStream).getModelElementById("transaction");

    assertThat(transaction).isNotNull();
    assertThat(transaction.getMethod()).isEqualTo(TransactionMethod.Image);
    assertThat(transaction.getFlowElements()).hasSize(1);
  }

  @Test
  public void shouldWriteTransaction() throws ParserConfigurationException, SAXException, IOException {
    // given a model
    BpmnModelInstance newModel = Bpmn.createProcess("process").done();

    Process process = newModel.getModelElementById("process");

    Transaction transaction = newModel.newInstance(Transaction.class);
    transaction.setId("transaction");
    transaction.setMethod(TransactionMethod.Store);
    process.addChildElement(transaction);

    // that is written to a stream
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    Bpmn.writeModelToStream(outStream, newModel);

    // when reading from that stream
    ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    Document actualDocument = docBuilder.parse(inStream);

    // then it possible to traverse to the transaction element and assert its attributes
    NodeList transactionElements = actualDocument.getElementsByTagName("transaction");
    assertThat(transactionElements.getLength()).isEqualTo(1);

    Node transactionElement = transactionElements.item(0);
    assertThat(transactionElement).isNotNull();
    Node methodAttribute = transactionElement.getAttributes().getNamedItem("method");
    assertThat(methodAttribute.getNodeValue()).isEqualTo("##Store");

  }
}
