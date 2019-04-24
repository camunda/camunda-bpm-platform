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
package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.camunda.bpm.engine.history.HistoricVariableInstanceQuery}s via native (SQL) queries
 * @author Ramona Koch
 */
public interface NativeHistoricVariableInstanceQuery extends NativeQuery<NativeHistoricVariableInstanceQuery, HistoricVariableInstance> {

    /**
     * Disable deserialization of variable values that are custom objects. By default, the query
     * will attempt to deserialize the value of these variables. By calling this method you can
     * prevent such attempts in environments where their classes are not available.
     * Independent of this setting, variable serialized values are accessible.
     */
    NativeHistoricVariableInstanceQuery disableCustomObjectDeserialization();

}
