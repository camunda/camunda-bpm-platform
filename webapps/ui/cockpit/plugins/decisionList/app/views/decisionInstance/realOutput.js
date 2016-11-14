  'use strict';

  module.exports = [ 'ViewsProvider', function(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.decisionInstance.table', {
      id: 'realOutput',
      initialize: function(data) {
        var viewer = data.tableControl.getViewer().table;

        viewer.get('eventBus').on('cell.render', function(event) {

          var row = event.data.row;
          var ruleIndex = data.decisionInstance.outputs.map(function(output) {
            return output.ruleId;
          }).indexOf(row.id);

          var column = event.data.column;
          var columnIndex = data.decisionInstance.outputs.map(function(output) {
            return output.clauseId;
          }).indexOf(column.id);

          if(ruleIndex !== -1 && columnIndex !== -1) {

            var output = data.decisionInstance.outputs.filter(function(output) {
              return output.ruleId === row.id && output.clauseId === column.id;
            })[0];

            var realOutput = document.createElement('span');
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
            event.gfx.lastChild.appendChild(realOutput);
          }
        });
      }
    });
  }];
