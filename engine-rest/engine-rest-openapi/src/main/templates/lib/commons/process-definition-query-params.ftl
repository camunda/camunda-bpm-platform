  <@lib.parameter name = "processDefinitionId"
      location = "query"
      type = "string"
      desc = "Filter by process definition id."/>

  <@lib.parameter name = "processDefinitionIdIn"
      location = "query"
      type = "string"
      desc = "Filter by a comma-separated list of process definition ids."/>

  <@lib.parameter name = "name"
      location = "query"
      type = "string"
      desc = "Filter by process definition name."/>

  <@lib.parameter name = "nameLike"
      location = "query"
      type = "string"
      desc = "Filter by process definition names that the parameter is a substring of."/>

  <@lib.parameter name = "deploymentId"
      location = "query"
      type = "string"
      desc = "Filter by the deployment the id belongs to."/>

  <@lib.parameter name = "deployedAfter"
      location = "query"
      type = "string"
      format="date-time"
      desc = "Filter by the deploy time of the deployment the process definition belongs to.
              Only selects process definitions that have been deployed after (exclusive) a specific time.
              By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the
              format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g.,
              `2013-01-23T14:42:45.546+0200`."/>

  <@lib.parameter name = "deployedAt"
      location = "query"
      type = "string"
      format="date-time"
      desc = "Filter by the deploy time of the deployment the process definition belongs to.
              Only selects process definitions that have been deployed at a specific time (exact match).
              By [default](${docsUrl}/reference/rest/overview/date-format/), the date must have the
              format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, e.g.,
              `2013-01-23T14:42:45.546+0200`."/>

  <@lib.parameter name = "key"
      location = "query"
      type = "string"
      desc = "Filter by process definition key, i.e., the id in the BPMN 2.0 XML. Exact match."/>

  <@lib.parameter name = "keysIn"
      location = "query"
      type = "string"
      desc = "Filter by a comma-separated list of process definition keys."/>

  <@lib.parameter name = "keyLike"
      location = "query"
      type = "string"
      desc = "Filter by process definition keys that the parameter is a substring of."/>

  <@lib.parameter name = "category"
      location = "query"
      type = "string"
      desc = "Filter by process definition category. Exact match."/>

  <@lib.parameter name = "categoryLike"
      location = "query"
      type = "string"
      desc = "Filter by process definition categories that the parameter is a substring of."/>

  <@lib.parameter name = "version"
      location = "query"
      type = "integer"
      format="int32"
      desc = "Filter by process definition version."/>

  <@lib.parameter name = "latestVersion"
      location = "query"
      type = "boolean"
      desc = "Only include those process definitions that are latest versions.
              Value may only be `true`, as `false` is the default behavior."/>

  <@lib.parameter name = "resourceName"
      location = "query"
      type = "string"
      desc = "Filter by the name of the process definition resource. Exact match."/>

  <@lib.parameter name = "resourceNameLike"
      location = "query"
      type = "string"
      desc = "Filter by names of those process definition resources that the parameter is a substring of."/>

  <@lib.parameter name = "startableBy"
      location = "query"
      type = "string"
      desc = "Filter by a user name who is allowed to start the process."/>

  <@lib.parameter name = "active"
      location = "query"
      type = "boolean"
      desc = "Only include active process definitions.
              Value may only be `true`, as `false` is the default behavior."/>

  <@lib.parameter name = "suspended"
      location = "query"
      type = "boolean"
      desc = "Only include suspended process definitions.
              Value may only be `true`, as `false` is the default behavior."/>

  <@lib.parameter name = "incidentId"
      location = "query"
      type = "string"
      desc = "Filter by the incident id."/>

  <@lib.parameter name = "incidentType"
      location = "query"
      type = "string"
      desc = "Filter by the incident type.
              See the [User Guide](${docsUrl}/user-guide/process-engine/incidents/#incident-types)
              for a list of incident types."/>

  <@lib.parameter name = "incidentMessage"
      location = "query"
      type = "string"
      desc = "Filter by the incident message. Exact match."/>

  <@lib.parameter name = "incidentMessageLike"
      location = "query"
      type = "string"
      desc = "Filter by the incident message that the parameter is a substring of."/>

  <@lib.parameter name = "tenantIdIn"
      location = "query"
      type = "string"
      desc = "Filter by a comma-separated list of tenant ids.
              A process definition must have one of the given tenant ids."/>

  <@lib.parameter name = "withoutTenantId"
      location = "query"
      type = "boolean"
      desc = "Only include process definitions which belong to no tenant.
              Value may only be true, as false is the default behavior." />

  <@lib.parameter name = "includeProcessDefinitionsWithoutTenantId"
      location = "query"
      type = "boolean"
      desc = "Include process definitions which belong to no tenant. Can be used in combination with `tenantIdIn`.
              Value may only be `true`, as `false` is the default behavior."/>

  <@lib.parameter name = "versionTag"
      location = "query"
      type = "string"
      desc = "Filter by the version tag."/>

  <@lib.parameter name = "versionTagLike"
      location = "query"
      type = "string"
      desc = "Filter by the version tag that the parameter is a substring of."/>

  <@lib.parameter name = "withoutVersionTag"
      location = "query"
      type = "boolean"
      desc = "Only include process definitions without a `versionTag`."/>

  <@lib.parameter name = "startableInTasklist"
      location = "query"
      type = "boolean"
      desc = "Filter by process definitions which are startable in Tasklist.."/>

  <@lib.parameter name = "notStartableInTasklist"
      location = "query"
      type = "boolean"
      desc = "Filter by process definitions which are not startable in Tasklist."/>

  <@lib.parameter name = "startablePermissionCheck"
      location = "query"
      type = "boolean"
      last = last
      desc = "Filter by process definitions which the user is allowed to start in Tasklist.
              If the user doesn't have these permissions the result will be empty list.
              The permissions are:
              * `CREATE` permission for all Process instances
              * `CREATE_INSTANCE` and `READ` permission on Process definition level"/>
