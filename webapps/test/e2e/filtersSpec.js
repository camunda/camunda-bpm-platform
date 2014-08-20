'use strict';
/* jshint node:true */
ddescribe('Filters', function() {
  describe('list', function() {
    it('is accessible', function() {
      browser.get('/');
      expect(element(by.css('[cam-tasklist-filters]')).isDisplayed()).toBe(true);
    });


    describe('add button', function() {
      var newFilterLink = element(by.css('.controls .task-filters a'));
      var newFilterForm = element(by.css('.modal-content form[name="newFilter"]'));

      it('can be found in the controls row', function() {
        expect(newFilterLink.isDisplayed()).toBe(true);
      });


      it('opens a modal dialog when clicked', function() {
        newFilterLink
        .click().then(function() {
          expect(newFilterForm.isDisplayed()).toBe(true);
        });
      });


      describe('the "new filter" modal dialog', function() {
        it('has a "name" field', function() {
          expect(element(by.css('form[name="newFilter"] [id$="-name"]')).isDisplayed()).toBe(true);
        });


        it('has a "description" field', function() {
          expect(element(by.css('form[name="newFilter"] [id$="-description"]')).isDisplayed()).toBe(true);
        });


        it('has a "color" field', function() {
          expect(element(by.css('form[name="newFilter"] [id$="-color"]')).isDisplayed()).toBe(true);
        });


        describe('filter fields', function() {

        });
      });
    });


    describe('item', function() {
      it('has a summary of its filters', function() {

      });


      it('lists tasks when clicked', function() {

      });
    });
  });
});
