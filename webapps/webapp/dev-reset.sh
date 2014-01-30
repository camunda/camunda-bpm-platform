#! /bin/bash
rm -rvf node
rm -rvf node_modules
rm -rvf target
rm -rvf doc
rm -rvf src/main/webapp/assets/vendor

mvn clean jetty:run -Pdevelop
