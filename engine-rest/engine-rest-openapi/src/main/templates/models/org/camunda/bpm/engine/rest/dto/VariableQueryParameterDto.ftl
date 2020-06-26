<@lib.dto>

    <@lib.property
        name = "name"
        type = "string"
        desc = "Variable name"/>

    <@lib.property
        name = "operator"
        type = "string"
        enumValues = ['"eq"', '"neq"', '"gt"', '"gteq"', '"lt"', '"lteq"', '"like"']
        desc = "Comparison operator to be used"/>

    <@lib.property
        name = "value"
        type = "object"
        last = true
        desc = "The variable value, could be of type boolean, string or number"/>

</@lib.dto>
