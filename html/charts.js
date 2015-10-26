// requires d3:
// <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.6/d3.min.js" charset="utf-8"></script>
var charts = (function() {
  "use strict";

  var barChart = (function() {
      var allData = []

      function addData(dataPoint) {
        var svg = d3.select("svg");

        allData.push(dataPoint);

        var x = d3.scale.linear()
        .domain([0, allData.length])
        .range([0, 500]);

        var y = d3.scale.linear()
        .domain([0, d3.max(allData)])
        .range([0, 400]);

        var rects = svg.selectAll("rect").data(allData);

        rects.enter().append("rect").attr("color", "black");

        rects.attr("height", function(d) { return y(d); })
          .attr("width", function(d, i) { return 500 / allData.length; })
          .attr("x", function(d, i) { return x(i); })
          .attr("y", function(d) { return 400 - y(d); });

        rects.exit().remove();
      }

      return { allData: allData, addData: addData };
  })();

  var stackedBarChart = (function() {
      var allData = []

      function addData(keys, datum) {
        var svg = d3.select("svg");

        allData.push(datum)

        allData.forEach(function(d) {
          if (d.totalValue == undefined) {
            d.totalValue = keys.map(function(k) { return datum[k]; }).reduce(function(a, b) { return a + b; });
            d.index = allData.length - 1;
          }
        })

        var y = d3.scale.linear()
          .domain([0, d3.max(allData.map(function(d) { return d.totalValue; }))])
          .range([0, 400])

        var x = d3.scale.linear()
          .domain([0, allData.length])
          .range([0, 500]);

        var rectangleData = allData.map(function(d) {
            var accumulatedOffset = 0;
            return keys.map(function(k) {
                var coloredRectangle = {
                  xi:       d.index,
                  yi:       accumulatedOffset,
                  quantity: d[k],
                  color:    k
                };
                accumulatedOffset += d[k];
                return coloredRectangle;
              });
            }).reduce(function(a, b) { return a.concat(b); });

        var rects = svg.selectAll("rect").data(rectangleData);

        rects.enter().append("rect");

        rects.style("fill", function(d) { return d.color; })
          .attr("x", function(d) { return x(d.xi); })
          .attr("width", 500 / allData.length)
          .attr("height", function(d) { return y(d.quantity); })
          .attr("y", function(d) {
              console.log(d);
              return 400 - (y(d.quantity + d.yi));
              });

        rects.exit().remove();
      }

      return { allData: allData, addData: addData };
  })();

  return { barChart: barChart, stackedBarChart: stackedBarChart };
})();
