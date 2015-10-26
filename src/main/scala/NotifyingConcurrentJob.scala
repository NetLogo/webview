package org.nlogo.extensions.webview

import org.nlogo.nvm._

class NotifyingConcurrentJob(
  delegate: Job,
  val onFinished: (Option[AnyRef]) => Unit,
  val onError: Exception => Unit)
    extends ConcurrentJob(delegate.owner, delegate.agentset, delegate.topLevelProcedure, 0, delegate.parentContext, delegate.random) {

  var hasFinished = false
  var error = Option.empty[Exception]

  override def step() = {
    try {
      super.step()
    } catch {
      case e: Exception =>
        error = Some(e)
        throw e
    } finally {
      if (hasFinished && error.nonEmpty)
        error.foreach(onError)
      else if (hasFinished)
        onFinished(Option(result))
    }
  }

  override def finish() = {
    super.finish()
    hasFinished = true
  }
}
