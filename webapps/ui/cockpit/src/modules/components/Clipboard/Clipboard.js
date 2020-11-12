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

import React, { useEffect, useRef, useState } from "react";
import { Button, Glyphicon, OverlayTrigger, Tooltip } from "react-bootstrap";
import classNames from "classnames";
import translate from "utils/translation";

import "./Clipboard.scss";

export default function({ children, text }) {
  const [icon, setIcon] = useState("copy");
  const [needsResize, setNeedsResize] = useState(false);

  const parentRef = useRef();
  const contentRef = useRef();

  useEffect(() => {
    // This makes sure we only truncate the value when necessary
    const calculateSize = () => {
      const content = contentRef.current;
      const parent = parentRef.current;
      const icon = parent.querySelector(".btn"); // Can't use ref as it is a react-bootstrap element
      const elementStyle = window.getComputedStyle(parent);

      let contentWidth = 1;
      let containerWidth = 0;
      if (content && icon) {
        contentWidth = content.offsetWidth + icon.offsetWidth;
        containerWidth =
          parseInt(elementStyle.width) -
          parseInt(elementStyle.paddingRight) -
          parseInt(elementStyle.paddingLeft);
      }
      if (contentWidth - containerWidth > 0) {
        setNeedsResize(true);
      } else {
        setNeedsResize(false);
      }
    };
    calculateSize();

    window.addEventListener("resize", calculateSize);
    return () => window.removeEventListener("resize", calculateSize);
  }, []);

  async function copyToClipboard() {
    try {
      await navigator.clipboard.writeText(text);
      setIcon("ok");
    } catch (e) {
      setIcon("warning-sign");
    }

    await new Promise(resolve => setTimeout(resolve, 1200));
    setIcon("copy");
  }

  return (
    <span className="Clipboard" ref={parentRef}>
      <span
        className={classNames("content", needsResize ? "resize" : "")}
        ref={contentRef}
      >
        {children}
      </span>
      <Button className="copyButton" bsStyle="link" onClick={copyToClipboard}>
        <OverlayTrigger
          placement="top"
          overlay={
            <Tooltip id="tooltip">
              {translate("CAM_WIDGET_COPY", { value: text })}
            </Tooltip>
          }
        >
          <Glyphicon glyph={icon}></Glyphicon>
        </OverlayTrigger>
      </Button>
    </span>
  );
}
