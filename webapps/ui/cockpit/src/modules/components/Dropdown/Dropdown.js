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

import React, { useState, useEffect, useRef } from "react";
import classNames from "classnames";

import DropdownOption from "./DropdownOption";
import "./Dropdown.scss";

export default function Dropdown({ title, children, position }) {
  const [isOpen, setIsOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    const handleClick = ({ target }) => {
      // Close if we click outside of the menu or on a Option
      if (
        isOpen &&
        (!ref.current.contains(target) || target.closest(".DropdownOption"))
      ) {
        setIsOpen(false);
      }
    };

    document.body.addEventListener("click", handleClick, true);
    return () => {
      document.body.removeEventListener("click", handleClick, true);
    };
  }, [isOpen]);

  return (
    <div className="Dropdown" ref={ref}>
      <div
        className="title"
        onClick={() => {
          setIsOpen(!isOpen);
        }}
      >
        {title}
      </div>
      <ul className={classNames("menu", position, { open: isOpen })}>
        {children}
      </ul>
    </div>
  );
}

Dropdown.Option = DropdownOption;
Dropdown.Divider = function Divider() {
  return <li className="divider"></li>;
};
