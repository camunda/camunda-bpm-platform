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
import { Link } from "react-router-dom";

import withUser from "HOC/withUser";

import EngineSelect from "./EngineSelect";
import AppSelect from "./AppSelect";
import UserInformation from "./UserInformation";

import { ReactComponent as Logo } from "./logo-2020-round.svg";
import "./Header.scss";
import SmallScreenSwitch from "./SmallScreenSwitch";
import Tooltip from "components/Tooltip";
import translate from "utils/translation";
import Navigation from "./Navigation";

function Header({ user }) {
  return (
    <div className="Header">
      {user ? (
        <Link to="/" className="app-banner">
          <Logo className="logo" />
          {translate("APP_VENDOR")} Cockpit
        </Link>
      ) : null}

      {user && <Navigation />}

      <span className="divider" />
      <SmallScreenSwitch
        smallContent={
          <Tooltip
            position="bottom"
            title={<span className="glyphicon glyphicon-exclamation-sign" />}
          >
            {translate("CAM_WIDGET_HEADER_SMALL_SCREEN_WARNING")}
          </Tooltip>
        }
      />
      <EngineSelect />
      {user && <UserInformation user={user} />}
      <AppSelect user={user} />
    </div>
  );
}

export default withUser(Header);
