QUnit.config.autostart = false;
window.addEventListener('polymer-ready', function() {
    scheduleTests();
    QUnit.start();
});

function scheduleTests() {
    asyncTest('Start with first name', function() {
        var e = document.getElementById('first-only');
        // If running part1() right now, without the e.async(…),
        // the name hasn't propagated yet from .typedFirst to .prettyFirst
        // and the test fails. Firefox shows this every time.
        e.async(part1);

        function part1() {
            deepEqual(e.prettyFirst, 'James', 'First name');
            deepEqual(e.prettyLast, '', 'Last name empty');
            deepEqual(e.prettyFull, 'James', 'Full name');
            e.typedLast = '   T.\n \t   Kirk';
            e.async(part2);
        }

        function part2() {
            deepEqual(e.prettyFull, 'James T. Kirk', 'Full name property');
            start();
        }
    });

    asyncTest('Start with last name', function() {
        var e = document.getElementById('last-only');
        strictEqual(e.prettyFirst, '', 'First name empty');
        strictEqual(e.prettyLast, 'Simmons', 'Last name');
        strictEqual(e.prettyFull, 'Simmons', 'Full name');

        (function part1() {
            e.typedFirst = 'Kim   "Wild"  ';
            e.async(part2);
        })();

        function part2() {
            deepEqual(e.prettyFirst, 'Kim "Wild"', 'First name');
            deepEqual(e.prettyFull, 'Kim "Wild" Simmons',
                'Full name property');
            start();
        }
    });

    asyncTest('Start with both names', function() {
        var e = document.getElementById('both');
        strictEqual(e.prettyFirst, 'Mark', 'First name');
        strictEqual(e.prettyLast, 'Petrie', 'Last name');
        strictEqual(e.prettyFull, 'Mark Petrie', 'Full name');


        (function part1() {
            e.typedFirst = 'Art';
            e.typedLast = 'Tas  ';
            e.async(part2);
        })();

        function part2() {
            deepEqual(e.prettyFirst, 'Art', 'First name');
            deepEqual(e.prettyFull, 'Art Tas', 'Full name property');
            start();
        }
    });
}
