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
package org.camunda.bpm.dmn.engine.impl;

import org.camunda.bpm.dmn.engine.impl.hitpolicy.DmnHitPolicyLogger;
import org.camunda.bpm.dmn.engine.impl.transform.DmnTransformLogger;
import org.camunda.commons.logging.BaseLogger;

public class DmnLogger extends BaseLogger {

 public static final String PROJECT_CODE = "DMN";
 public static final String PROJECT_LOGGER= "org.camunda.bpm.dmn";

 public static DmnEngineLogger ENGINE_LOGGER = createLogger(DmnEngineLogger.class, PROJECT_CODE, PROJECT_LOGGER, "01");
 public static DmnTransformLogger TRANSFORM_LOGGER = createLogger(DmnTransformLogger.class, PROJECT_CODE, PROJECT_LOGGER + ".transform", "02");
 public static DmnHitPolicyLogger HIT_POLICY_LOGGER = createLogger(DmnHitPolicyLogger.class, PROJECT_CODE, PROJECT_LOGGER + ".hitPolicy", "03");

}
