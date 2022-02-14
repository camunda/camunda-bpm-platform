<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/group/put-update/index.html -->
{
  <@lib.endpointInfo
      id = "updateGroup"
      tag = "Group"
      summary = "Update Group"
      desc = "Updates a given group by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the group."
          last = true
      />
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "GroupDto"
      examples = ['"example-1": {
                     "summary": "PUT `/group/sales`",
                     "value": {
                       "id": "sales",
                       "name": "Sales",
                       "type": "Organizational Unit"
                     }
                   }']
  />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful. No content."
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "Identity service is read-only (Cannot modify users / groups / memberships)."
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "If the group with the requested Id cannot be found."
    />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "The group could not be updated due to an internal server error.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error
                response format."
        last = true
    />

  }

}
</#macro>