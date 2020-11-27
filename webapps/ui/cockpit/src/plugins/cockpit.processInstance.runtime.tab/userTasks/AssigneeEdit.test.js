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
import { shallow } from "enzyme";
import { FormControl, Form, Button } from "react-bootstrap";
import { GlyphIcon } from "components";
import { post } from "utils/request";

import AssigneeEdit from "./AssigneeEdit";

jest.mock("utils/request", () => {
  return {
    post: jest.fn()
  };
});

it("should display assignee", () => {
  const assignee = "john";
  const node = shallow(<AssigneeEdit assignee={assignee} />);

  expect(node.find("span").text()).toContain(assignee);
});

it("Should open click", () => {
  const assignee = "";
  const node = shallow(<AssigneeEdit assignee={assignee} />);

  node.find(GlyphIcon).simulate("click");

  expect(node.find(FormControl)).toExist();
});

it("Allows editing", () => {
  const assignee = "john";
  const node = shallow(<AssigneeEdit assignee="" />);

  node.find(GlyphIcon).simulate("click");

  node.find(FormControl).simulate("change", { target: { value: assignee } });
  expect(node.find(FormControl)).toHaveValue(assignee);
});

it("Submits the correct assignee", done => {
  const assignee = "john";
  post.mockImplementation(async (url, query) => {
    expect(query.userId).toBe(assignee);
    done();
  });

  const node = shallow(<AssigneeEdit assignee="" />);

  node.find(GlyphIcon).simulate("click");
  node.find(FormControl).simulate("change", { target: { value: assignee } });
  node.find(Form).simulate("submit");
});
