/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.model.bpmn;

import org.camunda.bpm.model.bpmn.instance.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sebastian Menski
 */
public class CollaborationParserTest {

  private static BpmnModelInstance modelInstance;
  private static Collaboration collaboration;

  @BeforeClass
  public static void parseModel() {
    modelInstance = Bpmn.readModelFromStream(CollaborationParserTest.class.getResourceAsStream("CollaborationParserTest.bpmn"));
    collaboration = modelInstance.getModelElementById("collaboration1");
  }

  @Test
  public void testConversations() {
    assertThat(collaboration.getConversationNodes()).hasSize(1);

    ConversationNode conversationNode = collaboration.getConversationNodes().iterator().next();
    assertThat(conversationNode).isInstanceOf(Conversation.class);
    assertThat(conversationNode.getParticipants()).isEmpty();
    assertThat(conversationNode.getCorrelationKeys()).isEmpty();
    assertThat(conversationNode.getMessageFlows()).isEmpty();
  }

  @Test
  public void testConversationLink() {
    Collection<ConversationLink> conversationLinks = collaboration.getConversationLinks();
    for (ConversationLink conversationLink : conversationLinks) {
      assertThat(conversationLink.getId()).startsWith("conversationLink");
      assertThat(conversationLink.getSource()).isInstanceOf(Participant.class);
      Participant source = (Participant) conversationLink.getSource();
      assertThat(source.getName()).isEqualTo("Pool");
      assertThat(source.getId()).startsWith("participant");

      assertThat(conversationLink.getTarget()).isInstanceOf(Conversation.class);
      Conversation target = (Conversation) conversationLink.getTarget();
      assertThat(target.getId()).isEqualTo("conversation1");
    }
  }

  @Test
  public void testMessageFlow() {
    Collection<MessageFlow> messageFlows = collaboration.getMessageFlows();
    for (MessageFlow messageFlow : messageFlows) {
      assertThat(messageFlow.getId()).startsWith("messageFlow");
      assertThat(messageFlow.getSource()).isInstanceOf(ServiceTask.class);
      assertThat(messageFlow.getTarget()).isInstanceOf(Event.class);
    }
  }

  @Test
  public void testParticipant() {
    Collection<Participant> participants = collaboration.getParticipants();
    for (Participant participant : participants) {
      assertThat(participant.getProcess().getId()).startsWith("process");
    }
  }

  @Test
  public void testUnused() {
    assertThat(collaboration.getCorrelationKeys()).isEmpty();
    assertThat(collaboration.getArtifacts()).isEmpty();
    assertThat(collaboration.getConversationAssociations()).isEmpty();
    assertThat(collaboration.getMessageFlowAssociations()).isEmpty();
    assertThat(collaboration.getParticipantAssociations()).isEmpty();
  }


  @AfterClass
  public static void validateModel() {
    Bpmn.validateModel(modelInstance);
  }

}
