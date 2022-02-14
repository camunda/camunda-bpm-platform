<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/group/post-create/index.html -->
{
  <@lib.endpointInfo
      id = "createGroup"
      tag = "Group"
      summary = "Create Group"
      desc = "Creates a new group."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "GroupDto"
      examples = ['"example-1": {
                     "summary": "POST `/group/create`",
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
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        desc = "Identity service is read-only (Cannot modify users / groups / memberships)."
    />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "The group could not be created due to an internal server error. See the
               [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the
               error response format."
        last = true
    />

  }
}
</#macro>