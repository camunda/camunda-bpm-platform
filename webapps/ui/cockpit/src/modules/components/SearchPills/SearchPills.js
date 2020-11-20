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

import React, { useCallback, useEffect, useRef, useState } from "react";
import { Tooltip, OverlayTrigger } from "react-bootstrap";
import search from "utils/search";
import translate from "utils/translation";
import classNames from "classnames";
import {
  Typeahead,
  InlineEdit,
  Clipboard,
  GlyphIcon,
  LinkButton
} from "components";

import "./SearchPills.scss";

const getSearch = url => new URLSearchParams(url.split("?")[1]);
const getHash = search =>
  window.location.hash.split("?")[0] + "?" + search.toString();

const isNumeric = str => {
  return !isNaN(str) && !isNaN(parseFloat(str));
};

const parseIfNumber = value => (isNumeric(value) ? +value : value);

const toCriteriaSelection = (acc, criterionSelected) => {
  const { type, operator, value, name } = criterionSelected;

  acc.push({
    operator: operator,
    value: value,
    name: type,
    varKey: name,
    valid: true
  });
  return acc;
};

const toLabels = criteria =>
  Object.entries(criteria).reduce((acc, [option, value]) => {
    acc[option] = value.label;
    return acc;
  }, {});

function SearchPills({
  criteria,
  labels,
  onChange = () => {},
  rowsCount = null
}) {
  const [criteriaSelection, setCriteriaSelection] = useState([]);

  const [criterionCursor, setCriterionCursor] = useState(-1);
  const [operatorCursor, setOperatorCursor] = useState(-1);
  const [valueCursor, setValueCursor] = useState(-1);
  const [varKeyCursor, setVarKeyCursor] = useState(-1);

  const addCriterionElRef = useRef();

  const criteriaLabels = React.useMemo(() => {
    return toLabels(criteria);
  }, [criteria]);

  const operatorLabels = React.useMemo(() => {
    return Object.entries(criteria).reduce(
      (acc, [criterion, { operators }]) => {
        acc[criterion] = Object.entries(operators).reduce(
          (acc, [key, { name, label }]) => {
            acc[name] = label;
            return acc;
          },
          {}
        );
        return acc;
      },
      {}
    );
  }, [criteria]);

  const triggerSearchChange = useCallback(
    criteriaSelection => {
      setCriteriaSelection(criteriaSelection);

      const toPayload = (acc, criterionSelected) => {
        const { name, valid, operator, value, varKey } = criterionSelected;

        if (valid) {
          const criterion = criteria[name];
          const isVariable = criterion.type === "variable";
          const { queryParam, type } = criterion.operators.filter(
            op => op.name === operator
          )[0];

          let queryParamName = null;
          if (isVariable && criterion.queryParam) {
            queryParamName = criterion.queryParam;
          } else if (!isVariable && queryParam) {
            queryParamName = queryParam;
          } else {
            queryParamName = name;
          }

          acc[queryParamName] = acc[queryParamName] || [];
          let queryValue =
            operator.toLowerCase() === "like"
              ? `%${value}%`
              : parseIfNumber(value);
          queryValue = type === "array" ? [queryValue] : queryValue;
          if (isVariable) {
            acc[queryParamName].push({
              name: varKey,
              operator: operator,
              value: queryValue
            });
          } else {
            acc[queryParamName] = queryValue;
          }
        }
        return acc;
      };

      let payload = criteriaSelection.reduce(toPayload, {});
      onChange(payload);

      const toSearch = (acc, criterionSelected) => {
        const { name, valid, operator, value, varKey } = criterionSelected;
        if (valid) {
          acc.push({
            type: name,
            name: varKey || "",
            operator: operator,
            value: value
          });
        }
        return acc;
      };

      let searchParams = criteriaSelection.reduce(toSearch, []);
      const search = getSearch(window.location.hash);
      search.set("searchQuery", JSON.stringify(searchParams));
      // Prevents triggering the "hashchange" event listener
      window.history.replaceState(null, "", getHash(search));
    },
    [criteria, onChange]
  );

  useEffect(() => {
    const searchQuery = JSON.parse(search.get("searchQuery") || "[]");
    const criteria = searchQuery.reduce(toCriteriaSelection, []);
    triggerSearchChange(criteria);

    const handleHashchange = event => {
      const newSearchQuery = getSearch(event.newURL).get("searchQuery");
      const oldSearchQuery = getSearch(event.oldURL).get("searchQuery");
      if (newSearchQuery !== oldSearchQuery) {
        const searchQuery = JSON.parse(newSearchQuery || "[]");
        const criteria = searchQuery.reduce(toCriteriaSelection, []);
        triggerSearchChange(criteria);
      }
    };

    window.addEventListener("hashchange", handleHashchange);

    return () => {
      window.removeEventListener("hashchange", handleHashchange);
    };
  }, [triggerSearchChange]);

  const addCriterion = (event, name, value) => {
    const criterion = criteria[name];
    const isValid = !!value;
    let newCriterion = {
      name: name,
      operator: criterion.operators[0].name,
      value: value,
      valid: isValid
    };

    const newCriteria = [...criteriaSelection, newCriterion];
    if (isValid) {
      triggerSearchChange(newCriteria);
    } else {
      setCriteriaSelection(newCriteria);
    }

    if (value) {
      addCriterionElRef.current.focus();
    } else {
      if (criterion.type === "variable") {
        setVarKeyCursor(newCriteria.length - 1);
      } else {
        setValueCursor(newCriteria.length - 1);
      }
    }
  };

  const changeCriterion = (event, idx, newCriterionName, oldCriterionName) => {
    const variableTyped = criteria[newCriterionName].type === "variable";
    if (newCriterionName !== oldCriterionName) {
      const { valid, value, varKey, ...rest } = criteriaSelection[idx];
      const isValid = variableTyped ? !!varKey && !!value : !!value;
      const newCriteria = [
        ...criteriaSelection.slice(0, idx),
        {
          ...rest,
          name: newCriterionName,
          operator: criteria[newCriterionName].operators[0].name,
          value: value,
          varKey: varKey,
          valid: isValid
        },
        ...criteriaSelection.slice(idx + 1)
      ];
      if (isValid || valid) {
        triggerSearchChange(newCriteria);
      } else {
        setCriteriaSelection(newCriteria);
      }
    }

    setCriterionCursor(-1);

    if (event.key === "Tab") {
      if (variableTyped) {
        setVarKeyCursor(idx);
      } else {
        if (criteria[newCriterionName].operators.length > 1) {
          setOperatorCursor(idx);
        } else {
          setValueCursor(idx);
        }
      }
    }
  };

  const changeOperator = (event, idx, newOperator, oldOperator) => {
    if (newOperator !== oldOperator) {
      const criterion = criteriaSelection[idx];
      const newCriteria = [
        ...criteriaSelection.slice(0, idx),
        {
          ...criterion,
          operator: newOperator
        },
        ...criteriaSelection.slice(idx + 1)
      ];
      if (criterion.valid) {
        triggerSearchChange(newCriteria);
      } else {
        setCriteriaSelection(newCriteria);
      }
    }

    setOperatorCursor(-1);
    if (event.key === "Tab") {
      setValueCursor(idx);
    }
  };

  const setVarKey = (event, idx, selectedCriterion, newVarKey, oldVarKey) => {
    if (newVarKey !== oldVarKey) {
      const { valid, value, ...rest } = selectedCriterion;
      const isValid = !!value && !!newVarKey;
      const newCriteria = [
        ...criteriaSelection.slice(0, idx),
        {
          ...rest,
          varKey: newVarKey,
          value: value,
          valid: isValid
        },
        ...criteriaSelection.slice(idx + 1)
      ];

      if (isValid || valid) {
        triggerSearchChange(newCriteria);
      } else {
        setCriteriaSelection(newCriteria);
      }
    }

    setVarKeyCursor(-1);

    if (event.key === "Tab") {
      event.preventDefault();
      setOperatorCursor(idx);
    }
  };

  const remove = (idx, selectedCriterion) => {
    const newCriteria = [
      ...criteriaSelection.slice(0, idx),
      ...criteriaSelection.slice(idx + 1)
    ];
    if (selectedCriterion.valid) {
      triggerSearchChange(newCriteria);
    } else {
      setCriteriaSelection(newCriteria);
    }
  };

  const setValue = (event, selectedCriterion, idx, newValue) => {
    if (newValue !== selectedCriterion.value) {
      const { varKey, name, ...rest } = selectedCriterion;
      const variableTyped = criteria[name].type === "variable";

      const isValid = variableTyped ? !!varKey && !!newValue : !!newValue;

      const newCriteria = [
        ...criteriaSelection.slice(0, idx),
        {
          ...rest,
          name: name,
          value: newValue,
          varKey: varKey,
          valid: isValid
        },
        ...criteriaSelection.slice(idx + 1)
      ];
      if (isValid || selectedCriterion.valid) {
        triggerSearchChange(newCriteria);
      } else {
        setCriteriaSelection(newCriteria);
      }
    }
    setValueCursor(-1);
    if (event.key === "Tab" && criteriaSelection.length - 1 > idx) {
      event.preventDefault();
      setCriterionCursor(idx + 1);
    }

    if (
      (event.key === "Enter" || event.key === "Tab") &&
      criteriaSelection.length - 1 === idx
    ) {
      if (event.key === "Tab") {
        event.preventDefault();
      }
      addCriterionElRef.current.focus();
    }
  };

  return (
    <>
      <div className="SearchPills">
        {criteriaSelection.map((selectedCriterion, idx) => (
          <span
            key={idx}
            className={classNames(
              "pill",
              selectedCriterion.valid ? null : "invalid"
            )}
          >
            <LinkButton onClick={() => remove(idx, selectedCriterion)}>
              <OverlayTrigger
                placement="top"
                overlay={<Tooltip id="tooltip">{labels.remove}</Tooltip>}
              >
                <GlyphIcon type="remove" />
              </OverlayTrigger>
            </LinkButton>
            {criterionCursor === idx && (
              <Typeahead
                options={Object.keys(criteria)}
                optionLabels={criteriaLabels}
                onSelect={(event, newCriterion) => {
                  changeCriterion(
                    event,
                    idx,
                    newCriterion,
                    selectedCriterion.name
                  );
                }}
                onToggle={open => !open && setCriterionCursor(-1)}
                value={criteriaLabels[selectedCriterion.name]}
              />
            )}
            {criterionCursor !== idx && (
              <LinkButton onClick={() => setCriterionCursor(idx)}>
                <OverlayTrigger
                  placement="top"
                  overlay={<Tooltip id="tooltip">{labels.name}</Tooltip>}
                >
                  <span>{criteriaLabels[selectedCriterion.name]}</span>
                </OverlayTrigger>
              </LinkButton>
            )}
            &nbsp;
            {criteria[selectedCriterion.name].type === "variable" && (
              <>
                <span> : </span>
                <InlineEdit
                  tooltip={labels.varKey}
                  value={selectedCriterion.varKey || ""}
                  edit={varKeyCursor === idx}
                  onEditStart={() => setVarKeyCursor(idx)}
                  onEditEnd={() => setVarKeyCursor(-1)}
                  onSet={(event, value) => {
                    setVarKey(
                      event,
                      idx,
                      selectedCriterion,
                      value,
                      selectedCriterion.varKey
                    );
                  }}
                />
              </>
            )}
            &nbsp;
            {operatorCursor === idx && (
              <Typeahead
                options={criteria[selectedCriterion.name].operators.map(
                  operator => operator.name
                )}
                optionLabels={operatorLabels[selectedCriterion.name]}
                placeholderLabel={labels.addCriteria}
                onSelect={(event, newOperator) => {
                  changeOperator(
                    event,
                    idx,
                    newOperator,
                    selectedCriterion.operator
                  );
                }}
                onToggle={open => !open && setOperatorCursor(-1)}
                value={
                  operatorLabels[selectedCriterion.name][
                    selectedCriterion.operator
                  ]
                }
              />
            )}
            {operatorCursor !== idx && (
              <>
                {criteria[selectedCriterion.name].operators.length > 1 && (
                  <LinkButton onClick={() => setOperatorCursor(idx)}>
                    <OverlayTrigger
                      placement="top"
                      overlay={
                        <Tooltip id="tooltip">{labels.operator}</Tooltip>
                      }
                    >
                      <span>
                        {
                          operatorLabels[selectedCriterion.name][
                            selectedCriterion.operator
                          ]
                        }
                      </span>
                    </OverlayTrigger>
                  </LinkButton>
                )}
                {criteria[selectedCriterion.name].operators.length === 1 && (
                  <OverlayTrigger
                    placement="top"
                    overlay={<Tooltip id="tooltip">{labels.operator}</Tooltip>}
                  >
                    <span>
                      {
                        operatorLabels[selectedCriterion.name][
                          selectedCriterion.operator
                        ]
                      }
                    </span>
                  </OverlayTrigger>
                )}
              </>
            )}
            &nbsp;
            <InlineEdit
              tooltip={labels.value}
              value={selectedCriterion.value || ""}
              edit={valueCursor === idx}
              onEditStart={() => setValueCursor(idx)}
              onEditEnd={() => setValueCursor(-1)}
              onSet={(event, newValue) =>
                setValue(event, selectedCriterion, idx, newValue)
              }
            />
          </span>
        ))}
        <Typeahead
          className="add-criterion"
          ref={addCriterionElRef}
          options={Object.keys(criteria)}
          optionLabels={criteriaLabels}
          placeholderLabel={labels.addCriteria}
          onSelect={addCriterion}
        />
        <div className="controls">
          <OverlayTrigger
            placement="top"
            overlay={<Tooltip id="tooltip">{labels.rowsCount}</Tooltip>}
          >
            <span>{rowsCount}</span>
          </OverlayTrigger>
          <Clipboard
            tooltip={translate("CAM_WIDGET_COPY_LINK")}
            initialIcon="link"
            text={window.location.href}
            fade={false}
          />
        </div>
      </div>
    </>
  );
}

export default SearchPills;
