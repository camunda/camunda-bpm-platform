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
import { addMessage, addError } from "utils/notifications";
import { post } from "utils/request";
import translate from "utils/translation";

import { FormControl, Form, Button } from "react-bootstrap";
import { GlyphIcon } from "components";

import "./AssigneeEdit.scss";

export default function AssigneeEdit({ id, assignee, name }) {
  const [editing, setEditing] = useState(false);
  const [content, setContent] = useState(assignee || "");
  const [originalValue, setOriginalValue] = useState(assignee);

  function handleClick() {
    setOriginalValue(content);
    setEditing(true);
  }
  if (!editing) {
    return (
      <span className="AssigneeEdit">
        {content}
        &nbsp;
        <GlyphIcon type="pencil" onClick={handleClick} />
      </span>
    );
  }

  function handleSubmit() {
    post(`%ENGINE_API%/task/${id}/assignee`, { userId: content || null })
      .then(() => {
        const placeholders = {
          name,
          assignee: content
        };
        const message = content
          ? "PLUGIN_USER_TASKS_MESSAGE_1"
          : "PLUGIN_USER_TASKS_MESSAGE_2";

        addMessage({
          status: translate("PLUGIN_USER_TASKS_STATUS_ASSIGNEE"),
          message: translate(message, placeholders)
        });
      })
      .catch(err => {
        const placeholders = {
          name,
          assignee: content,
          error: err
        };
        const message = content
          ? "PLUGIN_USER_TASKS_MESSAGE_3"
          : "PLUGIN_USER_TASKS_MESSAGE_4";

        addError({
          status: translate("PLUGIN_USER_TASKS_STATUS_ASSIGNEE"),
          message: translate(message, placeholders),
          exclusive: true
        });
        setContent(originalValue);
      });
    setEditing(false);
  }

  function handleAbort() {
    setContent(originalValue);
    setEditing(false);
  }

  return (
    <Form onSubmit={handleSubmit} className="AssigneeEdit">
      <FormControl
        value={content}
        onChange={e => setContent(e.target.value)}
      ></FormControl>
      <div className="control">
        <Button onClick={handleSubmit} bsStyle="primary" bsSize="small">
          <GlyphIcon type="ok" />
        </Button>
        <Button onClick={handleAbort} bsStyle="default" bsSize="small">
          <GlyphIcon type="ban-circle" />
        </Button>
      </div>
    </Form>
  );
}
