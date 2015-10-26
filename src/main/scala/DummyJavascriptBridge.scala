package org.nlogo.extensions.webview

import netscape.javascript.JSObject

class DummyJavascriptBridge extends JavascriptBridge {
  override def evaluateCommand(commands: String, onFinish: JSObject, onError: JSObject) = {
    log(s"command submitted: $commands")
    try {
      commands match {
        case "error" => runCallback(onError)
        case "wait"  =>
          val t = new Thread(new Runnable {
            def run(): Unit = {
              Thread.sleep(500)
              runCallback(onFinish)
            }
          })
          t.start()
          case _ => runCallback(onFinish)
      }
    } catch {
      case e: Exception =>
        println(e)
        throw e
    }
  }

  override def evaluateReporter(reporter: String, onResult: JSObject, onError: JSObject) = {
    log(s"reporter submitted: '$reporter'")
    val result = reporter match {
      case "number"  => 12345.0
      case "string"  => "I'm a string!!!"
      case "boolean" => Boolean.box(true)
      case "list"    => Seq[AnyRef]("String", Double.box(56789.0))
      case _         => "foobar"
    }
    reporter match {
      case "error" => runCallback(onError)
      case _       => runCallback(onResult, Array(result))
    }
  }
}
