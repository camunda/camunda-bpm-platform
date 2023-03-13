# Vendor

The scripts located in this directory are dependencies of the
[camunda-commons-ui](http://github.com/camunda/camunda-commons-ui)
and were downloaded manually.


## Open Sans fonts

Source: http://www.fontsquirrel.com/fonts/open-sans

Note: The webfonts were generated from
http://www.fontsquirrel.com/tools/webfont-generator

License: [Apache 2.0](http://www.fontsquirrel.com/license/open-sans)


## jQuery UI

Version: 1.10.4
Source: http://github.com/jquery/jquery-ui

- jquery.ui.core.js
- jquery.ui.draggable.js
- jquery.ui.mouse.js
- jquery.ui.widget.js

License: [MIT](https://raw.githubusercontent.com/jquery/jquery-ui/master/LICENSE.txt)



## AngularJS Bootstrap

Version: 0.11.2
Source: Custom build from [forked ui-bootstrap](//github.com/camunda-third-party/bootstrap). To rebuild:
```sh
git clone https://github.com/camunda-third-party/bootstrap.git angular-ui-bootstrap
cd angular-ui-bootstrap
npm install
grunt build 
# copy 
# '<angular-ui-bootstrap directory>/dist/ui-bootstrap-tpls.0.11.2.js'
# to
# '<camunda-commons-ui directory>/vendor/ui-bootstrap-tpls.0.11.2-camunda.js'
```

- ui-bootstrap-tpls.0.11.2-camunda.js

License: [MIT](https://github.com/angular-ui/bootstrap/blob/master/LICENSE)



## domReady

Version: 2.0.1
Source: http://cdnjs.cloudflare.com/ajax/libs/require-domReady/2.0.1/domReady.js

- domReady.js

License: [MIT or BSD](https://raw.githubusercontent.com/requirejs/domReady/master/LICENSE)



## Placeholders.js

Version: 3.0.2
Source: https://github.com/jamesallardice/Placeholders.js

- placeholders.main.js
- placeholders.utils.js
- placeholders.jquery.js

License: MIT



## bpmn-js

Version: 0.5.1
Source: https://raw.githubusercontent.com/bpmn-io/bower-bpmn-js/v0.5.1/bpmn-viewer.js

- bpmn-js.js

License: https://raw.githubusercontent.com/bpmn-io/bower-bpmn-js/v0.5.1/LICENSE



## Snap.svg

Version: 0.3.0
Source: https://raw.githubusercontent.com/adobe-webplatform/Snap.svg/v0.3.0/dist/snap.svg.js

- snap.svg.js

License: [Apache](http://www.apache.org/licenses/LICENSE-2.0)
