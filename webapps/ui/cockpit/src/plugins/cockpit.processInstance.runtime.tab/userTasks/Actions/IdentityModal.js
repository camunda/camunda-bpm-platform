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
import translate from "utils/translation";
import { get, post } from "utils/request";

import { Button, Modal, InputGroup, FormControl } from "react-bootstrap";
import { GlyphIcon, Table, Notifications } from "components";
import { addError } from "utils/notifications";

import "./IdentityModal.scss";

export default function GroupModal({ id, open, onClose, type }) {
  const [identityLinks, setIdentityLinks] = useState([]);
  const [newEntry, setNewEntry] = useState("");

  const labels =
    type === "group"
      ? {
          submitError: translate("PLUGIN_USER_TASKS_NOTIFICATION_ADD_GROUP"),
          deleteError: translate("PLUGIN_USER_TASKS_NOTIFICATION_REMOVE_GROUP"),
          header: translate("PLUGIN_USER_TASKS_MANAGE_GROUPS"),
          current: translate("PLUGIN_USER_TASKS_CURRENT_GROUPS"),
          tableId: translate("PLUGIN_USER_TASKS_GROUP_ID"),
          tableAction: translate("PLUGIN_USER_TASKS_ACTION"),
          add: translate("PLUGIN_USER_TASKS_ADD_GROUP"),
          close: translate("CLOSE")
        }
      : {
          submitError: translate("PLUGIN_USER_TASKS_NOTIFICATION_ADD_USER"),
          deleteError: translate("PLUGIN_USER_TASKS_NOTIFICATION_REMOVE_USER"),
          header: translate("PLUGIN_USER_TASKS_MANAGE_USERS"),
          current: translate("PLUGIN_USER_TASKS_CURRENT_USERS"),
          tableId: translate("PLUGIN_USER_TASKS_USER_ID"),
          tableAction: translate("PLUGIN_USER_TASKS_ACTION"),
          add: translate("PLUGIN_USER_TASKS_ADD_USER"),
          close: translate("CLOSE")
        };

  useEffect(() => {
    async function fetchIdentityLinks() {
      setIdentityLinks(
        await (await get(`%ENGINE_API%/task/${id}/identity-links`)).json()
      );
    }

    if (id && open) {
      fetchIdentityLinks();
    }
  }, [id, open]);

  const displayedLinks = identityLinks.filter(
    el => el.type === "candidate" && (type === "group" ? el.groupId : el.userId)
  );

  function handleSubmit() {
    const payload =
      type === "group"
        ? { type: "candidate", groupId: newEntry }
        : { type: "candidate", userId: newEntry };
    post(`%ENGINE_API%/task/${id}/identity-links`, payload)
      .then(() => {
        setNewEntry("");
        setIdentityLinks([...identityLinks, payload]);
      })
      .catch(async err => {
        addError({
          status: labels.submitError,
          message: err.message,
          exclusive: true
        });
      });
  }

  function deleteGroup(identity) {
    post(`%ENGINE_API%/task/${id}/identity-links/delete`, identity)
      .then(() => {
        setIdentityLinks(identityLinks.filter(el => el !== identity));
      })
      .catch(async err => {
        addError({
          status: labels.deleteError,
          message: err.message,
          exclusive: true
        });
      });
  }

  function handleClose() {
    setNewEntry("");
    onClose();
  }

  return (
    <Modal show={open} onHide={handleClose} className="IdentityModal">
      <Modal.Header>
        <Modal.Title componentClass="h3">{labels.header}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Notifications />
        <form onSubmit={handleSubmit}>
          {displayedLinks.length ? (
            <>
              <legend>{labels.current}</legend>
              <Table
                head={
                  <>
                    <Table.Head key="id">{labels.tableId}</Table.Head>
                    <Table.Head key="action" className={"text-right"}>
                      {labels.tableAction}
                    </Table.Head>
                  </>
                }
              >
                {displayedLinks.map((group, idx) => (
                  <Table.Row key={idx}>
                    <Table.Cell key="id">
                      {type === "group" ? group.groupId : group.userId}
                    </Table.Cell>
                    <Table.Cell key="action" className={"text-right"}>
                      <Button onClick={() => deleteGroup(group)}>
                        <GlyphIcon type="ban-circle" />
                      </Button>
                    </Table.Cell>
                  </Table.Row>
                ))}
              </Table>
            </>
          ) : null}

          <legend>{labels.add}</legend>
          <InputGroup>
            <FormControl
              type="text"
              value={newEntry}
              onChange={e => setNewEntry(e.target.value)}
            />
            <InputGroup.Button>
              <Button
                bsStyle="primary"
                disabled={!newEntry}
                onClick={handleSubmit}
              >
                <GlyphIcon type="plus" />
              </Button>
            </InputGroup.Button>
          </InputGroup>
        </form>
      </Modal.Body>
      <Modal.Footer>
        <Button bsStyle="default" onClick={handleClose}>
          {labels.close}
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
