require 'date'
node = S($input, "application/json")
object = {
    "date" => Date.today
}

node.prop("comment", object)