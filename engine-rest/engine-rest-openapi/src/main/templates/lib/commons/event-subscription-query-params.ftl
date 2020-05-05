  <@lib.parameter
      name = "eventSubscriptionId"
      location = "query"
      type = "string"
      desc = "Only select subscription with the given id." />

  <@lib.parameter
      name = "eventName"
      location = "query"
      type = "string"
      desc = "Only select subscriptions for events with the given name." />

  <@lib.parameter
      name = "eventType"
      location = "query"
      type = "string"
      enumValues = [ "message", "signal", "compensate", "conditional" ]
      desc = "Only select subscriptions for events with the given type.
              Valid values: `message`, `signal`, `compensate` and `conditional`." />

  <@lib.parameter
      name = "executionId"
      location = "query"
      type = "string"
      desc = "Only select subscriptions that belong to an execution with the given id." />

  <@lib.parameter
      name = "processInstanceId"
      location = "query"
      type = "string"
      desc = "Only select subscriptions that belong to a process instance with the given id." />

  <@lib.parameter
      name = "activityId"
      location = "query"
      type = "string"
      desc = "Only select subscriptions that belong to an activity with the given id." />

  <@lib.parameter
      name = "tenantIdIn"
      location = "query"
      type = "string"
      desc = "Filter by a comma-separated list of tenant ids.
              Only select subscriptions that belong to one of the given tenant ids." />

  <@lib.parameter
      name = "withoutTenantId"
      location = "query"
      type = "boolean"
      desc = "Only select subscriptions which have no tenant id.
              Value may only be `true`, as `false` is the default behavior." />

  <@lib.parameter
      name = "includeEventSubscriptionsWithoutTenantId"
      location = "query"
      type = "boolean"
      last = last
      desc = "Select event subscriptions which have no tenant id.
              Can be used in combination with tenantIdIn parameter.
              Value may only be `true`, as `false` is the default behavior." />