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
import { Button } from "react-bootstrap";

import { Login } from "./Login";
import { Redirect } from "react-router-dom";
import { post } from "utils/request";
import { addError } from "utils/notifications";

jest.mock("utils/notifications", () => {
  return {
    addError: jest.fn(),
    clearAll: jest.fn()
  };
});

jest.mock("utils/request", () => {
  return {
    get: jest.fn(),
    post: jest.fn()
  };
});

it("renders without crashing", () => {
  shallow(<Login />);
});

it("should redirect when logged in", () => {
  const node = shallow(<Login user="foo" />);
  expect(node.find(Redirect)).toExist();
});

it("should have entered values in the input fields", () => {
  const node = shallow(<Login />);
  const input = "asdf";
  const field = "text";

  node
    .find(`[type="${field}"]`)
    .simulate("change", { target: { value: input } });

  expect(node.find(`[type="${field}"]`)).toHaveValue(input);
});

it("should call the login function when submitting the form", async () => {
  const node = shallow(<Login refreshUser={jest.fn()} />);

  const username = "david";
  const password = "dennis";

  node
    .find(`[type="text"]`)
    .simulate("change", { target: { value: username } });
  node
    .find(`[type="password"]`)
    .simulate("change", { target: { value: password } });

  await node.find("form").simulate("submit", { preventDefault: jest.fn() });

  expect(post).toHaveBeenCalledWith(
    expect.any(String),
    `username=${username}&password=${password}`,
    expect.any(Object)
  );
});

it("should display error message on failed login", async done => {
  const node = shallow(<Login refreshUser={jest.fn()} />);

  post.mockImplementationOnce(() => {
    const result = { status: 401 };
    throw result;
  });

  addError.mockImplementationOnce(message => {
    expect(message).toBeInstanceOf(Object);
    done();
  });
  await node.find("form").simulate("submit", { preventDefault: jest.fn() });
});

it("should disable the login button when waiting for server response", () => {
  const node = shallow(<Login refreshUser={jest.fn()} />);

  node.find("form").simulate("submit", { preventDefault: jest.fn() });

  expect(node.find(Button)).toBeDisabled();
});
