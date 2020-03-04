{

  <@lib.endpointInfo
      id = "throwSignal"
      tag = "Signal"
      desc = "A signal is an event of global scope (broadcast semantics) and is delivered to all active handlers. 
              Internally this maps to the engine's signal event received builder method RuntimeService#createSignalEvent().
              For more information about the signal behavior, see the [Signal Events](${docsUrl}/reference/bpmn20/events/signal-events/)
              section of the [BPMN 2.0 Implementation Reference](${docsUrl}/reference/bpmn20/)." />

  "parameters" : [],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "SignalDto"
      examples = ['"examle-1": {
                     "summary": "POST /signal",
                     "description": "The content of the Request Body",
                     "value": {
                         "name": "policy_conditions_changed",
                          "variables": {
                            "newTimePeriodInMonth": {
                              "value": 24
                           }
                         }
                       }
                     }']/>

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful."/>

    <@lib.response
        code = "400"
        desc = "Returned if no name was given
                or if the variable value or type is invalid, for example if the value could not be parsed to an integer value or the passed variable type is not supported
                or if a tenant id and an execution id is specified."/>

    <@lib.response
        code = "403"
        desc = "Returned if the user is not allowed to throw a signal event."/>

    <@lib.response
        code = "500"
        last = true
        desc = "Returned if a single execution is specified and no such execution exists or has not subscribed to the signal."/>
  }
}