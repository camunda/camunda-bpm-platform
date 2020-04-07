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
import { Dropdown } from "components";

import "./AppSelect.scss";
import pathUtil from "utils/paths";

export default function AppSelect({ user }) {
  const authorizedApps = user?.authorizedApps || ["admin", "tasklist"];

  if (
    !authorizedApps.includes("admin") &&
    !authorizedApps.includes("tasklist")
  ) {
    return null;
  }

  return (
    <div className="AppSelect">
      <Dropdown
        position="right"
        title={
          <>
            <span className="glyphicon glyphicon-home" />
            <span className="caret" />
          </>
        }
      >
        {authorizedApps.includes("admin") ? (
          <Dropdown.Option>
            <a href={`../../admin/${pathUtil("engine")}/`}>Admin</a>
          </Dropdown.Option>
        ) : null}
        {authorizedApps.includes("tasklist") ? (
          <Dropdown.Option>
            <a href={`../../tasklist/${pathUtil("engine")}/`}>Tasklist</a>
          </Dropdown.Option>
        ) : null}
      </Dropdown>
    </div>
  );
}
