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
        maxDays = Number(document.getElementById('maxdays').value);

    if (isNaN(baseDays) || isNaN(basePrice) || isNaN(extraDayPrice) ||
            isNaN(maxDays) ||
            baseDays < 0 || maxDays < baseDays) {
        document.getElementById(chartDivId).textContent =
            'Invalid input values';
        return;
    }

    var totalPrice = [{days: baseDays, price: basePrice}];
    for (var d = baseDays + 1, p = basePrice; d <= maxDays; d++) {
        p += extraDayPrice;
        totalPrice.push({days: d, price: p});
    }

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
          }],
    });
}
