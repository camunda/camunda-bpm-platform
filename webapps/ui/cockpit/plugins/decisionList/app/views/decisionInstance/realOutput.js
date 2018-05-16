  'use strict';

  var angular = require('camunda-commons-ui/vendor/angular');  

  module.exports = [ 'ViewsProvider', function(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.decisionInstance.table', {
      id: 'realOutput',
      initialize: function(data) {
        var outputCell, selector, realOutput;

        data.decisionInstance.outputs.map(function(output) {
          selector = '.output-cell[data-col-id='+output.clauseId+'][data-row-id='+output.ruleId+']';
          outputCell = angular.element(selector)[0];
          realOutput = document.createElement('span');
          if(output.type !== 'Object' &&
          output.type !== 'Bytes' &&
          output.type !== 'File') {

            realOutput.className = 'dmn-output';
            realOutput.textContent = ' = ' + output.value;

          } else {
            realOutput.className = 'dmn-output-object';
            realOutput.setAttribute('title', 'Variable value of type ' + output.type + ' is not shown');
            realOutput.textContent = ' = [' + output.type + ']';
          }
          outputCell.appendChild(realOutput);
        });
      }
    });
  }];
