package org.nlogo.extensions.webview

import org.nlogo.agent.Observer
import org.nlogo.api.{ CompilerException, SimpleJobOwner }
import org.nlogo.app.App
import org.nlogo.nvm.Procedure
import org.nlogo.workspace.AbstractWorkspace
import netscape.javascript.JSObject

class NetLogoJavascriptBridge extends JavascriptBridge {
  def evaluateCommand(commands: String, onFinish: JSObject, onError: JSObject) =
    asyncEval(_.compileCommands(commands), onFinish, onError)

  def evaluateReporter(reporter: String, onResult: JSObject, onError: JSObject) =
    asyncEval(_.compileReporter(reporter), onResult, onError)

  private def asyncEval(toProcedure: AbstractWorkspace => Procedure, onResult: JSObject, onError: JSObject) = {
    val workspace = App.app.workspace
    val owner = new SimpleJobOwner("WebView Extension", workspace.world.mainRNG, classOf[Observer])
    try {
      val procedure = toProcedure(workspace)
      val job = workspace.jobManager.makeConcurrentJob(owner, workspace.world.observers, procedure)
      val notifyingJob = new NotifyingConcurrentJob(job,
        { (r) => runCallback(onResult, Array(r.getOrElse(null))) },
        { (e) => runCallback(onError, Array(e)) })
      workspace.jobManager.addJob(notifyingJob, false)
    } catch {
      case e: CompilerException =>
        runCallback(onError, Array(e))
    }
  }
}
