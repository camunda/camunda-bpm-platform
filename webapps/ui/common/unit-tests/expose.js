var $ = window.jQuery = window.$ = require('jquery');
var commons = require('camunda-commons-ui/lib');
var sdk = require('camunda-commons-ui/vendor/camunda-bpm-sdk-angular');
var dataDepend = require('angular-data-depend');
var angular = require('camunda-commons-ui/vendor/angular');

window.angular = angular;
window.jquery = $;
window['camunda-commons-ui'] = commons;
window['camunda-bpm-sdk-js'] = sdk;
window['angular-data-depend'] = dataDepend;
window['moment'] = require('camunda-commons-ui/vendor/moment');
window['events'] = require('events');
window['cam-common']= require('../scripts/module');
