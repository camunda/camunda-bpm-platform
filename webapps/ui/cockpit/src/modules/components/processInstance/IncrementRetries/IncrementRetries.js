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
import { Button, Modal } from "react-bootstrap";
import withProcessInstance from "../../../../components/ProcessInstance/HOC/withProcessInstance";
import withActivityInstanceMap from "../../../../components/ProcessInstance/HOC/withActivityInstanceMap";
import translate from "utils/translation";
import { addMessage, addError, clearAll } from "utils/notifications";
import { post, put } from "utils/request";
import {
  Notifications,
  ActionButton,
  Table,
  GlyphIcon,
  TablePaginated
} from "components";

const STATUS = Object.freeze({
  INITIAL: 1,
  ACTION_PENDING: 2,
  CONFIRMATION: 3,
  FAILED: 4,
  NOT_FOUND: 5
});

const ROWS_PER_PAGE = 5;

const JOBS_PAYLOAD = {
  withException: true,
  noRetriesLeft: true
};

function IncrementRetries({ processInstance, instanceIdToInstancesMap }) {
  const { id } = processInstance;

  const [show, setShow] = useState(false);
  const [status, setStatus] = useState(null);

  const [jobSelection, setJobSelection] = useState({});

  const [executionIdToInstanceMap, setExecutionIdToInstanceMap] = useState({});

  useEffect(() => {
    const mapExecutionIdToInstances = instanceIdToInstanceMap => {
      const executionIdToInstanceMap = {};

      Object.keys(instanceIdToInstanceMap || {}).forEach(key => {
        const instance = instanceIdToInstanceMap[key],
          executionIds = instance.executionIds,
          executionId = instance.executionId;

        if (executionIds) {
          for (let i = 0, execId; (execId = executionIds[i]); i++) {
            executionIdToInstanceMap[execId] = instance;
          }
        }

        if (executionId) {
          executionIdToInstanceMap[executionId] = instance;
        }
      });

      return executionIdToInstanceMap;
    };

    setExecutionIdToInstanceMap(
      mapExecutionIdToInstances(instanceIdToInstancesMap)
    );
  }, [instanceIdToInstancesMap]);

  useEffect(() => {
    if (id) {
      if (show) {
        setStatus(STATUS.INITIAL);
      } else {
        setStatus(null);
      }
    } else {
      setStatus(STATUS.NOT_FOUND);
    }
  }, [id, show]);

  const incrementRetries = async () => {
    const incrementedJobs = [];
    for (const jobId of Object.keys(jobSelection)) {
      const job = {
        id: jobId,
        executionId: jobSelection[jobId].executionId
      };
      incrementedJobs.push(job);
    }

    setStatus(STATUS.ACTION_PENDING);
    setJobSelection(incrementedJobs);

    let allSuccessful = true;
    for (const job of incrementedJobs) {
      try {
        await put(`%ENGINE_API%/job/${job.id}/retries`, { retries: 1 });
        job.status = "successful";
      } catch (e) {
        job.status = "failed";
        allSuccessful = false;
      }
    }

    if (allSuccessful) {
      setStatus(STATUS.CONFIRMATION);
      addMessage({
        status: translate("PLUGIN_JOB_RETRY_STATUS_FINISHED"),
        message: translate("PLUGIN_JOB_RETRY_MESSAGE_2"),
        exclusive: true
      });
    } else {
      setStatus(STATUS.FAILED);
      addError({
        status: translate("PLUGIN_JOB_RETRY_STATUS_FINISHED"),
        message: translate("PLUGIN_JOB_RETRY_ERROR_2"),
        exclusive: true
      });
    }
  };

  const closeDialog = () => {
    if (!check(STATUS.ACTION_PENDING)) {
      setShow(false);
      clearAll();
    }
  };

  const check = checkedStatus => {
    return status === checkedStatus;
  };

  const fetchPage = useCallback(
    page => {
      const loadJobs = async () => {
        return await (await post(
          `%ENGINE_API%/job?firstResult=${(page - 1) *
            ROWS_PER_PAGE}&maxResults=${ROWS_PER_PAGE}`,
          Object.assign(
            {
              processInstanceId: id
            },
            JOBS_PAYLOAD
          )
        )).json();
      };
      return loadJobs();
    },
    [id]
  );

  const fetchRowsCount = useCallback(() => {
    const fetchJobsCount = async () => {
      return await (await post(
        `%ENGINE_API%/job/count`,
        Object.assign(
          {
            processInstanceId: id
          },
          JOBS_PAYLOAD
        )
      )).json();
    };

    return fetchJobsCount();
  }, [id]);

  const CommonTableHeads = () => (
    <>
      <Table.Head>{translate("PLUGIN_JOB_RETRY_BULK_ID")}</Table.Head>
      <Table.Head>{translate("PLUGIN_JOB_RETRY_BULK_SCOPE")}</Table.Head>
    </>
  );

  const CommonTableCells = props => (
    <>
      <Table.Cell className="uuid">{props.job.id}</Table.Cell>
      <Table.Cell>
        {executionIdToInstanceMap[props.job.executionId]?.activityName}
      </Table.Cell>
    </>
  );

  return (
    <>
      <ActionButton
        labels={{
          enabled: "PLUGIN_JOB_RETRY_BULK_RETRIES_FAILED_JOB",
          disabled: "PLGN_HIST_INCREMENT_RETRIES_NOT_POSSIBLE"
        }}
        icon="repeat"
        onClick={() => setShow(true)}
        disabled={check(STATUS.NOT_FOUND)}
      />

      <Modal bsSize="large" show={show} onHide={closeDialog}>
        <Modal.Header>
          <h3>{translate("PLUGIN_JOB_RETRY_BULK_INCREMENT_RETRIES")}</h3>
        </Modal.Header>

        <Modal.Body>
          <Notifications />

          {(check(STATUS.INITIAL) || check(STATUS.ACTION_PENDING)) && (
            <p>{translate("PLUGIN_JOB_RETRY_BULK_SELECT_FAILED_JOB")}</p>
          )}

          {check(STATUS.INITIAL) && (
            <>
              <TablePaginated
                rowsPerPage={ROWS_PER_PAGE}
                fetchRowsCount={fetchRowsCount}
                fetchPage={fetchPage}
                onRowSelection={setJobSelection}
                columns={
                  <>
                    <CommonTableHeads />
                    <Table.Head>
                      {translate("PLUGIN_JOB_RETRY_BULK_EXCEPTION")}
                    </Table.Head>
                  </>
                }
                cells={job => {
                  return (
                    <>
                      <CommonTableCells job={job} />
                      <Table.Cell>{job.exceptionMessage}</Table.Cell>
                    </>
                  );
                }}
                noRows={translate(
                  "PLUGIN_JOB_RETRY_BULK_THERE_ARE_NO_FAILED_JOBS"
                )}
              />
            </>
          )}

          {(check(STATUS.CONFIRMATION) ||
            check(STATUS.FAILED) ||
            check(STATUS.ACTION_PENDING)) && (
            <>
              <TablePaginated
                fetchPage={() => jobSelection}
                columns={
                  <>
                    <CommonTableHeads />
                    <Table.Head>
                      {translate("PLUGIN_JOB_RETRY_BULK_STATUS")}
                    </Table.Head>
                  </>
                }
                cells={job => {
                  return (
                    <>
                      <CommonTableCells job={job} />
                      <Table.Cell>
                        {check(STATUS.ACTION_PENDING) && (
                          <GlyphIcon className="animate-spin" type="refresh" />
                        )}
                        {(check(STATUS.CONFIRMATION) ||
                          check(STATUS.FAILED)) && (
                          <>
                            {job.status === "successful" && (
                              <>
                                <GlyphIcon type="ok" />
                                {" " +
                                  translate("PLUGIN_JOB_RETRY_BULK_SUCCESSFUL")}
                              </>
                            )}
                            {job.status === "failed" && (
                              <>
                                <GlyphIcon type="remove" />
                                {" " +
                                  translate("PLUGIN_JOB_RETRY_BULK_FAILED")}
                              </>
                            )}
                          </>
                        )}
                      </Table.Cell>
                    </>
                  );
                }}
              />
            </>
          )}
        </Modal.Body>

        <Modal.Footer>
          {(check(STATUS.INITIAL) || check(STATUS.ACTION_PENDING)) && (
            <>
              <Button
                disabled={check(STATUS.ACTION_PENDING)}
                onClick={closeDialog}
              >
                {translate("PLUGIN_JOB_RETRY_BULK_CLOSE")}
              </Button>
              <Button
                bsStyle="primary"
                disabled={
                  check(STATUS.ACTION_PENDING) ||
                  Object.keys(jobSelection).length === 0
                }
                onClick={incrementRetries}
              >
                {translate("PLUGIN_JOB_RETRY_BULK_RETRY")}
                {Object.keys(jobSelection).length > 1 &&
                  " " +
                    translate("PLUGIN_JOB_RETRY_BULK_JOBS", {
                      count: Object.keys(jobSelection).length
                    })}
              </Button>
            </>
          )}

          {(check(STATUS.CONFIRMATION) || check(STATUS.FAILED)) && (
            <>
              <Button bsStyle="primary" onClick={closeDialog}>
                {translate("PLUGIN_JOB_RETRY_BULK_OK")}
              </Button>
            </>
          )}
        </Modal.Footer>
      </Modal>
    </>
  );
}

export default withProcessInstance(withActivityInstanceMap(IncrementRetries));
