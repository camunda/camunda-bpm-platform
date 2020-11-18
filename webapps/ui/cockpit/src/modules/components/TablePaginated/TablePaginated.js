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

import React, { useEffect, useState } from "react";
import classNames from "classnames";
import { Pagination, Table, LoadingIndicator } from "components";

import "./TablePaginated.scss";

function TablePaginated({
  rowsPerPage = 5,
  columns,
  cells,
  fetchPage,
  fetchRowsCount = null,
  noRows = null,
  onRowSelection = null
}) {
  const [allSelected, setAllSelected] = useState(false);
  const [page, setPage] = useState(1);
  const [rows, setRows] = useState([]);
  const [rowsCount, setRowsCount] = useState(0);
  const [rowSelection, setRowSelection] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (fetchRowsCount) {
      fetchRowsCount().then(rowsCount => setRowsCount(rowsCount.count));
    } else {
      setRowsCount(1);
    }
  }, [fetchRowsCount]);

  useEffect(() => {
    setAllSelected(false);
    setLoading(true);

    Promise.resolve(fetchPage(page)).then(rows => {
      setLoading(false);
      setRows(typeof rows === "object" ? Object.values(rows) : rows);
    });
  }, [page, fetchPage]);

  useEffect(() => {
    if (onRowSelection) {
      onRowSelection(rowSelection);
    }
  }, [onRowSelection, rowSelection]);

  const checkRowSelected = rowId => {
    return Object.keys(rowSelection).includes(rowId);
  };

  return (
    <div className={classNames("TablePaginated")}>
      {loading && <LoadingIndicator />}
      <>
        {rowsCount > 0 && (
          <>
            {!loading && (
              <Table
                className={classNames("cam-table")}
                head={
                  <>
                    {onRowSelection && (
                      <Table.Head>
                        <input
                          type="checkbox"
                          checked={allSelected}
                          onChange={e => {
                            const rowIds = rows.map(row => {
                              return row.id;
                            });

                            const checked = e.target.checked;
                            if (checked) {
                              const selectedRows = {};
                              const rowSelectionIds = Object.keys(rowSelection);
                              rows
                                .filter(row => {
                                  return !rowSelectionIds.includes(row["id"]);
                                })
                                .forEach(row => {
                                  selectedRows[row.id] = row;
                                });

                              setRowSelection({
                                ...rowSelection,
                                ...selectedRows
                              });
                            } else {
                              const newRowSelection = {};
                              for (const [rowId, cache] of Object.entries(
                                rowSelection
                              )) {
                                if (!rowIds.includes(rowId)) {
                                  newRowSelection[rowId] = cache;
                                }
                              }
                              setRowSelection(newRowSelection);
                            }
                            setAllSelected(checked);
                          }}
                        />
                      </Table.Head>
                    )}
                    {columns}
                  </>
                }
              >
                {rows.map((row, idx) => {
                  return (
                    <Table.Row key={idx}>
                      {onRowSelection && (
                        <Table.Cell>
                          <input
                            type="checkbox"
                            checked={checkRowSelected(row.id)}
                            onChange={e => {
                              if (e.target.checked) {
                                setRowSelection({
                                  ...rowSelection,
                                  ...{ [row.id]: row }
                                });
                              } else {
                                const {
                                  [row.id]: value,
                                  ...newRowSelection
                                } = rowSelection;
                                setRowSelection(newRowSelection);
                              }
                            }}
                          />
                        </Table.Cell>
                      )}
                      {cells(row)}
                    </Table.Row>
                  );
                })}
              </Table>
            )}
            <Pagination
              current={page}
              pages={rowsCount / rowsPerPage}
              onPageChange={setPage}
              enableSearch={false}
            />
          </>
        )}
        {!loading && rowsCount === 0 && noRows && (
          <>
            <p className="no-rows">{noRows}</p>
          </>
        )}
      </>
    </div>
  );
}

export default TablePaginated;
