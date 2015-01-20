package org.camunda.spin.groovy.xml.dom
def map = [
  a:"http://camunda.com"
]

query = S(input).xPath(expression).ns(map)
