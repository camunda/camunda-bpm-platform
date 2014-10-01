define(function() {
  'use strict';

  var dateExpLangHelp = 'E.g.: ${ now() }, ${ timeDate() } or ${ timeDate().plusWeeks(2) })';
  var userExpLangHelp = 'E.g.: ${ currentUser() }';
  var groupExpLangHelp = 'E.g.: ${ currentUserGroups() }';

  // check that the date format follows `yyyy-MM-dd'T'HH:mm:ss` or is an expression language string
  var expressionsExp = /^[\s]*(\#|\$)\{/;
  var dateExp = /^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(|\.[0-9]{0,4})(|Z)$/;
  function dateValidate(value) {
    if (expressionsExp.test(value)) {
      return false;
    }

    return !dateExp.test(value) ? 'INVALID_DATE' : false;
  }


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
          help: userExpLangHelp
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
          help: groupExpLangHelp
        },
        {
          name: 'candidateGroups',
          label: 'Candidate Groups',
          expressionSupport: true,
          help: groupExpLangHelp
        },
        {
          name: 'candidateUser',
          label: 'Candidate User',
          expressionSupport: true,
          help: userExpLangHelp
        },
        {
          name: 'involvedUser',
          label: 'Involved User',
          expressionSupport: true,
          help: userExpLangHelp
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
          help: dateExpLangHelp,
          validate: dateValidate
        },
        {
          name: 'createdAfter',
          label: 'Created After',
          expressionSupport: true,
          help: dateExpLangHelp,
          validate: dateValidate
        },
        {
          name: 'dueBefore',
          label: 'Due Before',
          expressionSupport: true,
          help: dateExpLangHelp,
          validate: dateValidate
        },
        {
          name: 'dueAfter',
          label: 'Due After',
          expressionSupport: true,
          help: dateExpLangHelp,
          validate: dateValidate
        },
        {
          name: 'followUpAfter',
          label: 'Follow Up After',
          expressionSupport: true,
          help: dateExpLangHelp,
          validate: dateValidate
        },
        {
          name: 'followUpBefore',
          label: 'Follow Up Before',
          expressionSupport: true,
          help: dateExpLangHelp,
          validate: dateValidate
        }
        {
          name: 'followUpBeforeOrNotExistent',
          label: 'Follow Up Before or Not Existent',
          expressionSupport: true,
          help: dateExpLangHelp,
          validate: dateValidate
        }
      ]
    }
  ];
  return criteria;
});
