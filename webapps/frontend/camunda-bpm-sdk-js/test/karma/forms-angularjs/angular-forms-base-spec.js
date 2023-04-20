/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* global jQuery: false */
'use strict';
var CamSDK = require('../../../lib/angularjs/index.js');
var angular = require('angular');

var request = require('superagent');
var mockConfig = require('../../superagent-mock-config');

function waitUntil(test, next, max) {
  max = max || 1000;
  next = typeof next === 'function' ? next : function() {};

  function timestamp() {
    return (new Date()).getTime();
  }

  function elapsed(from) {
    return timestamp() - from;
  }

  function repeat() {
    tested = test();
    var tooLong = elapsed(started) > max;
    if (!tested && !tooLong) {
      return setTimeout(repeat, 10);
    }
    next(tested ? null : new Error('Exceeded ' + max + 'ms timeout'));
  }

  var started = timestamp();
  var tested = test();

  repeat();
}






describe('The input field', function() {
  var $ = jQuery;
  var $simpleFormDoc;
  var camForm, camClient, procDef;

  var superagentMock;
  before(function() {
    superagentMock = require('superagent-mock')(request, mockConfig);
  });

  after(function() {
    superagentMock.unset();
  });

  it('prepares the testing environemnt', function(done) {
    jQuery.ajax('/base/test/karma/forms-angularjs/angular-form.html', {
      success: function(data) {
        $simpleFormDoc = jQuery('<div id="test-form" ng-controller="AppController">'+ data +'</div>');
        // the following lines allow to see the form in the browser
        var _$top = $(top.document);
        _$top.find('#test-form').remove();
        _$top.find('#browsers').after($simpleFormDoc);

        expect(typeof CamSDK).to.eql('object');

        done();
      },
      error: done
    });
  });


  it('needs a process definition', function(done) {
    camClient = new CamSDK.Client({
      apiUri: 'engine-rest/engine'
    });

    camClient.resource('process-definition').list({}, function(err, result) {
      if (err) { return done(err); }

      procDef = result.items.pop();

      expect(procDef.id).to.be.ok;

      done();
    });
  });


  it('should provide CamSDKFormsAngular', function() {
    expect(CamSDK.Form).to.not.be.undefined;
  });


  /**
   * ensures that angular integration works for a form
   * which is pre-rendered
   */
  it('should use pre-rendered form', function(done) {
    var scope;


    function whenRetrieved(err) {
      if (err) { return done(err); }

      camForm.variableManager.variable('stringVar').value = 'secondUpdate';

      camForm.applyVariables();

      waitUntil(function() {
        return scope.modelProperty === 'secondUpdate';
      }, done);
    }


    function ready(err) {
      if (err) { return done(err); }

      expect(scope).to.not.be.undefined;

      // change the value in the scope
      scope.$apply(function() {
        scope.modelProperty= 'updated';
      });

      // if we retrieve the variables from the form
      camForm.retrieveVariables();

      waitUntil(function() {
        // the variable updated using angular is updated in the variable manager
        return 'updated' === camForm.variableManager.variable('stringVar').value;
      }, whenRetrieved);
    }


    angular.module('testApp', ['cam.embedded.forms'])
      .controller('AppController', ['$scope', function($scope) {
        camForm = new CamSDK.Form({
          client: camClient,
          processDefinitionId: procDef.id,
          formElement: $simpleFormDoc.find('form[cam-form]'),
          done: function() {window.setTimeout(ready);}
        });

        scope = $scope;
      }]);

    angular.bootstrap($simpleFormDoc, ['testApp', 'cam.embedded.forms']);
  });

  /**
   * ensures that angular integration works if form is rendered
   * after the angular application has been bootstrapped.
   */
  it('should render form', function(done) {
    // initialize a new angular app (off screen):
    var appElement = $('<div ng-controller="AppController" />');
    var scope;


    function whenUpdated(err) {
      if (err) { return done(err); }

      camForm.variableManager.variable('stringVar').value = 'secondUpdate';

      camForm.variableManager.variable('autoBindVar').value = 'autoBindValue';

      camForm.applyVariables();

      waitUntil(function() {
        return scope.modelProperty === 'secondUpdate';
      }, function(err) {
        if (err) { return done(err); }

        waitUntil(function() {
          return scope.autoBindVar === 'autoBindValue';
        }, done);
      });
    }


    function ready(err) {
      if (err) { return done(err); }

      expect(scope).to.not.be.undefined;

      // change the value in the scope
      scope.$apply(function() {
        scope.modelProperty= 'updated';
      });

      // if we retrieve the variables from the form
      camForm.retrieveVariables();

      waitUntil(function() {
        // the variable updated using angular is updated in the variable manager
        return 'updated' === camForm.variableManager.variable('stringVar').value;
      }, whenUpdated);
    }


    angular.module('testApp', [])
      .controller('AppController', ['$scope', function($scope) {
        camForm = new CamSDK.Form({
          client: camClient,
          processDefinitionId: procDef.id,
          containerElement: appElement,
          formUrl: '/base/test/karma/forms-angularjs/angular-form.html',
          done: function() {window.setTimeout(ready);}
        });
        scope = $scope;

      }]);

    angular.bootstrap(appElement, ['testApp', 'cam.embedded.forms']);
  });



  it('should set form invalid', function(done) {
    // initialize a new angular app (off screen):
    var appElement = $('<div ng-controller="AppController" />');
    var scope;


    function ready(err) {
      if (err) { return done(err); }

      expect(scope).to.not.be.undefined;

      // change the value in the scope
      scope.$apply(function() {
        scope.integerProperty= 'abc';
      });

      waitUntil(function() {
        var $el = appElement.find('input[name="integerVar"]');
        return $el.hasClass('ng-invalid') &&
                $el.hasClass('ng-invalid-cam-variable-type') &&
                scope.form &&
                scope.form.$invalid &&
                scope.form.$error &&
                scope.form.$error.camVariableType;
      }, done);
    }


    angular.module('testApp', [])
      .controller('AppController', ['$scope', function($scope) {
        camForm = new CamSDK.Form({
          client: camClient,
          processDefinitionId: procDef.id,
          containerElement: appElement,
          formUrl: '/base/test/karma/forms-angularjs/angular-form.html',
          done: function() {window.setTimeout(ready);}
        });

        scope = $scope;
      }]);

    angular.bootstrap(appElement, ['testApp', 'cam.embedded.forms']);
  });
});
