package org.nlogo.extensions.webview

import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference
import java.io.File
import java.net.URL

import javafx.application.Platform
import javafx.beans.value.{ ChangeListener, ObservableValue }
import javafx.concurrent.Worker
import javafx.event.EventHandler
import javafx.scene.web.{ WebErrorEvent, WebView => JFXWebView }

import netscape.javascript.JSObject

import org.nlogo.extensions.webview.util.FunctionToCallback.{ function2Runnable, function2ChangeListener }

import scala.util.Try
import scala.concurrent.SyncVar

class JavaFXWebView(val webView: JFXWebView) extends WebView {
  val currentState =
    new AtomicReference[Worker.State](webView.getEngine.getLoadWorker.stateProperty.getValue)

  private var onNextLoadRunnables = Seq[Runnable]({ () =>
    webView.getEngine.locationProperty.addListener { (old: String, newLocation: String) =>
      load(new URL(newLocation))
    }
  })
  private var onEachLoadRunnables = Seq[Runnable]()

  def isLoading = currentState.get == Worker.State.RUNNING

  def isLoaded  = currentState.get == Worker.State.SUCCEEDED

  private def loadedState = webView.getEngine.getLoadWorker.stateProperty

  private def executeOnLoad(f: () => Unit, everyLoad: Boolean = false): Unit = {
    if (everyLoad)
      onEachLoadRunnables = onEachLoadRunnables :+ function2Runnable(f)

    if (isLoaded)
      Platform.runLater(f)
    else if (! everyLoad)
      onNextLoadRunnables = onNextLoadRunnables :+ function2Runnable(f)
  }

  val listener: ChangeListener[Worker.State] =
    function2ChangeListener { (oldState: Worker.State, newState: Worker.State) =>
      newState match {
        case Worker.State.SUCCEEDED => runOnLoadRunnables()
        case _                      =>
      }
      currentState.set(newState)
    }

  if (loadedState.getValue == Worker.State.SUCCEEDED)
    Platform.runLater { () => runOnLoadRunnables() }
  else
    loadedState.addListener(listener)

  def bind(name: String, boundValue: AnyRef, allowRemoteAccess: Boolean): Unit =
    executeOnLoad({ () =>
      if (allowRemoteAccess || webView.getEngine.getLocation.startsWith("file"))
        webView.getEngine.executeScript("window").asInstanceOf[JSObject].setMember(name, boundValue)
    }, everyLoad = true)

  def load(url: URL) = load(url, false)

  def load(url: URL, waitForCompletion: Boolean = false): Unit = {
    val finished = new SyncVar[Boolean]()
    onNextLoadRunnables = onNextLoadRunnables :+ function2Runnable(() => finished.put(true))
    Platform.runLater { () => webView.getEngine.load(url.toString) }
    if (waitForCompletion)
      finished.take
  }

  def reload(): Unit =
    Platform.runLater { () => webView.getEngine.reload() }

  def executeJS(js: String): Unit =
    executeOnLoad { () => carefullyExecuteScript(js) }

  def executeJSForResult(js: String): AnyRef = {
    if (! isLoaded && ! isLoading)
      throw new IllegalStateException("Cannot execute js for result when no page open")
    else {
      val result = new SyncVar[Try[AnyRef]]()
      Platform.runLater { () =>
        val translator = new JsTranslator(webView.getEngine)
        result.put(Try(webView.getEngine.executeScript(js)).map(translator.convert))
      }
      result.get(3000).getOrElse {
        Platform.runLater { () =>
          webView.getEngine.setJavaScriptEnabled(false)
          webView.getEngine.setJavaScriptEnabled(true)
        }
        throw new TimeoutException("timeout exceeeded when waiting for javascript value")
      }.get
    }
  }

  private def runOnLoadRunnables(): Unit = {
    onEachLoadRunnables.foreach(_.run())
    onNextLoadRunnables.foreach(_.run())
    onNextLoadRunnables = Seq()
  }

  private def carefullyExecuteScript(js: String): Unit = {
    try {
      webView.getEngine.executeScript(js)
    } catch {
      case e: netscape.javascript.JSException => // swallow if no one is waiting for a result
    }
  }
}


object JavaFXWebView extends WebViewFactory {

  def browser(bridge: JavascriptBridge): JavaFXWebView = {
    val handleVar = new SyncVar[JavaFXWebView]
    Platform.runLater { () =>
      initBrowser(bridge, handleVar)
    }
    handleVar.take
  }

  def initBrowser(bridge: JavascriptBridge, handleVar: SyncVar[JavaFXWebView]): Unit = {
    val wv = new JFXWebView()
    wv.getEngine.setOnError(new EventHandler[WebErrorEvent] {
      def handle(errorEvent: WebErrorEvent): Unit = {
        System.out.println("Error encountered")
        System.err.println(errorEvent.getException)
        System.err.println(errorEvent.getMessage)
      }
    })
    val handle = new JavaFXWebView(wv)
    handle.bind("NetLogo",     bridge,         false)
    handle.bind("javaWebView", handle.webView, false)
    handleVar.put(handle)
  }
}
