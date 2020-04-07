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

import { Table } from "./Table";

function UserOperationCount({ processDefinitionId }) {
  const [opLog, setOpLog] = useState();

  console.log(opLog);

  useEffect(() => {
    fetch(
      `/camunda/api/engine/engine/default/history/user-operation?maxResults=2000&processDefinitionId=${processDefinitionId}`
    )
      .then(async res => {
        setOpLog(await res.json());
      })
      .catch(err => {
        console.error(err);
      });
  }, [processDefinitionId]);

  if (!opLog) {
    return <div>Loading...</div>;
  }

  const userMap = {};

  opLog.forEach(element => {
    const currentEntry = userMap[element.userId] || {
      categories: new Set(),
      operations: 0
    };

    currentEntry.operations++;
    currentEntry.categories.add(element.category);

    userMap[element.userId] = currentEntry;
  });

  return (
    <Table
      head={
        <>
          <Table.Head key="id">UserId</Table.Head>
          <Table.Head key="role">User Roles</Table.Head>
          <Table.Head key="count">Number of Operations</Table.Head>
        </>
      }
    >
      {Object.keys(userMap).map(value => {
        const user = userMap[value];
        return (
          <Table.Row key={value}>
            <Table.Cell key="id">{value}</Table.Cell>
            <Table.Cell key="role">
              {[...user.categories].join(", ")}
            </Table.Cell>
            <Table.Cell key="count">{user.operations}</Table.Cell>
          </Table.Row>
        );
      })}
    </Table>
  );
}

export default UserOperationCount;
