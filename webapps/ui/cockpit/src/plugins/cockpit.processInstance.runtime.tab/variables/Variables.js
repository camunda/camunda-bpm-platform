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

import React, { useCallback, useEffect, useState } from "react";
import withProcessInstance from "../../../components/ProcessInstance/HOC/withProcessInstance";
import withBpmn from "../../../components/ProcessInstance/HOC/withBpmn";
import withActivityInstances from "../../../components/ProcessInstance/HOC/withActivityInstances";
import translate from "utils/translation";
import { post, get } from "utils/request";
import { formatDate } from "utils/formatting";
import { getItem, setItem } from "utils/localstorage";
import { getSearchPillsConfig } from "./SearchPillsConfig";
import { SearchPills, TablePaginated, Table, Clipboard } from "components";

import "./Variables.scss";

const ROWS_PER_PAGE = 50;
const PAYLOAD = {};

const header = [
  { searchQuery: "variableName", label: "PLUGIN_VARIABLE_NAME" },
  { searchQuery: "variableType", label: "PLUGIN_VARIABLE_TYPE" },
  { label: "PLUGIN_VARIABLE_VALUE" },
  { label: "PLUGIN_VARIABLE_SCOPE" }
];

const shorten = (input, length) =>
  input.length > length ? input.substring(0, length) + "..." : input;

const getActivityName = bpmnElement => {
  if (bpmnElement) {
    const name = bpmnElement.name;
    if (!name) {
      return (
        bpmnElement.$type.substr(5) + " (" + shorten(bpmnElement.id, 8) + ")"
      );
    }

    return name;
  }
};

function Variables({
  processInstance,
  engineApi,
  activityInstances,
  bpmnElements,
  instanceIdToInstancesMap
}) {
  const searchPillsConfig = React.useMemo(() => {
    return getSearchPillsConfig();
  }, []);

  const { id, definitionId } = processInstance;

  const [search, setSearch] = useState();
  const [sortOrder, setSortOrder] = useState(
    getItem("sortHistVarInstTab", {
      sortBy: "variableName",
      sortOrder: "asc"
    })
  );
  const [rowsCount, setRowsCount] = useState(0);
  const [definition, setDefinition] = useState();

  useEffect(() => {
    if (definitionId) {
      const loadDefinition = async () => {
        const definition = await (
          await get(`%ENGINE_API%/process-definition/${definitionId}`)
        ).json();
        setDefinition(definition);
      };
      loadDefinition();
    }
  }, [definitionId]);

  const fetchPage = useCallback(
    page => {
      const loadJobs = async () => {
        return await (
          await post(
            `%ENGINE_API%/variable-instance?firstResult=${(page - 1) *
              ROWS_PER_PAGE}&maxResults=${ROWS_PER_PAGE}&deserializeValues=false`,
            Object.assign(
              {
                processInstanceIdIn: [id],
                sorting: [{ ...sortOrder }]
              },
              PAYLOAD,
              search
            )
          )
        ).json();
      };
      return loadJobs();
    },
    [id, search, sortOrder]
  );

  const fetchRowsCount = useCallback(() => {
    const fetchJobsCount = async () => {
      return await (
        await post(
          `%ENGINE_API%/variable-instance/count`,
          Object.assign(
            {
              processInstanceIdIn: [id]
            },
            PAYLOAD,
            search
          )
        )
      ).json();
    };

    return fetchJobsCount();
  }, [id, search]);

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
    setItem("sortHistVarInstTab", sortObj);
    setSortOrder(sortObj);
  };

  const getSearchQuery = activityInstanceId => {
    if (activityInstanceId) {
      return encodeURI(
        JSON.stringify([
          {
            type: "activityInstanceIdIn",
            operator: "eq",
            name: "",
            value: activityInstanceId
          }
        ])
      );
    }
  };

  const getActivityInstance = activityInstanceId => {
    if (instanceIdToInstancesMap) {
      const activityInstance = instanceIdToInstancesMap[activityInstanceId];
      if (activityInstance) {
        return activityInstance;
      } else {
        return activityInstances;
      }
    }
  };

  const getScope = activityInstanceId => {
    const activityInstance = getActivityInstance(activityInstanceId);
    return activityInstance?.activityName
      ? activityInstance?.activityName
      : activityInstance.name
      ? activityInstance.name
      : getActivityName(bpmnElements[definition?.key]);
  };

  const handleSearch = useCallback(payload => setSearch(payload), []);

  return (
    <>
      <div className="VariablesTab">
        <SearchPills
          rowsCount={rowsCount}
          criteria={searchPillsConfig.criteria}
          labels={searchPillsConfig.labels}
          onChange={handleSearch}
        />

        {search && (
          <TablePaginated
            rowsPerPage={ROWS_PER_PAGE}
            header={header.map((head, idx) => {
              return (
                <>
                  {head.searchQuery && (
                    <Table.Head
                      key={idx}
                      onSort={() => handleSortChange(head.searchQuery)}
                      sortOrder={
                        sortOrder.sortBy === head.searchQuery
                          ? sortOrder.sortOrder
                          : null
                      }
                    >
                      {translate(head.label)}
                    </Table.Head>
                  )}
                  {!head.searchQuery && (
                    <Table.Head key={idx}>{translate(head.label)}</Table.Head>
                  )}
                </>
              );
            })}
            generateRow={variable => (
              <>
                <Table.Cell>
                  <Clipboard text={variable.name}>{variable.name}</Clipboard>
                </Table.Cell>
                <Table.Cell>{variable.type}</Table.Cell>
                <Table.Cell>
                  <>
                    {/*TODO: Object, JSON, XML support*/}
                    {variable.type !== "File" &&
                      variable.type !== "Object" &&
                      variable.type !== "Date" && (
                        <Clipboard text={variable.value + ""}>
                          {variable.value + ""}
                        </Clipboard>
                      )}
                    {variable.type === "Date" && (
                      <Clipboard text={formatDate(variable.value)}>
                        {formatDate(variable.value)}
                      </Clipboard>
                    )}
                    {variable.type === "File" && (
                      <a
                        href={`${engineApi}/variable-instance/${variable.id}/data`}
                        download={variable.name + "-data"}
                      >
                        Download
                      </a>
                    )}
                  </>
                </Table.Cell>
                <Table.Cell>
                  {
                    <a
                      href={`#/process-instance/${id}/runtime?detailsTab=variables-tab&searchQuery=${getSearchQuery(
                        getActivityInstance(variable.activityInstanceId)?.id
                      )}`}
                      title={
                        getActivityInstance(variable.activityInstanceId)?.id
                      }
                    >
                      {getScope(variable.activityInstanceId)}
                    </a>
                  }
                </Table.Cell>
              </>
            )}
            fetchPage={fetchPage}
            fetchRowsCount={fetchRowsCount}
            onRowsCount={setRowsCount}
            noRows={translate("PLUGIN_VARIABLE_NO_PROCESS_VARIABLES")}
          />
        )}
      </div>
    </>
  );
}

export default withProcessInstance(withActivityInstances(withBpmn(Variables)));
