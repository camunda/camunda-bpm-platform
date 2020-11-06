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
import { Tooltip, OverlayTrigger, Button } from "react-bootstrap";
import translate from "utils/translation";

export default function ActionButton({ icon, labels, disabled, onClick }) {
  return (
    <OverlayTrigger
      placement="left"
      overlay={
        <Tooltip id="tooltip">
          {translate(disabled ? labels.disabled : labels.enabled)}
        </Tooltip>
      }
    >
      <div>
        <Button
          bsStyle="default"
          className="btn-toolbar"
          onClick={disabled ? () => {} : onClick}
          disabled={disabled}
        >
          <span className={"glyphicon glyphicon-" + icon} />
        </Button>
      </div>
    </OverlayTrigger>
  );
}
