<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/group/options/index.html -->
{
  <@lib.endpointInfo
      id = "availableGroupOperations"
      tag = "Group"
      summary = "Group Resource Options"
      desc = "The `/group` resource supports two custom OPTIONS requests, this one for the resource as such and one for
              individual group instances. The OPTIONS request allows checking for the set of available operations that
              the currently authenticated user can perform on the `/group` resource. If the user can perform an operation
              or not may depend on various things, including the users authorizations to interact with this resource and
              the internal configuration of the process engine."
  />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ResourceOptionsDto"
        desc = "Request successful."
        examples = ['"example-1": {
                          "summary": "Status 200.",
                          "description": "OPTIONS `/group`",
                          "value": {
                            "links": [
                              {
                                "method": "GET",
                                "href": "http://localhost:8080/engine-rest/group",
                                "rel": "list"
                              },
                              {
                                "method": "GET",
                                "href": "http://localhost:8080/engine-rest/group/count",
                                "rel": "count"
                              },
                              {
                                "method": "POST",
                                "href": "http://localhost:8080/engine-rest/group/create",
                                "rel": "create"
                              }
                            ]
                          }
                        }']
        last = true
    />

  }

}

</#macro>