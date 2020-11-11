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

import React from "react";
import classNames from "classnames";

import "./Table.scss";
import { LinkButton } from "components/LinkButton";

export default function Table({ head, children, className, ...props }) {
  return (
    <table className={classNames("Table", className)} {...props}>
      <thead>
        <tr>{head}</tr>
      </thead>
      <tbody>{children}</tbody>
    </table>
  );
}

Table.Head = function Head({ children, sortOrder, onSort, className }) {
  if (!onSort) {
    return <th className={classNames("TableHead", className)}>{children}</th>;
  }

  let sortIcon;
  switch (sortOrder) {
    case "asc":
      sortIcon = "glyphicon-chevron-up";
      break;

    case "desc":
      sortIcon = "glyphicon-chevron-down";
      break;

    default:
      sortIcon = "glyphicon-minus";
      break;
  }

  return (
    <th className={classNames("TableHead", className)}>
      {children} &nbsp;
      <LinkButton onClick={onSort}>
        <span className={classNames("glyphicon", sortIcon)}></span>
      </LinkButton>
    </th>
  );
};

Table.Row = function Row({ children, className }) {
  return <tr className={classNames("TableRow", className)}>{children}</tr>;
};

Table.Cell = function Cell({ children, className }) {
  return <td className={classNames("TableCell", className)}>{children}</td>;
};
