declare namespace example = "http://example/";
declare variable $myDocument external;

<book title="{$myDocument/books/book[1]/title/text()}">
	<xml/>
</book>