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

import React, { useRef, useState, useEffect } from "react";
import { Dropdown } from "components/Dropdown";

const emphasize = (token, emphasized) => {
  if (emphasized) {
    return token.replace(
      new RegExp(emphasized, "gi"),
      string => `<strong>${string}</strong>`
    );
  } else {
    return token;
  }
};

const getIdx = (cursor, criteriaCount) =>
  cursor < 0
    ? (criteriaCount - (Math.abs(cursor) % criteriaCount)) % criteriaCount
    : cursor % criteriaCount;

function Typeahead({
  options,
  onSelect,
  value,
  onToggle = () => {},
  toggleEL,
  optionLabels = {},
  placeholderLabel
}) {
  const [show, setShow] = useState(false);
  const [emphasized, setEmphasized] = useState("");
  const [filteredOptions, setFilteredOptions] = useState([]);

  const [cursor, setCursor] = useState(0);

  useEffect(() => {
    setFilteredOptions(options);
  }, [options]);

  useEffect(() => {
    if (value && toggleEL) {
      toggleEL.current.value = value;
      toggleEL.current.focus();
      toggleEL.current.select();
    }
  }, [value, toggleEL]);

  useEffect(() => {
    const handler = event => {
      if (event.key === "ArrowDown") {
        setCursor(cursor + 1);
      }
      if (event.key === "ArrowUp") {
        setCursor(cursor - 1);
      }
      if (
        event.key === "Enter" ||
        event.key === "Tab" ||
        event.key === "Escape"
      ) {
        if (event.key === "Tab" || event.key === "Escape") {
          event.preventDefault();
        }

        if (event.key !== "Escape") {
          if (filteredOptions.length > 0) {
            const selectedOption =
              filteredOptions[getIdx(cursor, filteredOptions.length)];
            onSelect(event, selectedOption);

            setShow(false);
          } else if (filteredOptions.length === 0) {
            const selectedOption = toggleEL.current.value;
            onSelect(event, options[0], selectedOption);
          }
        }

        if (event.key === "Escape") {
          setShow(false);
          if (toggleEL.current) {
            toggleEL.current.blur();
          }
        }

        if (toggleEL.current) {
          toggleEL.current.value = "";
        }

        setEmphasized("");

        setCursor(0);
        setFilteredOptions(options);
      }
    };

    if (show) {
      document.body.addEventListener("keydown", handler);

      return () => {
        document.body.removeEventListener("keydown", handler);
      };
    }
  }, [show, cursor, options, filteredOptions, onSelect, toggleEL]);

  return (
    <Dropdown
      blur
      position="left"
      onToggle={isOpen => {
        onToggle(isOpen);
        setShow(isOpen);
      }}
      show={show}
      title={
        <input
          type="text"
          ref={toggleEL}
          onChange={e => {
            const value = e.target.value;
            setEmphasized(value);
            setFilteredOptions(
              options.filter(criterion =>
                criterion.toLowerCase().includes(value.toLowerCase())
              )
            );
          }}
          placeholder={placeholderLabel}
        />
      }
    >
      {filteredOptions.map((option, idx) => {
        return (
          <Dropdown.Option
            key={idx}
            className={
              idx === getIdx(cursor, filteredOptions.length) ? "active" : null
            }
            onClick={event => {
              setCursor(0);
              setFilteredOptions(options);
              setEmphasized("");
              onSelect(event, option);
            }}
          >
            <span
              dangerouslySetInnerHTML={{
                __html: emphasize(optionLabels[option] || option, emphasized)
              }}
            />
          </Dropdown.Option>
        );
      })}
    </Dropdown>
  );
}

export default React.forwardRef((props, ref) => (
  <Typeahead {...props} toggleEL={ref || useRef()} />
));
