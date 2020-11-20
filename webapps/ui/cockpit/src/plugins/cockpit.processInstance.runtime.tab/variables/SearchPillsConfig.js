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

import translate from "utils/translation";

export const getSearchPillsConfig = () => {
  return {
    criteria: {
      variableName: {
        label: translate("PLUGIN_VIEW_VARIABLE_TYPE_VARIABLE_NAME"),
        operators: [
          {
            label: "=",
            name: "eq"
          },
          {
            label: translate("PLUGIN_VIEW_VARIABLE_OPERATOR_LIKE"),
            name: "Like",
            queryParam: "variableNameLike"
          }
        ]
      },
      activityInstanceIdIn: {
        label: translate("PLUGIN_VIEW_VARIABLE_ACTIVITY_INSTANCE_ID"),
        operators: [
          {
            label: "=",
            name: "eq",
            type: "array"
          }
        ]
      },
      variableValues: {
        label: translate("PLUGIN_VIEW_VARIABLE_VARIABLE_VALUE"),
        type: "variable",
        operators: [
          {
            name: "eq",
            label: "="
          },
          {
            name: "neq",
            label: "!="
          },
          {
            name: "gt",
            label: ">"
          },
          {
            name: "gteq",
            label: ">="
          },
          {
            name: "lt",
            label: "<"
          },
          {
            name: "lteq",
            label: "<="
          },
          {
            name: "like",
            label: translate("PLUGIN_VIEW_VARIABLE_OPERATOR_LIKE")
          }
        ]
      }
    },
    labels: {
      addCriteria: translate("PLUGIN_VIEW_VARIABLE_ADD_CRITERIA"),
      rowsCount: translate("CAM_WIDGET_SEARCH_TOTAL_NUMBER_RESULTS"),
      remove: translate("PLUGIN_VIEW_VARIABLE_DELETE_SEARCH"),
      name: translate("PLUGIN_VIEW_VARIABLE_TYPE"),
      operator: translate("PLUGIN_VIEW_VARIABLE_OPERATOR"),
      varKey: translate("PLUGIN_VIEW_VARIABLE_NAME"),
      value: translate("PLUGIN_VIEW_VARIABLE_VALUE")
    }
  };
};
