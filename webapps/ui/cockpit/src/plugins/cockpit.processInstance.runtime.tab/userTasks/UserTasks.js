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
import withFilter from "../../../components/ProcessInstance/HOC/withFilter";
import withActivityInstanceMap from "../../../components/ProcessInstance/HOC/withActivityInstanceMap";
import { paginateComponent } from "components/Pagination";

import { getEngine } from "utils/config";
import { formatDate } from "utils/formatting";
import { getItem, setItem } from "utils/localstorage";
import { post } from "utils/request";

import {
  Clipboard,
  LinkButton,
  LoadingIndicator,
  Pagination,
  Table
} from "components";
import AssigneeEdit from "./AssigneeEdit";
import { UserAction, GroupAction } from "./Actions";

import "./UserTasks.scss";
import { OverlayTrigger, Tooltip } from "react-bootstrap";
import translate from "utils/translation";

function UserTasks({
  processInstanceId,
  filter,
  setFilter,
  activityIdToInstancesMap,
  startingPage = 1
}) {
  const [userTasks, setUserTasks] = useState(null);
  const [userTasksCount, setUserTasksCount] = useState();
  const [sortOrder, setSortOrder] = useState(
    getItem("sortPIUserTaskTab", {
      sortBy: "created",
      sortOrder: "desc"
    })
  );
  const [queryObject, setQueryObject] = useState({
    activityInstanceIdIn: filter.activityInstanceIds || [],
    processInstanceId: processInstanceId,
    sorting: [sortOrder]
  });
  const [loadingState, setLoadingState] = useState("LOADING");
  const [page, setPage] = useState(startingPage);

  // Generate new Query Object on filter or sort change
  useEffect(() => {
    let newQueryObject = {
      activityInstanceIdIn: filter.activityInstanceIds,
      processInstanceId: processInstanceId,
      sorting: [sortOrder]
    };

    if (
      JSON.stringify(queryObject.activityInstanceIds || []) !==
      JSON.stringify(filter.activityInstanceIds || [])
    ) {
      setPage(1);
    }

    if (!newQueryObject.activityInstanceIdIn) {
      delete newQueryObject.activityInstanceIdIn;
    }

    // Filter changes often, only update the Query object if we need to update it
    if (JSON.stringify(newQueryObject) !== JSON.stringify(queryObject)) {
      setQueryObject(newQueryObject);
    }
  }, [
    filter,
    processInstanceId,
    queryObject,
    queryObject.activityInstanceIds,
    sortOrder
  ]);

  // Fetch everything
  useEffect(() => {
    setLoadingState("LOADING");
    post(
      `%ENGINE_API%/task?maxResults=50&firstResult=${(page - 1) * 50}`,
      queryObject
    )
      .then(res => res.json())
      .then(setUserTasks)
      .then(() => setLoadingState("DONE"));
  }, [page, queryObject]);

  // Fetch new Task count
  useEffect(() => {
    post("%ENGINE_API%/task/count", queryObject)
      .then(res => res.json())
      .then(json => setUserTasksCount(json.count));
  }, [queryObject]);

  if (loadingState === "LOADING") {
    return <LoadingIndicator />;
  }

  if (!userTasks.length) {
    return <span>No user tasks</span>;
  }

  const handleSortChange = property => {
    const sortObj = {
      sortBy: property,
      sortOrder:
        sortOrder.sortBy === property
          ? sortOrder.sortOrder === "desc"
            ? "asc"
            : "desc"
          : "asc"
    };
    setItem("sortPIUserTaskTab", sortObj);
    setSortOrder(sortObj);
  };

  const handleSearch = activity => {
    setFilter({
      ...filter,
      activityIds: [activity],
      activityInstanceIds: [
        activityIdToInstancesMap[activity.taskDefinitionKey].filter(execution =>
          execution.executionIds.includes(activity.executionId)
        )[0].id
      ]
    });
  };

  const rows = [
    { searchQuery: "nameCaseInsensitive", label: "Activity" },
    { searchQuery: "assignee", label: "Assignee" },
    { label: "Owner" },
    { searchQuery: "created", label: "Creation Date" },
    { searchQuery: "dueDate", label: "Due Date" },
    { searchQuery: "followUpDate", label: "Follow Up Date" },
    { searchQuery: "priority", label: "Priority" },
    { label: "Delegation State" },
    { searchQuery: "id", label: "Task ID" },
    { label: "Action" }
  ];

  return (
    <>
      <Table
        className="UserTasks"
        head={rows.map((heading, idx) => {
          return (
            <Table.Head
              key={idx}
              onSort={
                heading.searchQuery
                  ? () => handleSortChange(heading.searchQuery)
                  : null
              }
              sortOrder={
                sortOrder.sortBy === heading.searchQuery
                  ? sortOrder.sortOrder
                  : null
              }
            >
              {heading.label}
            </Table.Head>
          );
        })}
      >
        {userTasks.map((row, idx) => {
          return (
            <Table.Row key={idx}>
              <Table.Cell>
                <LinkButton onClick={() => handleSearch(row)}>
                  {row.name || row.taskDefinitionKey}
                </LinkButton>
              </Table.Cell>
              <Table.Cell>
                <AssigneeEdit {...row} />
              </Table.Cell>
              <Table.Cell>{row.owner}</Table.Cell>
              <Table.Cell>{formatDate(row.created)}</Table.Cell>
              <Table.Cell>{formatDate(row.due)}</Table.Cell>
              <Table.Cell>{formatDate(row.followUp)}</Table.Cell>
              <Table.Cell>{row.priority}</Table.Cell>
              <Table.Cell>{row.delegationState}</Table.Cell>
              <Table.Cell>
                <Clipboard text={row.id}>
                  <OverlayTrigger
                    placement="top"
                    overlay={
                      <Tooltip id="tooltip">
                        {translate("PLUGIN_USER_TASKS_LINK_TO_TASKLIST")}
                      </Tooltip>
                    }
                  >
                    <a href={`../../tasklist/${getEngine()}/#/?task=${row.id}`}>
                      {row.id}
                    </a>
                  </OverlayTrigger>
                </Clipboard>
              </Table.Cell>
              <Table.Cell>
                <GroupAction {...row} />
                <UserAction {...row} />
              </Table.Cell>
            </Table.Row>
          );
        })}
      </Table>
      <Pagination
        current={page}
        pages={userTasksCount / 50}
        onPageChange={setPage}
      />
    </>
  );
}

export default withActivityInstanceMap(
  withFilter(paginateComponent(UserTasks))
);
