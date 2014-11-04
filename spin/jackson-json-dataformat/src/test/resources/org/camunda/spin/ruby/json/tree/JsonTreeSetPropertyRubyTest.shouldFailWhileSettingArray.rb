require 'date'
node = JSON($input)
list = [Date.today]

node.prop("comment", list)