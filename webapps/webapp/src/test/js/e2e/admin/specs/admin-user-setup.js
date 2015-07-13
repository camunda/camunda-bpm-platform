'use strict';

var fs = require('fs'),
    factory = require('../../setup-factory.js'),
    combine = factory.combine,
    operation = factory.operation;

module.exports = {

  setup1:
    combine(
      operation('user', 'delete', [{
        id: 'admin'
      }])

)};
