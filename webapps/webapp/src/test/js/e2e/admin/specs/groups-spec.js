'use strict';

var testHelper = require('../../test-helper');
var setupFile = require('./groups-setup');

var groupsPage = require('../pages/groups');
var usersPage = require('../pages/users');

describe('Admin Groups Spec', function() {


	describe('group page navigation', function() {

		before(function(done) {
			testHelper(setupFile, done);
			groupsPage.navigateToWebapp('Admin');
      groupsPage.authentication.userLogin('admin', 'admin');
		});

		after(function() {
			groupsPage.logout();
		});

		beforeEach(function() {
			groupsPage.navigateTo();
		});


		it('should select group by name', function(done) {

			// when
			groupsPage.groupName(0).getText().then(function(name) {
				groupsPage.selectGroupByNameLink(0);

				// then
				expect(groupsPage.editGroup.pageHeader()).to.eventually.eql(name);
			});

		});


		it('should select group by edit link', function (done) {

			// when
			groupsPage.groupType(1).getText().then(function(type) {
				groupsPage.selectGroupByEditLink(1);

				// then
				expect(groupsPage.editGroup.groupTypeInput().getAttribute('value')).to.eventually.eql(type);
			})
		});

	});


	describe('create new group', function() {

		before(function(done) {
			testHelper(setupFile, done);
			groupsPage.navigateToWebapp('Admin');
      groupsPage.authentication.userLogin('admin', 'admin');
		});

		after(function() {
			groupsPage.logout();
		});


		it('should navigate to new group menu', function() {

			// given
			groupsPage.navigateTo();

      // when
      groupsPage.newGroupButton().click();
      groupsPage.newGroup.isActive();

      // then
      expect(groupsPage.newGroup.pageHeader()).to.eventually.eql('Create New Group');
    });


    it('should create new group', function() {

      // when
      groupsPage.newGroup.createNewGroup('4711', 'blaw', 'Marketing');

      // then
      expect(groupsPage.groupList().count()).to.eventually.eql(5);
      expect(groupsPage.groupId(0).getText()).to.eventually.eql('4711');
      expect(groupsPage.groupName(0).getText()).to.eventually.eql('blaw');
      expect(groupsPage.groupType(0).getText()).to.eventually.eql('Marketing');
    });

	});


	describe('update/delete group', function() {

		before(function(done) {
			testHelper(setupFile, done);
			groupsPage.navigateToWebapp('Admin');
      groupsPage.authentication.userLogin('admin', 'admin');
		});

		after(function() {
			groupsPage.logout();
		});


		it('should navigate to edit group menu', function (done) {

      // given
      groupsPage.navigateTo();


			//expect(groupsPage.groupId(1).getText()).to.eventually.eql('camunda-admin');

      // when
      groupsPage.selectGroupByNameLink(1);

      // then
      expect(groupsPage.editGroup.pageHeader()).to.eventually.eql('camunda BPM Administrators');
      groupsPage.editGroup.isActive({ group: 'camunda-admin' });
      expect(groupsPage.editGroup.updateGroupButton().isEnabled()).to.eventually.eql(false);
    });


    it('should edit group', function() {

      // when
      groupsPage.editGroup.groupNameInput('i');
      groupsPage.editGroup.updateGroupButton().click();

      // then
      expect(groupsPage.editGroup.pageHeader()).to.eventually.eql('camunda BPM Administratorsi');
    });


    it('should delete group', function() {

      // when
      groupsPage.editGroup.deleteGroupButton().click();
      groupsPage.editGroup.deleteGroupAlert().accept();

      // then
      expect(groupsPage.groupList().count()).to.eventually.eql(3);
    });
	});


/*	describe('assign user to group', function() {

		before(function(done) {
			testHelper(setupFile, done);
			groupsPage.navigateToWebapp('Admin');
      groupsPage.authentication.userLogin('admin', 'admin');
		});

		after(function() {
			groupsPage.logout();
		});


	});*/

});