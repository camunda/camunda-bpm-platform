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
import BpmnIO from "bpmn-js/lib/NavigatedViewer";

import { get } from "utils/request";
import withProcessInstance from "./withProcessInstance";

function transformBpmn20Xml(bpmn20Xml) {
  return new Promise(resolve => {
    BpmnIO.prototype.options = {};
    var moddle = BpmnIO.prototype._createModdle({
      moddleExtensions: {}
    });
    moddle.fromXML(bpmn20Xml, "bpmn:Definitions", function(
      err,
      definitions,
      context
    ) {
      resolve({
        definitions: definitions,
        bpmnElements: context.elementsById
      });
    });
  });
}

const BpmnContext = createContext();

// Provides `bpmnElements` and `definitions`
export const BpmnProvider = withProcessInstance(function({
  processInstance,
  children
}) {
  const [bpmn, setBpmn] = useState({});

  useEffect(() => {
    const fetchBpmn = async () => {
      const response = await (
        await get(
          `%ENGINE_API%/process-definition/${processInstance.definitionId}/xml`
        )
      ).json();

      setBpmn(await transformBpmn20Xml(response.bpmn20Xml));
    };
    if (processInstance) {
      fetchBpmn();
    }
  }, [processInstance]);
  return <BpmnContext.Provider value={bpmn}>{children}</BpmnContext.Provider>;
});

export default Component => props => (
  <Component {...useContext(BpmnContext)} {...props}></Component>
);
