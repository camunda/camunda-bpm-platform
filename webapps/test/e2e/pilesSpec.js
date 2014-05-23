'use strict';
/* jshint node:true */
ddescribe('Piles', function() {
  describe('list', function() {
    it('is accessible', function() {
      browser.get('/');
      expect(element(by.css('[cam-tasklist-piles]')).isDisplayed()).toBe(true);
    });


    describe('add button', function() {
      var newPileLink = element(by.css('.controls .task-piles a'));
      var newPileForm = element(by.css('.modal-content form[name="newPile"]'));

      it('can be found in the controls row', function() {
        expect(newPileLink.isDisplayed()).toBe(true);
      });


      it('opens a modal dialog when clicked', function() {
        newPileLink
        .click().then(function() {
          expect(newPileForm.isDisplayed()).toBe(true);
        });
      });


      describe('the "new pile" modal dialog', function() {
        it('has a "name" field', function() {
          expect(element(by.css('form[name="newPile"] [id$="-name"]')).isDisplayed()).toBe(true);
        });


        it('has a "description" field', function() {
          expect(element(by.css('form[name="newPile"] [id$="-description"]')).isDisplayed()).toBe(true);
        });


        it('has a "color" field', function() {
          expect(element(by.css('form[name="newPile"] [id$="-color"]')).isDisplayed()).toBe(true);
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
