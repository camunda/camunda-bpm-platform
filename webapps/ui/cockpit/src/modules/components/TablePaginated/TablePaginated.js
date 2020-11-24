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

const ZERO_NOOP = () => 0;
const NOOP = () => {};

/**
 * A paginated table component.
 *
 * @param rowsPerPage is the number of rows which are displayed per page
 * @param header is a {JSX.Element} which is placed inside the <thead>...</thead> of the table
 * @param generateRow is a function which is called for each row, passes the row object and returns a {JSX.Element}
 * @param fetchPage is a function which returns the page, passes the page number and returns an array of rows (can be a promise); each row must have a unique id
 * @param fetchRowsCount is a function which returns the total amount of rows wrapped in a promise
 * @param noRows is {JSX.Element} which is rendered when the rows count is zero
 * @param onRowSelection is a function which is called when a row is selected and passes the selected rows
 * @returns the TablePaginated component as {JSX.Element}
 */
function TablePaginated({
  rowsPerPage = 5,
  header,
  generateRow,
  fetchPage,
  fetchRowsCount = ZERO_NOOP,
  noRows = null,
  onRowSelection = NOOP
}) {
  const [allSelected, setAllSelected] = useState(false);
  const [page, setPage] = useState(1);
  const [rows, setRows] = useState([]);
  const [rowsCount, setRowsCount] = useState(0);
  const [rowSelection, setRowSelection] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (fetchRowsCount !== ZERO_NOOP) {
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
    onRowSelection(rowSelection);
  }, [onRowSelection, rowSelection]);

  const checkRowSelected = rowId => {
    return Object.keys(rowSelection).includes(rowId);
  };

  const select = (e, row) => {
    if (e.target.checked) {
      setRowSelection({
        ...rowSelection,
        ...{ [row.id]: row }
      });
    } else {
      const { [row.id]: value, ...newRowSelection } = rowSelection;
      setRowSelection(newRowSelection);
    }
  };

  const selectAll = e => {
    const rowIds = rows.map(row => row.id);

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
      for (const [rowId, cache] of Object.entries(rowSelection)) {
        if (!rowIds.includes(rowId)) {
          newRowSelection[rowId] = cache;
        }
      }
      setRowSelection(newRowSelection);
    }
    setAllSelected(checked);
  };

  if (loading) {
    return (
      <div className={classNames("TablePaginated")}>
        <LoadingIndicator />
      </div>
    );
  }

  if (rowsCount === 0) {
    return (
      <div className={classNames("TablePaginated")}>
        <p className="no-rows">{noRows}</p>
      </div>
    );
  }

  return (
    <div className={classNames("TablePaginated")}>
      <Table
        className={classNames("cam-table")}
        head={
          <>
            {onRowSelection !== NOOP && (
              <Table.Head>
                <input
                  type="checkbox"
                  checked={allSelected}
                  onChange={selectAll}
                />
              </Table.Head>
            )}
            {header}
          </>
        }
      >
        {rows.map((row, idx) => {
          return (
            <Table.Row key={idx}>
              {onRowSelection !== NOOP && (
                <Table.Cell>
                  <input
                    type="checkbox"
                    checked={checkRowSelected(row.id)}
                    onChange={e => select(e, row)}
                  />
                </Table.Cell>
              )}
              {generateRow(row)}
            </Table.Row>
          );
        })}
      </Table>

      <Pagination
        current={page}
        pages={rowsCount / rowsPerPage}
        onPageChange={setPage}
        enableSearch={false}
      />
    </div>
  );
}

export default TablePaginated;
