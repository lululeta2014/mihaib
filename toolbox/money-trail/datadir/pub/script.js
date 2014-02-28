function pagerGoto(n) {
    window.location.href = '?page=' + encodeURIComponent(n);
}

// 'elem' is the <tr> element or any descendent of it
function getTrAncestor(elem) {
    while (elem != null && elem.tagName != "TR")
        elem = elem.parentElement;
    if (elem == null)
        alert('Can\'t find parent <tr> element');
    return elem
}

function getErrSpan(tr) {
    var arr = tr.getElementsByTagName('span');
    for (var i = 0; i < arr.length; i++)
        if (arr[i].classList.contains('errSpan'))
            return arr[i];
    alert('Can\'t find <span> element for error messages');
    return null;
}

function asyncReqAndRunIfSuccess(url, errSpan, f) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
        if (req.readyState == 4) {
            if (req.status == 200) {
                // delete any previous error
                errSpan.textContent = '';
                f(req.responseText);
            } else {
                errSpan.textContent = req.responseText;
            }
        }
    }
    req.open('GET', url, true);
    req.send();
}

function deleteDbRow(elem) {
    var tr = getTrAncestor(elem);
    if (tr == null)
        return;
    var errSpan = getErrSpan(tr);
    if (errSpan == null)
        return;

    var cssCls = "rowToDelete";
    tr.classList.add(cssCls);
    if (confirm("Delete this row?")) {
        var url ="deleterow/?dbRowId=" +
            encodeURIComponent(tr.getAttribute('dbRowId'));
        asyncReqAndRunIfSuccess(url, errSpan,
                function(responseText) { window.location.reload(); }
                );
    } else {
        tr.classList.remove(cssCls);
    }
}

// Selects fields used by 'new row' and 'edit row' for user input
var inpElemSelectors = "input, select";

function selectFirstInpElem(parentElem) {
    var elems = parentElem.querySelectorAll(inpElemSelectors);
    if (elems.length > 0)
        elems[0].focus();
}

function showNewRowForm(btn) {
    var c = document.getElementById('newRowContainer');
    if (c == null) {
        alert('Can\'t find #newRowContainer');
        return;
    }
    var errSpan = getErrSpan(btn.parentElement);
    if (errSpan == null)
        return;

    asyncReqAndRunIfSuccess('newrow', errSpan, function(responseText) {
        c.innerHTML = responseText;
        selectFirstInpElem(c);
    });
}

function asyncSubmitFormAndRunIfSuccess(parentElem, action, method, extraArgs,
        errSpan, f) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
        if (req.readyState == 4) {
            if (req.status == 200) {
                // delete any previous error
                errSpan.textContent = '';
                f(req.responseText);
            } else {
                errSpan.textContent = req.responseText;
            }
        }
    }
    req.open(method, action, true);
    req.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');

    var arr = [];
    var elems = parentElem.querySelectorAll(inpElemSelectors);
    for (var i = 0; i < elems.length; i++) {
        var k = encodeURIComponent(elems[i].name);
        var v = encodeURIComponent(elems[i].value);
        arr.push(k + '=' + v);
    }

    var toSend = arr.join('&');
    if (extraArgs != '')
        if (toSend != '')
            toSend += '&'
        toSend += extraArgs;
    req.send(toSend);
}

// 'elem' is any descendant of <tr>
function submitNewRowForm(elem) {
    var tr = getTrAncestor(elem);
    if (tr == null)
        return;
    var errSpan = getErrSpan(tr);
    if (errSpan == null)
        return;

    asyncSubmitFormAndRunIfSuccess(tr, "insertrow/", "POST", '', errSpan,
            function(responseText) {window.location.reload();});
}

function cancelNewRowForm() {
    var c = document.getElementById('newRowContainer');
    if (c == null) {
        alert('Can\'t find #newRowContainer');
        return;
    }
    c.textContent = '';
}

function keyDownNewRowForm(elem, ev) {
    if (ev.keyCode == 13)
        submitNewRowForm(elem);
    else if (ev.keyCode == 27)
        cancelNewRowForm();
}

function editDbRow(elem) {
    var tr = getTrAncestor(elem);
    if (tr == null)
        return;
    var errSpan = getErrSpan(tr);
    if (errSpan == null)
        return;

    var url ="editrow/?dbRowId=" +
        encodeURIComponent(tr.getAttribute('dbRowId'));
    asyncReqAndRunIfSuccess(url, errSpan, function(responseText) {
        tr.insertAdjacentHTML('afterend', responseText);
        selectFirstInpElem(tr.nextElementSibling);
        tr.parentNode.removeChild(tr);
    });
}

// 'elem' is any descendant of <tr>
function updateDbRow(elem) {
    var tr = getTrAncestor(elem);
    if (tr == null)
        return;
    var errSpan = getErrSpan(tr);
    if (errSpan == null)
        return;

    var extraArgs = 'dbRowId='+encodeURIComponent(tr.getAttribute('dbRowId'));
    asyncSubmitFormAndRunIfSuccess(tr, "updaterow/", "POST", extraArgs,
            errSpan, function(responseText) {
                asyncReqAndRunIfSuccess('viewrow/?' + extraArgs, errSpan,
                    function(responseText) {
                        tr.insertAdjacentHTML('afterend', responseText);
                        tr.parentNode.removeChild(tr);
                    });
            });
}

// 'elem' is any descendant of <tr>
function cancelDbRowEdit(elem) {
    var tr = getTrAncestor(elem);
    if (tr == null)
        return;
    var errSpan = getErrSpan(tr);
    if (errSpan == null)
        return;

    var url='viewrow/?dbRowId='+encodeURIComponent(tr.getAttribute('dbRowId'));
    asyncReqAndRunIfSuccess(url, errSpan, function(responseText) {
        tr.insertAdjacentHTML('afterend', responseText);
        tr.parentNode.removeChild(tr);
    });
}

function keyDownDbRow(elem, ev) {
    if (ev.keyCode == 13)
        updateDbRow(elem);
    else if (ev.keyCode == 27)
        cancelDbRowEdit(elem);
}

var MT_DATETIMER_TIMEOUT = 100;

function checkDateLater(elem, instantly) {
    if (elem.MONEYTRAIL_DATETIMER_PENDING)
        // timer has already been set, hasn't fired yet
        return;

    var resultElem = null;
    var arr = elem.parentElement.getElementsByTagName('span');
    for (var i = 0; i < arr.length; i++)
        if (arr[i].classList.contains('parsedDate')) {
            resultElem = arr[i];
            break;
        }
    if (resultElem == null)
        return;

    elem.MONEYTRAIL_DATETIMER = setTimeout(
            function() {doCheckDate(elem, resultElem);},
            instantly ? 0 : MT_DATETIMER_TIMEOUT);
    elem.MONEYTRAIL_DATETIMER_PENDING = true;
}

function doCheckDate(inputElem, resultElem) {
    var our_timer = inputElem.MONEYTRAIL_DATETIMER;
    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
        if (inputElem.MONEYTRAIL_DATETIMER != our_timer)
            // another timer got started after us; in case our Ajax call
            // completed later, our data is older and must not overwrite
            return;

        if (req.readyState != 4)
            return;

        resultElem.textContent = req.responseText;
    }
    var url = '/date/?date=' + encodeURIComponent(inputElem.value);
    req.open('GET', url, true);
    req.send();

    inputElem.MONEYTRAIL_DATETIMER_PENDING = false;
}
