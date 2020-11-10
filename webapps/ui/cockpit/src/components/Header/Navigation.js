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

import { Dropdown } from "components";
import PluginPoint from "utils/PluginPoint";
import translate from "utils/translation";

import "./Navigation.scss";

function topLevelItems({ location, properties, children }) {
  const path = properties.path;
  return (
    <div className={path && location.pathname.match(path) ? "active" : null}>
      {children}
    </div>
  );
}
function additionalItems({ location, properties, children }) {
  const path = properties.path;
  return (
    <Dropdown.Option
      className={path && location.pathname.match(path) ? "active" : null}
    >
      {children}
    </Dropdown.Option>
  );
}

const TopLevelItems = withRouter(topLevelItems);
const AdditionalItems = withRouter(additionalItems);

function Navigation() {
  return (
    <nav className="Navigation">
      <PluginPoint
        location="cockpit.navigation"
        filter={({ priority }) => priority >= 0}
        wrapPlugins={TopLevelItems}
      />
      <Dropdown
        title={
          <>
            {translate("COCKPIT_MORE")}
            <span className="caret" />
          </>
        }
      >
        <PluginPoint
          location="cockpit.navigation"
          filter={({ priority }) => priority < 0}
          wrapPlugins={AdditionalItems}
        />
      </Dropdown>
    </nav>
  );
}

export default Navigation;
