// when everything is ready, show the chart
window.onload = showChart;

var chartDivId = 'chartcontainer';
var chart;

function showChart() {
    if (chart) {
        chart.destroy();
        chart = undefined;
    }

    var baseDays = Number(document.getElementById('basedays').value),
        basePrice = Number(document.getElementById('baseprice').value),
        extraDayPrice = Number(document.getElementById('extradayprice').value),
        maxDays = Number(document.getElementById('maxdays').value),
        maxDataPoints = Number(document.getElementById('maxdatapoints').value);

    if (isNaN(baseDays) || isNaN(basePrice) || isNaN(extraDayPrice) ||
            isNaN(maxDays) ||
            baseDays < 0 || maxDays < baseDays || maxDataPoints < 1) {
        document.getElementById(chartDivId).textContent =
            'Invalid input values';
        return;
    }

    // evenly spaced-out points, at most maxPoints (e.g. 50)
    var daysArr = (function(min, max, maxPoints) {
        maxPoints = Math.min(maxPoints, max - min + 1);
        if (maxPoints == 1) {
            return [min];
        }
        var result = [];
        for (var i = 0; i < maxPoints; i++) {
            result.push(Math.round(min + (max-min)*i/(maxPoints-1)));
        }
        return result;
    })(baseDays, maxDays, maxDataPoints);
    var totalPrice = daysArr.map(function(d) {
        return {days: d, price: basePrice + (d-baseDays)*extraDayPrice};
    });

    chart = new Highcharts.Chart({
        chart: { renderTo: chartDivId },
        title: { text: 'HSL Season Ticket Price' },
        subtitle: { text: 'Use the controls below the chart to show/hide ' +
            'Total/Monthly/Daily prices. They use separate y-axes.' },
        xAxis: { title: { text: 'Validity days' } },
        yAxis: [{
            title: { text: 'Total price' },
            showEmpty: false
        }, {
            title: { text: 'Price per month (30 days)' },
            showEmpty: false
        }, {
            title: { text: 'Price per day' },
            showEmpty: false
        }],
        tooltip: { valueDecimals: 2 },
        series: [{
            name: 'Total price',
            data: totalPrice.map(function(k) {
                return {name: 'If buying ' + k.days + ' days:',
                    x: k.days, y: k.price};
            })
        }, {
            name: 'Monthly (30-day) price',
            yAxis: 1,
            data: totalPrice.map(function(k) {
                return {name: 'If buying ' + k.days + ' days:',
                    x: k.days, y: k.price * 30 / k.days};
            })
        }, {
            name: 'Price per day',
            // the ‘visible’ field isn't documented in an obvious place
            visible: false,
            yAxis: 2,
            data: totalPrice.map(function(k) {
                return {name: 'If buying ' + k.days + ' days:',
                    x: k.days, y: k.price / k.days};
            })
        }]
    });
}
