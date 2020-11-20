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

import React, { useEffect, useRef } from "react";
import { Button, OverlayTrigger, Tooltip } from "react-bootstrap";
import { GlyphIcon, LinkButton } from "components";

import "./InlineEdit.scss";

function InlineEdit({
  onSet = () => {},
  onEditStart = () => {},
  onEditEnd = () => {},
  edit = false,
  value,
  tooltip
}) {
  const setElRef = useRef(null);
  const textELRef = useRef(null);

  useEffect(() => {
    if (edit) {
      setTimeout(() => {
        if (textELRef.current) {
          textELRef.current.value = value || "";
          textELRef.current.focus();
          textELRef.current.select();
        }
      }, 50);
    }
  }, [edit, value]);

  useEffect(() => {
    const handleKeydown = event => {
      if (edit) {
        if (event.key === "Enter" || event.key === "Tab") {
          onSet(event, textELRef.current.value);
        }

        if (event.key === "Escape") {
          onEditEnd(event);
        }
      }
    };

    document.body.addEventListener("keydown", handleKeydown);

    return () => {
      document.body.removeEventListener("keydown", handleKeydown);
    };
  }, [onEditEnd, onSet, edit]);

  useEffect(() => {
    const handleClick = event => {
      if (edit) {
        if (
          setElRef.current &&
          !setElRef.current.contains(event.target) &&
          textELRef.current &&
          !textELRef.current.contains(event.target)
        ) {
          onEditEnd(event);
        } else if (
          setElRef.current &&
          setElRef.current.contains(event.target)
        ) {
          onSet(event, textELRef.current.value);
        }
      }
    };

    setTimeout(() => document.body.addEventListener("click", handleClick), 50);

    return () => {
      document.body.removeEventListener("click", handleClick);
    };
  }, [onEditEnd, onSet, value, edit]);

  return (
    <>
      <>
        <span className="InlineEdit">
          <>
            {!edit && (
              <OverlayTrigger
                placement="top"
                overlay={<Tooltip id="tooltip">{tooltip}</Tooltip>}
              >
                <span>
                  <LinkButton onClick={() => onEditStart()}>
                    {value || "??"}
                  </LinkButton>
                </span>
              </OverlayTrigger>
            )}
            {edit && (
              <>
                <div className="input-controls">
                  <button
                    ref={setElRef}
                    type="button"
                    className="btn btn-xs btn-default"
                  >
                    <GlyphIcon type="ok" />
                  </button>
                  <Button bsSize="xs">
                    <GlyphIcon type="remove" />
                  </Button>
                </div>
                <input ref={textELRef} type="text" />
              </>
            )}
          </>
        </span>
      </>
    </>
  );
}

export default InlineEdit;
