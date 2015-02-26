'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./authorizations-setup');

var authorizationsPage = require('../pages/authorizations');
var cockpitPage = require('../../cockpit/pages/dashboard');

describe('Admin authorizations Spec', function() {

  describe('authorizations page navigation', function() {

    before(function() {
      return testHelper(setupFile, function() {

        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');
      });
    });


    it('should navigate to authorizations page', function() {

      // when
      authorizationsPage.selectNavbarItem('Authorizations');

      // then
      expect(authorizationsPage.pageHeader()).to.eventually.eql('Authorizations');
    });


    it('should navigate to group membership sub page', function(done) {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Group Membership');

      // then
      authorizationsPage.groupMembership.isActive();
      expect(authorizationsPage.groupMembership.createNewButton().isEnabled()).to.eventually.be.true;
      expect(authorizationsPage.groupMembership.boxHeader()).to.eventually.eql('Group Membership Authorizations');
    });


    it('should open new authorization input', function(done) {

      // give
      expect(authorizationsPage.groupMembership.createNewElement().isDisplayed()).to.eventually.be.false;

      // when
      authorizationsPage.groupMembership.createNewButton().click();

      // then
      expect(authorizationsPage.groupMembership.resourceId().getAttribute('value')).to.eventually.eql('*');
      expect(authorizationsPage.groupMembership.createNewElement().isDisplayed()).to.eventually.be.true;
    });


    it('should validate authorization type', function(done) {

      // when
      authorizationsPage.groupMembership.authorizationType('GLOBAL')

      // then
      expect(authorizationsPage.application.identityIdInputFiled().isEnabled()).to.eventually.be.false;

      // when
      authorizationsPage.groupMembership.authorizationType('DENY')

      // then
      expect(authorizationsPage.application.identityIdInputFiled().isEnabled()).to.eventually.be.true;

      // when
      authorizationsPage.groupMembership.authorizationType('ALLOW')

      // then
      expect(authorizationsPage.application.identityIdInputFiled().isEnabled()).to.eventually.be.true;
    });


    it('should abort new authorization input', function(done) {

      // when
      authorizationsPage.groupMembership.abortNewAuthorizationButton().click();

      // then
      expect(authorizationsPage.groupMembership.createNewElement().isDisplayed()).to.eventually.be.false;
    });

  });


  describe('authorizations sub pages', function() {

    before(function() {
      return testHelper(setupFile, function() {

        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');
      });
    });

    beforeEach(function() {
      authorizationsPage.navigateToWebapp('Admin');
      authorizationsPage.selectNavbarItem('Authorizations');
    });


    it('should validate application page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Application');

      // then
      authorizationsPage.application.isActive();
      expect(authorizationsPage.application.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.application.boxHeader()).to.eventually.eql('Application Authorizations');
    });


    it('should validate authorization page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Authorization');

      // then
      authorizationsPage.authorization.isActive();
      expect(authorizationsPage.authorization.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.authorization.boxHeader()).to.eventually.eql('Authorization Authorizations');
    });


    it('should validate filter page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Filter');

      // then
      authorizationsPage.filter.isActive();
      expect(authorizationsPage.filter.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.filter.boxHeader()).to.eventually.eql('Filter Authorizations');
    });


    it('should validate group page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Group');

      // then
      authorizationsPage.group.isActive();
      expect(authorizationsPage.group.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.group.boxHeader()).to.eventually.eql('Group Authorizations');
    });


    it('should validate group membership page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Group Membership');

      // then
      authorizationsPage.groupMembership.isActive();
      expect(authorizationsPage.groupMembership.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.groupMembership.boxHeader()).to.eventually.eql('Group Membership Authorizations');
    });


    it('should validate user page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('User');

      // then
      authorizationsPage.user.isActive();
      expect(authorizationsPage.user.createNewButton().isEnabled()).to.eventually.eql(true);
      expect(authorizationsPage.user.boxHeader()).to.eventually.eql('User Authorizations');
    });

  });


  xdescribe('create application authorization', function() {

    before(function() {
      return testHelper(setupFile, function() {

        authorizationsPage.navigateToWebapp('Admin');
        authorizationsPage.authentication.userLogin('admin', 'admin');

        authorizationsPage.selectNavbarItem('Authorizations');
      });
    });


    it('should navigate to application sub page', function() {

      // when
      authorizationsPage.selectAuthorizationNavbarItem('Application');

      // then
      authorizationsPage.application.isActive();
      expect(authorizationsPage.application.createNewButton().isEnabled()).to.eventually.be.true;
      expect(authorizationsPage.application.boxHeader()).to.eventually.eql('Application Authorizations');
    });


    it('should create new application authorization', function() {

      // when
      authorizationsPage.application.createNewButton().click();
      authorizationsPage.application.identityIdInputFiled('john');
      authorizationsPage.application.resourceId().clear();
      authorizationsPage.application.resourceId('Cockpit');
      authorizationsPage.application.submitNewAuthorizationButton().click();

      // then
      authorizationsPage.logout();
      cockpitPage.navigateToWebapp('Cockpit');
      cockpitPage.authentication.userLogin('john', 'MobyDick');
      cockpitPage.isActive();
    });

  });

});
