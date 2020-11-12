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

import React, { useEffect, useState, createContext, useContext } from "react";

import { get } from "utils/request";

const ProcessInstanceContext = createContext();

export function ProcessInstanceProvider({ processInstanceId, children }) {
  const [processInstance, setProcessInstance] = useState(null);

  useEffect(() => {
    const loadProcessInstance = async () => {
      return await (await get(
        `%ENGINE_API%/process-instance/${processInstanceId}`
      )).json();
    };

    loadProcessInstance().then(processInstance => {
      setProcessInstance(processInstance);
    });
  }, [processInstanceId]);

  return (
    <ProcessInstanceContext.Provider value={processInstance}>
      {children}
    </ProcessInstanceContext.Provider>
  );
}

export default Component => props => (
  <Component {...useContext(ProcessInstanceContext)} {...props} />
);
