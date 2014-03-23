QUnit.config.autostart = false;
window.addEventListener('polymer-ready', function() {
    scheduleTests();
    QUnit.start();
});

function scheduleTests() {
    asyncTest('Start with first name', function() {
        var e = document.getElementById('first-only');
        deepEqual(e.first, 'James', 'First name');
        deepEqual(e.last, '', 'Last name empty');
        deepEqual(e.full, 'James', 'Full name');

        (function part1() {
            e.last = 'Z';
            e.async(part2);
        })();

        function part2() {
            deepEqual(e.full, 'James Z', 'Full name property');
            deepEqual(e.getAttribute('full'), 'James Z', 'Full name attr');
            start();
        }
    });

    asyncTest('Start with last name', function() {
        var e = document.getElementById('last-only');
        strictEqual(e.first, '', 'First name empty');
        strictEqual(e.last, 'Simmons', 'Last name');
        strictEqual(e.full, 'Simmons', 'Full name');

        (function part1() {
            e.first = 'Kim';
            e.async(part2);
        })();

        function part2() {
            deepEqual(e.getAttribute('first'), 'Kim', 'First name attr');
            deepEqual(e.getAttribute('full'), 'Kim Simmons', 'Full name attr');
            deepEqual(e.full, 'Kim Simmons', 'Full name property');
            start();
        }
    });

    asyncTest('Start with both names', function() {
        var e = document.getElementById('both');
        strictEqual(e.first, 'Mark', 'First name');
        strictEqual(e.last, 'Petrie', 'Last name');
        strictEqual(e.full, 'Mark Petrie', 'Full name');


        (function part1() {
            e.first = 'Minna';
            e.last = 'Tas';
            e.async(part2);
        })();

        function part2() {
            deepEqual(e.getAttribute('first'), 'Minna', 'First name attr');
            deepEqual(e.getAttribute('full'), 'Minna Tas', 'Full name attr');
            deepEqual(e.full, 'Minna Tas', 'Full name property');
            start();
        }
    });
}
