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

'use strict';

var dateExpLangHelp =
  'E.g.: `${ now() }`, `${ dateTime() }` or `${ dateTime().plusWeeks(2) }`';
var userExpLangHelp = 'E.g.: `${ currentUser() }`';
var commaSeparatedExps =
  'List of values separated by comma or an expression which evaluates to a list. E.g.: `camunda-admin, accounting` or `${ currentUserGroups() }`';
var commaSeparatedValues =
  'List of values seperated by comma. E.g.: `keyC, keyA, keyB`';

// yyyy-MM-dd'T'HH:mm:ss
var dateRegex = /^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(|\.[0-9]{0,4})$/;
var expressionsRegex = /^[\s]*([#$]){/;
var numberRegex = /^-?[\d]+$/;

function isValid(regex, error, exprSupport) {
  return function(value) {
    if (exprSupport) {
      if (expressionsRegex.test(value)) {
        return {valid: true};
      }
    }
    if (regex.test(value)) {
      if (error === 'date') {
        var isValidDateValue = !isNaN(new Date(value).getTime());
        if (!isValidDateValue) {
          return {
            valid: false,
            error: 'dateValue'
          };
        }
      }
      return {valid: true};
    } else {
      return {
        valid: false,
        error: error || 'format'
      };
    }
  };
}

var criteria = [
  {
    group: 'Process Instance',
    options: [
      {
        name: 'processInstanceId',
        label: 'ID'
      },
      {
        name: 'processInstanceBusinessKey',
        label: 'Business Key',
        expressionSupport: true,
        help: userExpLangHelp
      },
      {
        name: 'processInstanceBusinessKeyLike',
        label: 'Business Key Like',
        expressionSupport: true,
        help: userExpLangHelp
      }
    ]
  },
  {
    group: 'Process definition',
    options: [
      {
        name: 'processDefinitionId',
        label: 'ID'
      },
      {
        name: 'processDefinitionKey',
        label: 'Key'
      },
      {
        name: 'processDefinitionKeyIn',
        label: 'Key In',
        help: commaSeparatedValues
      },
      {
        name: 'processDefinitionName',
        label: 'Name'
      },
      {
        name: 'processDefinitionNameLike',
        label: 'Name Like'
      }
    ]
  },
  {
    group: 'Case Instance',
    options: [
      {
        name: 'caseInstanceId',
        label: 'ID'
      },
      {
        name: 'caseInstanceBusinessKey',
        label: 'Business Key'
      },
      {
        name: 'caseInstanceBusinessKeyLike',
        label: 'Business Key Like'
      }
    ]
  },
  {
    group: 'Case definition',
    options: [
      {
        name: 'caseDefinitionId',
        label: 'ID'
      },
      {
        name: 'caseDefinitionKey',
        label: 'Key'
      },
      {
        name: 'caseDefinitionName',
        label: 'Name'
      },
      {
        name: 'caseDefinitionNameLike',
        label: 'Name Like'
      }
    ]
  },
  {
    group: 'Other',
    options: [
      {
        name: 'active',
        label: 'Active',
        bool: true
      },
      {
        name: 'activityInstanceIdIn',
        label: 'Activity Instance ID In',
        help: commaSeparatedValues
      },
      {
        name: 'executionId',
        label: 'Execution ID'
      }
    ]
  },
  {
    group: 'User / Group',
    options: [
      {
        name: 'assignee',
        label: 'Assignee',
        expressionSupport: true,
        help: userExpLangHelp
      },
      {
        name: 'assigneeIn',
        label: 'Assignee in',
        help: commaSeparatedValues
      },
      {
        name: 'assigneeLike',
        label: 'Assignee Like',
        expressionSupport: true,
        help: userExpLangHelp
      },
      {
        name: 'owner',
        label: 'Owner',
        expressionSupport: true,
        help: userExpLangHelp
      },
      {
        name: 'candidateGroup',
        label: 'Candidate Group',
        expressionSupport: true,
        includeAssignedTasksSupport: true
      },
      {
        name: 'candidateGroups',
        label: 'Candidate Groups',
        expressionSupport: true,
        help: commaSeparatedExps,
        includeAssignedTasksSupport: true
      },
      {
        name: 'candidateUser',
        label: 'Candidate User',
        expressionSupport: true,
        help: userExpLangHelp,
        includeAssignedTasksSupport: true
      },
      {
        name: 'involvedUser',
        label: 'Involved User',
        expressionSupport: true,
        help: userExpLangHelp
      },
      {
        name: 'assigned',
        label: 'Assigned',
        bool: true
      },
      {
        name: 'unassigned',
        label: 'Unassigned',
        bool: true
      },
      {
        name: 'delegationState',
        label: 'Delegation State'
      }
    ]
  },
  {
    group: 'Task',
    options: [
      {
        name: 'taskDefinitionKey',
        label: 'Definition Key'
      },
      {
        name: 'taskDefinitionKeyIn',
        label: 'Definition Key In',
        help: commaSeparatedValues
      },
      {
        name: 'taskDefinitionKeyLike',
        label: 'Definition Key Like'
      },
      {
        name: 'name',
        label: 'Name'
      },
      {
        name: 'nameLike',
        label: 'Name Like'
      },
      {
        name: 'description',
        label: 'Description'
      },
      {
        name: 'descriptionLike',
        label: 'Description Like'
      },
      {
        name: 'priority',
        label: 'Priority',
        validate: isValid(numberRegex, 'number')
      },
      {
        name: 'maxPriority',
        label: 'Priority Max',
        validate: isValid(numberRegex, 'number')
      },
      {
        name: 'minPriority',
        label: 'Priority Min',
        validate: isValid(numberRegex, 'number')
      },
      {
        name: 'tenantIdIn',
        label: 'Tenant ID In',
        help: commaSeparatedValues
      },
      {
        name: 'withoutTenantId',
        label: 'Without Tenant ID',
        bool: true
      },
      {
        name: 'withoutDueDate',
        label: 'Without Due Date',
        bool: true
      }
    ]
  },
  {
    group: 'Dates',
    validate: isValid(dateRegex, 'date', true),
    options: [
      {
        name: 'createdBefore',
        label: 'Created Before',
        expressionSupport: true,
        help: dateExpLangHelp
      },
      {
        name: 'createdAfter',
        label: 'Created After',
        expressionSupport: true,
        help: dateExpLangHelp
      },
      {
        name: 'dueBefore',
        label: 'Due Before',
        expressionSupport: true,
        help: dateExpLangHelp
      },
      {
        name: 'dueAfter',
        label: 'Due After',
        expressionSupport: true,
        help: dateExpLangHelp
      },
      {
        name: 'followUpAfter',
        label: 'Follow Up After',
        expressionSupport: true,
        help: dateExpLangHelp
      },
      {
        name: 'followUpBefore',
        label: 'Follow Up Before',
        expressionSupport: true,
        help: dateExpLangHelp
      },
      {
        name: 'followUpBeforeOrNotExistent',
        label: 'Follow Up Before or Not Existent',
        expressionSupport: true,
        help: dateExpLangHelp
      }
    ]
  }
];
module.exports = criteria.map(function(item) {
  item.name = item.group.toLowerCase().replace(/[^a-z0-9-]+/g, '-');
  return item;
});
