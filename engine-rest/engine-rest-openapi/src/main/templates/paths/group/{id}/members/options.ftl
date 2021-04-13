<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "availableGroupMembersOperations"
      tag = "Group"
      summary = "Group Membership Resource Options"
      desc = "The OPTIONS request allows checking for the set of available operations that the currently authenticated
              user can perform on the resource. If the user can perform an operation or not may depend on various
              things, including the users authorizations to interact with this resource and the internal configuration
              of the process engine." />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          last = true
          desc = "The id of the group." />
  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ResourceOptionsDto"
        last = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "OPTIONS `/group/sales/members`",
                       "value": {"links":[
                                    {"method":"DELETE","href":"http://localhost:8080/engine-rest/group/sales/members","rel":"delete"},
                                    {"method":"PUT","href":"http://localhost:8080/engine-rest/group/sales/members","rel":"create"}]
                                }
                      }'] />
    }
}

</#macro>