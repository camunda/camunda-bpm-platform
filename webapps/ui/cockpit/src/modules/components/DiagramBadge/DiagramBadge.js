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

import React, { useRef, useState } from "react";
import classNames from "classnames";

import { OverlayTrigger, Tooltip } from "react-bootstrap";

import "./DiagramBadge.scss";

export default function DiagramBadge({
  draggable = false,
  tooltip,
  onDrag = () => {},
  onDragEnd = () => {},
  onDragStart = () => {},
  className = "",
  children,
  ...props
}) {
  const [dragActive, setDragActive] = useState(false);

  const badgeRef = useRef();
  if (!children) {
    return null;
  }

  function handleDrag(event) {
    onDrag(event);
    setDragActive(true);
    badgeRef.current.setAttribute("style", "opacity: 0");
  }

  function handleDragEnd(event) {
    onDragEnd(event);
    setDragActive(false);
    badgeRef.current.setAttribute("style", "");
  }

  return (
    <OverlayTrigger
      placement="top"
      overlay={
        <Tooltip className={dragActive ? "hide" : ""} id="badge-tooltip">
          {tooltip}
        </Tooltip>
      }
    >
      <span
        onDrag={handleDrag}
        onDragEnd={handleDragEnd}
        onDragStart={onDragStart}
        draggable={draggable}
        className={classNames("DiagramBadge badge", className)}
        ref={badgeRef}
        {...props}
      >
        {children}
      </span>
    </OverlayTrigger>
  );
}
