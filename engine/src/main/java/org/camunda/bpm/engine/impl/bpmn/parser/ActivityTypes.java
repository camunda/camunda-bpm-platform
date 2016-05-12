/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.bpmn.parser;

/**
 * @author Thorben Lindhauer
 */
// TODO: complete this and make public API as part of CAM-3385
public class ActivityTypes {

  public static final String MULTI_INSTANCE_BODY = "multiInstanceBody";

  public static final String BOUNDARY_TIMER = "boundaryTimer";
  public static final String BOUNDARY_MESSAGE = "boundaryMessage";
  public static final String BOUNDARY_SIGNAL = "boundarySignal";
  public static final String BOUNDARY_COMPENSATION = "compensationBoundaryCatch";

  public static final String START_EVENT = "startEvent";
  public static final String START_EVENT_TIMER = "startTimerEvent";
  public static final String START_EVENT_MESSAGE = "messageStartEvent";
  public static final String START_EVENT_SIGNAL = "signalStartEvent";
  public static final String START_EVENT_ESCALATION = "escalationStartEvent";
  public static final String START_EVENT_COMPENSATION = "compensationStartEvent";
  public static final String START_EVENT_ERROR = "errorStartEvent";

  public static final String INTERMEDIATE_EVENT_MESSAGE = "intermediateMessageCatch";
  public static final String INTERMEDIATE_EVENT_TIMER = "intermediateTimer";

}
