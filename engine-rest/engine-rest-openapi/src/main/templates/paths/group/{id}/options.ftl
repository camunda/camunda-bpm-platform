<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/group/options/index.html -->
{
  <@lib.endpointInfo
      id = "availableGroupInstanceOperations"
      tag = "Group"
      summary = "Group Resource Instance Options"
      desc = "The `/group` resource supports two custom OPTIONS requests, one for the resource as such and this one for individual group instances.
              The OPTIONS request allows checking for the set of available operations that the currently authenticated user can perform on the
              `/group/{id}` resource instance. If the user can perform an operation or not may depend on various things, including the users authorizations
              to interact with this resource and the internal configuration of the process engine."
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

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ResourceOptionsDto"
        desc = "Request successful."
        examples = ['"example-1": {
                          "summary": "Status 200.",
                          "description": "OPTIONS `/group/aGroupId`",
                          "value": {
                            "links": [
                              {
                                "method": "GET",
                                "href": "http://localhost:8080/engine-rest/group/aGroupId",
                                "rel":"self"
                              },
                              {
                                "method": "DELETE",
                                "href":"http://localhost:8080/engine-rest/group/aGroupId",
                                "rel":"delete"
                              },
                              {
                                "method": "PUT",
                                "href":"http://localhost:8080/engine-rest/group/aGroupId",
                                "rel":"update"
                              }
                            ]
                          }
                        }']
        last = true
    />

  }

}

</#macro>