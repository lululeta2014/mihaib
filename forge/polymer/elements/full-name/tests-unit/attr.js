QUnit.config.autostart = false;
window.addEventListener('polymer-ready', function() {
    scheduleTests();
    QUnit.start();
});

function scheduleTests() {
    asyncTest('Start with first name', function() {
        var e = document.getElementById('first-only');
        // If running part1() right now, without the e.async(…),
        // the name hasn't propagated yet from .firstTxt to ‘get first()’
        // and the test fails. Firefox shows this every time.
        e.async(part1);

        function part1() {
            deepEqual(e.first, 'James', 'First name');
            deepEqual(e.last, '', 'Last name empty');
            deepEqual(e.full, 'James', 'Full name');
            e.last = '   T.\n \t   Kirk';
            e.async(part2);
        }

        function part2() {
            deepEqual(e.full, 'James T. Kirk', 'Full name property');
            start();
        }
    });

    asyncTest('Start with last name', function() {
        var e = document.getElementById('last-only');
        strictEqual(e.first, '', 'First name empty');
        strictEqual(e.last, 'Simmons', 'Last name');
        strictEqual(e.full, 'Simmons', 'Full name');

        (function part1() {
            e.first = 'Kim   "Wild"  ';
            e.async(part2);
        })();

        function part2() {
            deepEqual(e.getAttribute('first'), 'Kim "Wild"', 'First name attr');
            deepEqual(e.full, 'Kim "Wild" Simmons', 'Full name property');
            start();
        }
    });

    asyncTest('Start with both names', function() {
        var e = document.getElementById('both');
        strictEqual(e.first, 'Mark', 'First name');
        strictEqual(e.last, 'Petrie', 'Last name');
        strictEqual(e.full, 'Mark Petrie', 'Full name');


        (function part1() {
            e.first = 'Art';
            e.last = 'Tas  ';
            e.async(part2);
        })();

        function part2() {
            deepEqual(e.getAttribute('first'), 'Art', 'First name attr');
            deepEqual(e.full, 'Art Tas', 'Full name property');
            start();
        }
    });
}
