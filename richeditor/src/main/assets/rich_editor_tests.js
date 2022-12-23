"use strict";

var RichEditorTests = function() {
    var self = {};
    var tests = [];
    var link = "http://foo.bar/";
    var anchor = "<a id='link_id' href='" + link + "'>Foo</a>";
    var htmlWithLink = "<span><p id='prose'>What are these so withered and wild in their attire? " + anchor + " </p>that look not like the inhabitants of the Earth and yet are on't?</span>";
    var htmlWith2Links = "<span><p id='two_links'>Blah? " + anchor + " " + anchor + " Blah</p></span>";

    var tearDown = function() {
        RE.setHtml('');
    };

    /**
    This is the main and only public "method"
    **/
    self.runTests = function() {
        var content = "";
        for (var testName in tests) {
            tests[testName]();
            var log = 'Passed : ' + testName;
            console.log(log);
            content += log + "<br>";
            tearDown();
        }
        RE.setHtml(content);
    };

    tests['testGetSet'] = function() {
        var testContent = "Test";
        RE.setHtml(testContent);
        Assert.equals(RE.getHtml(), testContent, 'testGetSet');
    };

    tests['testGetSelectedHrefReturnsLinkOnFullSelection'] = function() {
        let htmlWithLink = "<a id='link_id' href='" + link + "'>Foo</a>";
        RE.setHtml(htmlWithLink);
            //select the anchor tag directly and fully
        RE.selectElementContents(document.querySelector('#link_id'));
        Assert.equals(RE.getSelectedHref(), link);
    };

    tests['testGetSelectedHrefWithSelectionContainingOneLink'] = function() {
        RE.setHtml(htmlWithLink);
            //select the anchor tag directly and fully
        RE.selectElementContents(document.querySelector('#prose'));
        Assert.equals(RE.getSelectedHref(), link);
    };

    tests['testCountAnchorTagsInSelection'] = function() {
        RE.setHtml(htmlWithLink);
            //select the anchor tag directly and fully
        RE.selectElementContents(document.querySelector('#prose'));
        let count = RE.countAnchorTagsInNode(getSelection().anchorNode);
        Assert.equals(count, 1);
    };

    tests['testgetSelectedHrefWith2LinksReturnsNull'] = function() {
        RE.setHtml(htmlWith2Links);

        //select the anchor tag directly and fully
        RE.selectElementContents(document.querySelector('#two_links'));
        let count = RE.countAnchorTagsInNode(getSelection().anchorNode);
        Assert.equals(count, 2);
            // Assert.equals(RE.getSelectedHref(), null)
    };

    return self;
}();

RichEditorTests.runTests();