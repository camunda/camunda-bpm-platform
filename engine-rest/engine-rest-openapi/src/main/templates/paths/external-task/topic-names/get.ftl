<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getTopicNames"
      tag = "External Task"
      summary = "Get External Task Topic Names"
      desc = "Queries for distinct topic names of external tasks that fulfill given parameters.
              Query can be restricted to only tasks with retries left, tasks that are locked, or tasks
              that are unlocked. The parameters withLockedTasks and withUnlockedTasks are
              exclusive. Setting them both to true will return an empty list.
              Providing no parameters will return a list of all distinct topic names with external tasks."
      />

  "parameters" : [

   <@lib.parameter
         name = "withLockedTasks"
         location = "query"
         type = "boolean"
         desc = "Only include external tasks that are currently locked (i.e., they have a lock time and it has not expired).
                 Value may only be `true`, as `false` matches any external task." />

     <@lib.parameter
         name = "withUnlockedTasks"
         location = "query"
         type = "boolean"
         desc = "Only include external tasks that are currently not locked (i.e., they have no lock or it has expired).
                 Value may only be `true`, as `false` matches any external task." />

     <@lib.parameter
         name = "withRetriesLeft"
         location = "query"
         type = "boolean"
         desc = "Only include external tasks that have a positive (&gt; 0) number of retries (or `null`). Value may only be
                 `true`, as `false` matches any external task."
         last = true />

  ],

  "responses" : {

     <@lib.response
          code = "200"
          mediaType = "application/json"
          flatType="string"
          array = true
          desc = "Request successful."
          examples = ['"example-1": {
                                 "summary": "GET /external-task/topic-names?withLockedTasks",
                                 "value": [
                                            "topic-a",
                                            "topic-b",
                                            "topic-c"
                                          ]
                               }']  />

    <@lib.response
        code = "400"
        last = true
        desc = "Returned if some of the query parameters are invalid." />
  }
}
</#macro>