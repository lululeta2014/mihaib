QUnit.config.autostart = false;
window.addEventListener('polymer-ready', function() {
    scheduleTests();
    QUnit.start();
});

function scheduleTests() {
    var e = document.querySelector('full-name');

    asyncTest('Initially empty', function() {
        deepEqual(e.prettyFirst, '', 'First name empty');
        deepEqual(e.prettyLast, '', 'Last name empty');
        deepEqual(e.prettyFull, '', 'Full name empty');

        // http://www.polymer-project.org/docs/polymer/polymer.html#asyncmethod
        (function part1() {
            e.typedFirst = 'Alan';
            e.async(part2);
        })();

        function part2() {
            deepEqual(e.prettyFirst, 'Alan', 'First name');
            deepEqual(e.prettyFull, 'Alan', 'Full name property');

            e.typedLast = 'Pangborn';
            e.async(part3);
        }

        function part3() {
            deepEqual(e.prettyLast, 'Pangborn', 'Last name property');
            deepEqual(e.prettyFull, 'Alan Pangborn', 'Full name property');
            start();
        }
    });
}
