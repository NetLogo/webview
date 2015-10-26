package org.nlogo.extensions.webview

import javafx.application.Platform
import netscape.javascript.JSObject
import org.nlogo.agent.Observer
import org.nlogo.api.SimpleJobOwner
import org.nlogo.app.App
import org.nlogo.nvm.Procedure

trait JavascriptBridge {
  def evaluateCommand(commands: String, onFinish: JSObject, onError: JSObject)

  def evaluateReporter(reporter: String, onResult: JSObject, onError: JSObject)

  def log(s: String) = {
    System.out.println(s)
  }

  protected def runCallback(callback: JSObject, args: Array[Any] = Array()) = {
    Platform.runLater(new Runnable() {
      def run(): Unit = {
        val emptyThis = callback.eval("undefined")
        callback.call("apply", emptyThis, args)
      }
    })
  }
}
