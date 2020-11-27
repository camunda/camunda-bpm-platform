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

import React from "react";
import { act } from "react-dom/test-utils";
import { shallow, mount } from "enzyme";
import { UserTasks } from "./UserTasks";
import { post } from "utils/request";
import { Table } from "components";

const mockUserTask = {
  id: "anId",
  name: "aName",
  assignee: "anAssignee",
  created: "2013-01-23T13:42:42.000+0200",
  due: "2013-01-23T13:49:42.576+0200",
  followUp: "2013-01-23T13:44:42.437+0200",
  delegationState: "RESOLVED",
  description: "aDescription",
  executionId: "anExecution",
  owner: "anOwner",
  parentTaskId: "aParentId",
  priority: 42,
  processDefinitionId: "aProcDefId",
  processInstanceId: "aProcInstId",
  caseDefinitionId: "aCaseDefId",
  caseInstanceId: "aCaseInstId",
  caseExecutionId: "aCaseExecution",
  taskDefinitionKey: "aTaskDefinitionKey",
  suspended: false,
  formKey: "aFormKey",
  tenantId: "aTenantId"
};

jest.mock("utils/request", () => {
  return {
    post: jest.fn()
  };
});

const defaultProps = { processInstanceId: "foo", filter: {}, bpmnElements: {} };

it("renders without crashing", () => {
  shallow(<UserTasks {...defaultProps} />);
});

it("loads User Tasks", async () => {
  post.mockImplementation(async (url, query) => {
    if (url.includes("count")) {
      return {
        json: () => {
          return { count: 10 };
        }
      };
    } else {
      return {
        json: () => {
          return [mockUserTask, mockUserTask, mockUserTask];
        }
      };
    }
  });

  const node = mount(<UserTasks {...defaultProps} />);
  await act(async () => {
    await new Promise(resolve => setImmediate(resolve));
    await node.update();
  });

  expect(node).toMatchSnapshot();
});
