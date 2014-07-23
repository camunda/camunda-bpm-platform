require 'date'
node = JSON($input)
object = {
    "date" => Date.today
}

node.prop("comment", object)