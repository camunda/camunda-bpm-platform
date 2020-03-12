  <@lib.parameter
      name = "firstResult"
      location = "query"
      type = "integer"
      desc = "Pagination of results. Specifies the index of the first result to return."/>

  <@lib.parameter
      name = "maxResults"
      location = "query"
      type = "integer"
      last = last
      desc = "Pagination of results. Specifies the maximum number of results to return.
              Will return less results if there are no more results left."/>
