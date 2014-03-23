QUnit.config.autostart = false;
window.addEventListener('polymer-ready', function() {
    scheduleTests();
    QUnit.start();
});

function scheduleTests() {
    var e = document.querySelector('full-name');

    asyncTest('Initially empty', function() {
        deepEqual(e.first, '', 'First name empty');
        deepEqual(e.last, '', 'Last name empty');
        deepEqual(e.full, '', 'Full name empty');
        // the ‘full’ attribute is null now, so not testing this here

        // http://www.polymer-project.org/docs/polymer/polymer.html#asyncmethod
        (function part1() {
            e.first = 'Alan';
            e.async(part2);
        })();

        function part2() {
            deepEqual(e.getAttribute('first'), 'Alan', 'First name attr');
            deepEqual(e.full, 'Alan', 'Full name property');
            deepEqual(e.getAttribute('full'), 'Alan', 'Full name attr');

            e.setAttribute('last', 'Pangborn');
            e.async(part3);
        }

        function part3() {
            deepEqual(e.last, 'Pangborn', 'Last name property');
            deepEqual(e.full, 'Alan Pangborn', 'Full name property');
            deepEqual(e.getAttribute('full'), 'Alan Pangborn',
                    'Full name attr');
            start();
        }
    });
}
