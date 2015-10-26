var netlogo = (function() {
  "use strict";

  function runCommand() {
    var commandInput = document.getElementById("command").value;
    document.getElementById("run-command").disabled = true;
    var reenableButton = function() {
      document.getElementById("run-command").disabled = false;
    };
    NetLogo.evaluateCommand(commandInput, reenableButton, reenableButton);
  }

  function translateResult(r) {
    if (r.doubleValue !== undefined) {
      return r.doubleValue();
    } else {
      return r;
    }
  }

  function runReporter() {
    var reporterInput = document.getElementById("reporter").value;
    document.getElementById("run-reporter").disabled = true;
    var reenableButton = function(e) {
      document.getElementById("run-reporter").disabled = false;
    };
    var reportedValue = function(rawResult) {
      var result = translateResult(rawResult);
      reenableButton();
      var resultP = document.createElement('p');
      resultP.appendChild(document.createTextNode('Result: ' + result.toString()));
      document.body.appendChild(resultP);
    };
    NetLogo.evaluateReporter(reporterInput, reportedValue, reenableButton);
  }

  return {
    runCommand: runCommand,
    runReporter: runReporter
  };
})();
