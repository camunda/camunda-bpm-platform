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
package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.impl.bpmn.behavior.BpmnBehaviorLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseLogger;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnBehaviorLogger;
import org.camunda.bpm.engine.impl.cmmn.operation.CmmnOperationLogger;
import org.camunda.bpm.engine.impl.cmmn.transformer.CmmnTransformerLogger;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.commons.logging.BaseLogger;

/**
 * @author Stefan Hentschel.
 */
public class ProcessEngineLogger extends BaseLogger {

  public static final String PROJECT_CODE = "ENGINE";

  public static final BpmnParseLogger PARSE_LOGGER = BaseLogger.createLogger(BpmnParseLogger.class, PROJECT_CODE, "org.camunda.bpm.engine.bpmn.parser", "01");
  public static final BpmnBehaviorLogger BPMN_BEHAVIOR_LOGGER = BaseLogger.createLogger(BpmnBehaviorLogger.class, PROJECT_CODE, "org.camunda.bpm.engine.bpmn.behavior", "02");
  public static final EnginePersistenceLogger PERSISTENCE_LOGGER = BaseLogger.createLogger(EnginePersistenceLogger.class, PROJECT_CODE, "org.camunda.bpm.engine.persistence", "03");

  public static final CmmnTransformerLogger CMMN_TRANSFORMER_LOGGER = BaseLogger.createLogger(CmmnTransformerLogger.class, PROJECT_CODE, "org.camunda.bpm.engine.cmmn.transformer", "04");
  public static final CmmnBehaviorLogger CMNN_BEHAVIOR_LOGGER = BaseLogger.createLogger(CmmnBehaviorLogger.class, PROJECT_CODE, "org.camunda.bpm.engine.cmmn.behavior", "05");
  public static final CmmnOperationLogger CMMN_OPERATION_LOGGER = BaseLogger.createLogger(CmmnOperationLogger.class, PROJECT_CODE, "org.camunda.bpm.engine.cmmn.operation", "06");
}
