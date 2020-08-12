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
import { withRouter } from "react-router-dom";

import { post } from "utils/request";
import translate from "utils/translation";
import pathUtil from "utils/paths";

import { Dropdown } from "components";

import "./UserInformation.scss";
import SmallScreenSwitch from "./SmallScreenSwitch";

function UserInformation({ user, history }) {
  const profile = user.profile;

  function logout() {
    post("%ADMIN_API%/auth/user/%ENGINE%/logout").then(() => {
      history.push("/login");
    });
  }

  return (
    <div className="UserInformation">
      <Dropdown
        title={
          <button className="user">
            <span className="glyphicon glyphicon-user" />
            <SmallScreenSwitch>
              <span className="userName">
                {profile
                  ? `${profile.firstName} ${profile.lastName}`
                  : user.userId}
              </span>
            </SmallScreenSwitch>
          </button>
        }
        position="right"
      >
        <Dropdown.Option>
          <a href={`../../welcome/${pathUtil("engine")}`}>
            {translate("MY_PROFILE")}
          </a>
        </Dropdown.Option>
        <Dropdown.Divider />
        <Dropdown.Option onClick={logout}>
          {translate("SIGN_OUT_ACTION")}
        </Dropdown.Option>
      </Dropdown>
    </div>
  );
}

export default withRouter(UserInformation);
