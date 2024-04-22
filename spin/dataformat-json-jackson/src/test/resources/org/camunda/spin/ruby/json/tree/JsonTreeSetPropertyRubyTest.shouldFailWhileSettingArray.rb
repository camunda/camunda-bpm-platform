require 'date'
node = S($input, "application/json")
list = [Date.today]

node.prop("comment", list)