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
import { Pagination as BsPagination } from "react-bootstrap";
import { LinkButton } from "../LinkButton";
import classNames from "classnames";

import "./Pagination.scss";
import search from "utils/search";

function Pagination({
  current = 1,
  pages = 0,
  maxItems = 7,
  onPageChange = () => {}
}) {
  pages = Math.ceil(pages);

  function handlePageChange(page) {
    const searchPage = search.get("page");
    if (searchPage !== page.toString()) {
      search.set("page", page);
    }

    if (!page || page === current || page > pages) {
      return;
    }
    onPageChange(page);
  }

  if (!pages || pages <= 1) {
    return null;
  }

  let start;

  if (current + Math.floor(maxItems / 2) < pages) {
    start = current - Math.floor(maxItems / 2);
  } else {
    start = pages - maxItems + 1;
  }

  start = Math.max(start, 1);

  const elements = [];

  for (let i = 0; i < maxItems; i++) {
    const pageNumber = start + i;
    if (pageNumber > pages) break;
    elements.push(
      <li
        key={i}
        className={classNames(
          "pagination-page",
          pageNumber === current ? "active" : null
        )}
      >
        <LinkButton onClick={() => handlePageChange(pageNumber)}>
          {pageNumber}
        </LinkButton>
      </li>
    );
  }

  return (
    <BsPagination bsSize="small" className="Pagination">
      <li
        key="first"
        className={classNames(
          "pagination-page",
          current === 1 ? "disabled" : null
        )}
      >
        <LinkButton onClick={() => handlePageChange(1)}>First</LinkButton>
      </li>
      <li
        key="prev"
        className={classNames(
          "pagination-page",
          current === 1 ? "disabled" : null
        )}
      >
        <LinkButton onClick={() => handlePageChange(current - 1)}>
          Previous
        </LinkButton>
      </li>
      {elements}
      <li
        key="next"
        className={classNames(
          "pagination-page",
          current === pages ? "disabled" : null
        )}
      >
        <LinkButton onClick={() => handlePageChange(current + 1)}>
          Next
        </LinkButton>
      </li>
      <li
        key="last"
        className={classNames(
          "pagination-page",
          current === pages ? "disabled" : null
        )}
      >
        <LinkButton onClick={() => handlePageChange(pages)}>Last</LinkButton>
      </li>
    </BsPagination>
  );
}

export default Pagination;
