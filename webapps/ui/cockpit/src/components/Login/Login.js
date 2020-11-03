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

import React, { useState } from "react";
import { Button, FormControl } from "react-bootstrap";
import { Redirect } from "react-router";
import { CamundaLogo } from "components";
import FirstLoginMessage from "./FirstLoginMessage";

import { withPreviousLocation, withUser } from "HOC";

import { post, get } from "utils/request";
import { addError, clearAll } from "utils/notifications";
import translate from "utils/translation";

import "./Login.scss";

function Login({ user, refreshUser, previousLocation }) {
  const [loadingState, setLoadingState] = useState("INITIAL");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  async function doLogin() {
    await post(
      "%ADMIN_API%/auth/user/%ENGINE%/login/cockpit",
      `username=${username}&password=${password}`,
      {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
        }
      }
    );
    await get("%API%/engine"); // Make sure we have an up to date CSRF-Token after login
    setLoadingState("DONE");
    clearAll();
    refreshUser(); // Do last so we don't do state updates on unmounted components
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoadingState("LOADING");
    await get("%API%/engine"); // Make sure we have an up to date CSRF-Token
    try {
      await doLogin();
    } catch (e) {
      setLoadingState("ERROR");
      setUsername("");
      setPassword("");

      if (e.status === 401 || e.status === 403) {
        addError({
          status: translate("PAGE_LOGIN_FAILED"),
          message: translate("PAGE_LOGIN_ERROR_MSG"),
          exclusive: true
        });
      } else {
        addError({
          status: translate("PAGE_LOGIN_FAILED"),
          message: (await e.json()).message,
          exclusive: true
        });
      }
    }
  }

  if (user || loadingState === "DONE") {
    return <Redirect to={previousLocation || "/"} />;
  }

  return (
    <div className="Login">
      <div className="login-header">
        <CamundaLogo />
        <div className="app-name">Cockpit</div>
      </div>

      <form onSubmit={handleSubmit}>
        <FormControl
          placeholder={translate("PAGE_LOGIN_USERNAME")}
          type="text"
          value={username}
          onChange={e => setUsername(e.target.value)}
          required
        />
        <FormControl
          placeholder={translate("PAGE_LOGIN_PASSWORD")}
          type="password"
          value={password}
          onChange={e => setPassword(e.target.value)}
          required
        />
        <Button
          type="submit"
          bsStyle="primary"
          bsSize="large"
          disabled={loadingState === "LOADING"}
        >
          {translate("PAGE_LOGIN_SIGN_IN_ACTION")}
        </Button>
      </form>
      <FirstLoginMessage />
    </div>
  );
}

export default withPreviousLocation(withUser(Login));
