    <@lib.parameter
        name = "sortBy"
        location = "query"
        type = "string"
        enum = true
        enumValues = sortByValues
        description = "Sort the results lexicographically by a given criterion. Must be used in conjunction with the sortOrder parameter."/>

    <@lib.parameter
        name = "sortOrder"
        location = "query"
        type = "string"
        enum = true
        enumValues=['"asc"', '"desc"']
        last = last
        description = "Sort the results in a given order. Values may be asc for ascending order or desc for descending order. Must be used in conjunction with the sortBy parameter."/>