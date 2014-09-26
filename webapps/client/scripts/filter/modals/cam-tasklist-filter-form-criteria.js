define([], function() {
  'use strict';
  var criteria = [
    {
      group: 'Process Instance',
      options: [
        {
          name: 'processInstanceId',
          label: 'Id'
        },
        {
          name: 'processInstanceBusinessKey',
          label: 'Business Key'
        },
        {
          name: 'processInstanceBusinessKeyLike',
          label: 'Business Key Like'
        // },
        // {
        //   name: 'processVariables',
        //   label: 'Variables'
        }
      ]
    },
    {
      group: 'Process definition',
      options: [
        {
          name: 'processDefinitionId',
          label: 'Id'
        },
        {
          name: 'processDefinitionKey',
          label: 'Key'
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
      group: 'Other',
      options: [
        {
          name: 'active',
          label: 'Active'
        },
        {
          name: 'activityInstanceIdIn',
          label: 'Activity Instance Id In'
        },
        {
          name: 'executionId',
          label: 'Execution Id'
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
          help: 'E.g.: ${ currentUser() }'
        },
        {
          name: 'assigneeLike',
          label: 'Assignee Like',
          expressionSupport: true,
          help: 'E.g.: ${ currentUser() }'
        },
        {
          name: 'owner',
          label: 'Owner',
          expressionSupport: true,
          help: 'E.g.: ${ currentUser() }'
        },
        {
          name: 'candidateGroup',
          label: 'Candidate Group',
          expressionSupport: true,
          help: 'E.g.: '
        },
        {
          name: 'candidateGroups',
          label: 'Candidate Groups',
          expressionSupport: true,
          help: 'E.g.: ${ currentUserGroups() }'
        },
        {
          name: 'candidateUser',
          label: 'Candidate User',
          expressionSupport: true,
          help: 'E.g.: ${ currentUser() }'
        },
        {
          name: 'involvedUser',
          label: 'Involved User',
          expressionSupport: true,
          help: 'E.g.: ${ currentUser() }'
        },
        {
          name: 'unassigned',
          label: 'Unassigned'
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
          label: 'Priority'
        },
        {
          name: 'maxPriority',
          label: 'Priority Max'
        },
        {
          name: 'minPriority',
          label: 'Priority Min'
        // },
        // {
        //   name: 'taskVariables',
        //   label: 'Variables'
        }
      ]
    },
    {
      group: 'Dates',
      options: [
        {
          name: 'createdBefore',
          label: 'Created Before',
          expressionSupport: true,
          help: 'E.g.: ${ now() }, ${ timeDate() } or ${ timeDate().plusWeeks(2) })'
        },
        {
          name: 'createdAfter',
          label: 'Created After',
          expressionSupport: true,
          help: 'E.g.: ${ now() }, ${ timeDate() } or ${ timeDate().plusWeeks(2) })'
        },
        {
          name: 'dueBefore',
          label: 'Due Before',
          expressionSupport: true,
          help: 'E.g.: ${ now() }, ${ timeDate() } or ${ timeDate().plusWeeks(2) })'
        },
        {
          name: 'dueAfter',
          label: 'Due After',
          expressionSupport: true,
          help: 'E.g.: ${ now() }, ${ timeDate() } or ${ timeDate().plusWeeks(2) })'
        },
        {
          name: 'followUpAfter',
          label: 'Follow Up After',
          expressionSupport: true,
          help: 'E.g.: ${ now() }, ${ timeDate() } or ${ timeDate().plusWeeks(2) })'
        },
        {
          name: 'followUpBefore',
          label: 'Follow Up Before',
          expressionSupport: true,
          help: 'E.g.: ${ now() }, ${ timeDate() } or ${ timeDate().plusWeeks(2) })'
        }
      ]
    }
  ];
  return criteria;
});
