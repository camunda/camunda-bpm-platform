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

import React, { useState, useEffect } from "react";
import { Dropdown } from "components";
import { get } from "utils/request";
import pathUtil from "utils/paths";
import translate from "utils/translation";
import Tooltip from "components/Tooltip";

import "./EngineSelect.scss";
import SmallScreenSwitch from "./SmallScreenSwitch";

export default function EngineSelect() {
  const [engines, setEngines] = useState(null);

  useEffect(() => {
    (async () => {
      const data = await (await get("%API%/engine")).json();

      setEngines(data);
    })();
  }, []);

  if (!engines || engines.length === 1) {
    return null;
  }

  return (
    <div className="EngineSelect">
      <SmallScreenSwitch>
        <Tooltip
          title={<span className="glyphicon glyphicon-info-sign" />}
          position="bottom"
        >
          {translate("DIRECTIVE_ENGINE_SELECT_TOOLTIP")}
        </Tooltip>
      </SmallScreenSwitch>
      <Dropdown
        position="right"
        title={
          <SmallScreenSwitch
            smallContent={<span className="glyphicon glyphicon-hdd" />}
          >
            <span>{pathUtil("engine")}</span>
          </SmallScreenSwitch>
        }
      >
        {engines.map(engine => {
          const name = engine.name;
          return (
            <Dropdown.Option key={name}>
              <a href={`../${name}/`}>{name}</a>
            </Dropdown.Option>
          );
        })}
      </Dropdown>
    </div>
  );
}
