'use strict';

var chai = require('chai'),
    angular = require('../../../../camunda-bpm-sdk-js/vendor/angular'),
    ngRoute = require('angular-route'),
    authModule = require('../index');

var expect = chai.expect;

function AuthenticatedController($scope, authentication) {
  this.authentication = authentication;
  this.called = true;
}

function DefaultController($scope) {
  this.called = true;
}


var testModule = angular.module('cam.commons.auth.test', [ angular.module('ngRoute').name, authModule.name ]);

testModule.config(function($routeProvider) {

  $routeProvider.when('/required-authentication', {
    authentication: 'required',
    controller: AuthenticatedController,
    controllerAs: 'controller',
    template: '<div>{{controller.authentication.name}}</div>'
  });

  $routeProvider.when('/optional-authentication', {
    authentication: 'optional',
    controller: AuthenticatedController,
    controllerAs: 'controller',
    template: '<div>{{controller.authentication.name}}</div>'
  });

  $routeProvider.when('/no-authentication', {
    controller: AuthenticatedController,
    controllerAs: 'controller',
    template: '<div>no-authentication</div>'
  });

  $routeProvider.when('/no-authentication-authenticated-controller', {
    controller: DefaultController,
    controllerAs: 'controller'
  });
});

authModule.config(function($qProvider) {
  $qProvider.errorOnUnhandledRejections(false);
});

describe('camunda-commons-ui/auth', function() {


  describe('AuthenticationService', function() {

    beforeEach(window.module(authModule.name));

    beforeEach(window.module(function($provide) {
      $provide.value('shouldDisplayAuthenticationError', function() {
        return true;
      });
    }));

    afterEach(inject(function($httpBackend) {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    }));


    describe('#login', function() {

      it('should POST to auth backend with username/password', inject(function($httpBackend, AuthenticationService) {

          // given
        $httpBackend
            .expect('POST', 'admin://auth/user/:engine/login/:appName', 'username=testUser&password=testPass')
            .respond({ userId: 'testUser', authorizedApps: [ 'a', 'b' ] });

        $httpBackend
          .expect('GET', 'engine://engine/')
            .respond('');


          // when
        AuthenticationService.login('testUser', 'testPass');
        $httpBackend.flush();

          // then
        $httpBackend.verifyNoOutstandingExpectation();
      }));


      it('should successfully login', inject(function($httpBackend, $rootScope, AuthenticationService) {

          // given
        var successListener = chai.spy();
        var authenticationChangedListener = chai.spy();

        $rootScope.$on('authentication.changed', authenticationChangedListener);
        $rootScope.$on('authentication.login.success', successListener);

        $httpBackend
            .when('POST', 'admin://auth/user/:engine/login/:appName', 'username=testUser&password=testPass')
            .respond({ userId: 'testUser', authorizedApps: [ 'a', 'b' ] });

        $httpBackend
          .expect('GET', 'engine://engine/')
            .respond('')

        var loginSuccess = chai.spy(),
            loginFailure = chai.spy();

          // when
        AuthenticationService.login('testUser', 'testPass').then(loginSuccess, loginFailure);
        $httpBackend.flush();

          // then
        expect(loginSuccess).to.have.been.called();
        expect(loginFailure).to.not.have.been.called();

        expect(authenticationChangedListener).to.have.been.called();
        expect(successListener).to.have.been.called();

        expect($rootScope.authentication.name).to.equal('testUser');
      }));


      it('should fail login', inject(function($httpBackend, $rootScope, AuthenticationService) {

          // given
        var failureListener = chai.spy();
        $rootScope.$on('authentication.login.failure', failureListener);

        $httpBackend
            .when('POST', 'admin://auth/user/:engine/login/:appName')
            .respond(401);

        var loginSuccess = chai.spy(),
            loginFailure = chai.spy();

          // when
        AuthenticationService.login('testUser', 'testPass').then(loginSuccess, loginFailure);
        $httpBackend.flush();

          // then
        expect(loginSuccess).to.not.have.been.called();
        expect(loginFailure).to.have.been.called();

        expect(failureListener).to.have.been.called();

        expect($rootScope.authentication).to.eql(undefined);
      }));

    });


    describe('#logout', function() {

      beforeEach(inject(function($httpBackend) {
        $httpBackend
            .when('POST', 'admin://auth/user/:engine/login/:appName')
            .respond({ userId: 'testUser' });

        $httpBackend
          .expect('GET', 'engine://engine/')
          .respond('');
      }));


      it('should POST auth backend', inject(function($httpBackend, AuthenticationService) {

          // given
        AuthenticationService.login('testUser', 'testPass');
        $httpBackend.flush();

        $httpBackend
            .expect('POST', 'admin://auth/user/:engine/logout')
            .respond(200);

          // when
        AuthenticationService.logout();
        $httpBackend.flush();

          // then
        $httpBackend.verifyNoOutstandingExpectation();
      }));


      it('should successfully logout', inject(function($httpBackend, $rootScope, AuthenticationService) {

          // given
        var successListener = chai.spy();
        var authenticationChangedListener = chai.spy();

        $rootScope.$on('authentication.changed', authenticationChangedListener);
        $rootScope.$on('authentication.logout.success', successListener);

        AuthenticationService.login('testUser', 'testPass');
        $httpBackend.flush();

        $httpBackend
            .when('POST', 'admin://auth/user/:engine/logout')
            .respond(200);

        var logoutSuccess = chai.spy(),
            logoutFailure = chai.spy();

          // when
        AuthenticationService.logout().then(logoutSuccess, logoutFailure);
        $httpBackend.flush();

          // then
        expect(logoutSuccess).to.have.been.called();
        expect(logoutFailure).to.not.have.been.called();

        expect(successListener).to.have.been.called();
        expect(authenticationChangedListener).to.have.been.called();

        expect($rootScope.authentication).to.eql(null);
      }));


      it('should fail logout', inject(function($httpBackend, $rootScope, AuthenticationService) {

          // given
        var failureListener = chai.spy();
        $rootScope.$on('authentication.logout.failure', failureListener);

        AuthenticationService.login('testUser', 'testPass');
        $httpBackend.flush();

        $httpBackend
            .when('POST', 'admin://auth/user/:engine/logout')
            .respond(500);

        var logoutSuccess = chai.spy(),
            logoutFailure = chai.spy();

          // when
        AuthenticationService.logout().then(logoutSuccess, logoutFailure);
        $httpBackend.flush();

          // then
        expect(logoutSuccess).to.not.have.been.called();
        expect(logoutFailure).to.have.been.called();

        expect(failureListener).to.have.been.called();

          // no logout
        expect($rootScope.authentication.name).to.equal('testUser');
      }));

    });


    describe('#getAuthentication', function() {


      it('should successfully retrieve', inject(function($httpBackend, $rootScope, AuthenticationService) {

          // given
        var authenticationChangedListener = chai.spy();
        $rootScope.$on('authentication.changed', authenticationChangedListener);

        $httpBackend
            .expect('GET', 'admin://auth/user/:engine')
            .respond(200, { userId: 'testUser', authorizedApps: [] });

        var getSuccess = chai.spy(),
            getFailure = chai.spy();

          // when
        AuthenticationService.getAuthentication().then(getSuccess, getFailure);
        $httpBackend.flush();

          // then
        expect(getSuccess).to.have.been.called();
        expect(getFailure).to.not.have.been.called();

        expect(authenticationChangedListener).to.have.been.called();

      }));


      it('should fail retrieve', inject(function($httpBackend, $rootScope, AuthenticationService) {

          // given
        var authenticationChangedListener = chai.spy();
        $rootScope.$on('authentication.changed', authenticationChangedListener);

        $httpBackend
            .expect('GET', 'admin://auth/user/:engine')
            .respond(404);

        var getSuccess = chai.spy(),
            getFailure = chai.spy();

          // when
        AuthenticationService.getAuthentication().then(getSuccess, getFailure);
        $httpBackend.flush();

          // then
        expect(getSuccess).to.not.have.been.called();
        expect(getFailure).to.have.been.called();

        expect(authenticationChangedListener).to.not.have.been.called();

      }));


      it('should preload', inject(function($rootScope, AuthenticationService) {

          // given
        $rootScope.authentication = { name: 'klaus' };

        var authenticationChangedListener = chai.spy();
        $rootScope.$on('authentication.changed', authenticationChangedListener);

        var getSuccess = chai.spy(),
            getFailure = chai.spy();

          // when
        AuthenticationService.getAuthentication().then(getSuccess, getFailure);
        $rootScope.$digest();

          // then
        expect(getSuccess).to.have.been.called();
        expect(getFailure).to.not.have.been.called();

        expect(authenticationChangedListener).to.not.have.been.called();
      }));

    });

  });


  describe('route authentication', function() {

    beforeEach(window.module(testModule.name));

    beforeEach(window.module(function($provide) {
      $provide.value('shouldDisplayAuthenticationError', function() {
        return true;
      });
    }));

    afterEach(inject(function($httpBackend) {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    }));


    describe('query authenticated user', function() {

      it('should query authenticated user (required)', inject(function($route, $location, $rootScope, $httpBackend) {

          // given
        $httpBackend
            .expect('GET', 'admin://auth/user/:engine')
            .respond(200, { userId: 'testUser', authorizedApps: [ 'a', 'b' ] });

        $location.path('/required-authentication');

          // when
        $httpBackend.flush();

          // then
        expect($rootScope.authentication.name).to.equal('testUser');
        expect($rootScope.authentication.authorizedApps).to.eql([ 'a', 'b' ]);
      }));


      it('should query authenticated user (optional)', inject(function($route, $location, $rootScope, $httpBackend) {

          // given
        $httpBackend
            .expect('GET', 'admin://auth/user/:engine')
            .respond(404);

        $location.path('/optional-authentication');

          // when
        $httpBackend.flush();

          // then
        expect($rootScope.authentication).to.eql(undefined);
      }));


      it('should cache authenticated user', inject(function($route, $location, $rootScope, $httpBackend) {

          // given
        $httpBackend
            .expect('GET', 'admin://auth/user/:engine')
            .respond(200, { userId: 'testUser', authorizedApps: [ 'a', 'b' ] });

          // when
        $location.path('/required-authentication');
        $httpBackend.flush();

        $location.path('/optional-authentication');
        $rootScope.$digest();

          // then
        expect($rootScope.authentication.name).to.equal('testUser');
        expect($rootScope.authentication.authorizedApps).to.eql([ 'a', 'b' ]);
      }));

    });


    describe('access restriction', function() {

      beforeEach(inject(function($httpBackend) {
        $httpBackend
            .when('GET', 'admin://auth/user/:engine')
            .respond(404);
      }));


      it('should redirect to /login', inject(function($route, $location, $rootScope, $httpBackend) {

          // given
        var listener = chai.spy();
        $rootScope.$on('authentication.login.required', listener);

          // when
        $location.path('/required-authentication');
        $httpBackend.flush();

          // then
        expect(listener).to.have.been.called();
        expect($location.url()).to.eql('/login');
      }));


      it('should silence redirect to /login', inject(function($route, $location, $rootScope, $httpBackend) {

          // given
        $rootScope.$on('authentication.login.required', function(event) {
          event.preventDefault();
        });

          // when
        $location.path('/required-authentication');
        $httpBackend.flush();

          // then
        expect($location.url()).to.eql('/required-authentication');
      }));
    });


    describe('post-login', function() {

      beforeEach(inject(function($httpBackend) {
        $httpBackend
            .when('GET', 'admin://auth/user/:engine')
            .respond(404);

        $httpBackend
            .when('POST', 'admin://auth/user/:engine/login/:appName')
            .respond({ userId: 'testUser', authorizedApps: [ 'a', 'b' ] });
      }));


      it('should redirect after successful login', inject(function($route, $location, $rootScope, $httpBackend, AuthenticationService) {

          // given
        $location.path('/required-authentication');
        $httpBackend.flush();

        $httpBackend
          .expect('GET', 'engine://engine/')
          .respond('');

          // when
        AuthenticationService.login('testUser', '');
        $httpBackend.flush();

          // then
        expect($location.url()).to.eql('/required-authentication');
      }));

    });


    describe('post-logout', function() {

      beforeEach(inject(function($httpBackend) {
        $httpBackend
            .when('POST', 'admin://auth/user/:engine/logout')
            .respond(200);

        $httpBackend
            .when('POST', 'admin://auth/user/:engine/login/:appName')
            .respond({ userId: 'testUser', authorizedApps: [ 'a', 'b' ] });
      }));


      it('should require re-authentication after logout', inject(function($route, $location, $rootScope, $httpBackend, AuthenticationService) {

          // given
        $httpBackend
          .expect('GET', 'engine://engine/')
          .respond('');

        AuthenticationService.login('testUser', '');
        $httpBackend.flush();

        AuthenticationService.logout();
        $httpBackend.flush();

          // assume
        expect($location.url()).to.eql('/login');
        expect($rootScope.authentication).to.eql(null);

          // when
        $location.path('/required-authentication');
        $rootScope.$digest();

          // then
        expect($location.url()).to.eql('/login');
      }));

    });

  });


  describe('ng-view integration', function() {

    beforeEach(window.module(testModule.name));

    beforeEach(window.module(function($provide) {
      $provide.value('shouldDisplayAuthenticationError', function() {
        return true;
      });
    }));

    var doc, element;

    beforeEach(inject(function($httpBackend, $rootScope, $compile) {
      doc = $('<div><div ng-view></div></div>').appendTo('body');
      element = $compile(doc.find('[ng-view]'))($rootScope);
    }));

    afterEach(function() {
      doc.remove();
    });


    it('should render injected authentication', inject(function($rootScope, $location) {

        // given
      $rootScope.authentication = { name: 'testUser' };

        // when
      $location.path('/required-authentication');
      $rootScope.$digest();

      expect($('[ng-view]').text()).to.equal('testUser');
    }));


    it('should render injected authentication (optional)', inject(function($rootScope, $location, $httpBackend) {

        // given
      $httpBackend
          .expect('GET', 'admin://auth/user/:engine')
          .respond(404);

        // when
      $location.path('/optional-authentication');
      $httpBackend.flush();

      expect($('[ng-view]').text()).to.equal('');
    }));

  });

});
