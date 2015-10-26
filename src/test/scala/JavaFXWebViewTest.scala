package org.nlogo.extensions.webview

import org.scalatest.FunSuite
import org.scalatest.concurrent.AsyncAssertions

import java.io.File
import java.net.URL

import javafx.application.Platform
import javafx.beans.value.{ ChangeListener, ObservableValue }
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.web.{ WebEngine, WebView => JFXWebView }

import org.scalatest.time.{ Millis, Span }

import org.nlogo.extensions.webview.util.FunctionToCallback.{ function2Runnable, function2ChangeListener }

import scala.concurrent.SyncVar

class JavaFXWebViewTest extends FunSuite with AsyncAssertions {
  val _ = new JFXPanel()

  trait JavaFXWebViewHelper {
    val w = new Waiter()

    def loadedContent: Option[String] = Some("")

    val webViewVar = new SyncVar[JavaFXWebView]()

    Platform.runLater { () =>
      val webView = new JFXWebView()
      loadedContent.foreach(webView.getEngine.loadContent(_))
      webViewVar.put(new JavaFXWebView(webView))
      w.dismiss()
    }

    w.await(timeout(Span(300, Millis)))

    val webView = webViewVar.take

    def awaitJavaFXWebViewLoad(): Unit = {
      while (! webView.isLoaded) {}
    }

    def webEngine =
      webView.webView.getEngine

    def platformAssert(assertion: => Boolean) = {
      Platform.runLater { () =>
        w { assert(assertion) }
        w.dismiss()
      }
      w.await(timeout(Span(100, Millis)))
    }
  }

  test("accepts javascript for execution before having browser set") {
    new JavaFXWebViewHelper {
      webView.executeJS("window.foo = 'bar';")
      awaitJavaFXWebViewLoad()
      platformAssert(webEngine.executeScript("window.foo") == "bar")
    }
  }

  test("accepts javascript for execution after having browser set") {
    new JavaFXWebViewHelper {
      awaitJavaFXWebViewLoad()
      webView.executeJS("window.foo = 'bar';")
      platformAssert(webEngine.executeScript("window.foo") == "bar")
    }
  }

  test("does not raise an exception if executeJS attempts to run bad javascript") {
    new JavaFXWebViewHelper {
      awaitJavaFXWebViewLoad()
      webView.executeJS("window.foo = ;")
      platformAssert {
        webEngine.executeScript("window.foo == undefined")
          .asInstanceOf[java.lang.Boolean].booleanValue
      }
    }
  }

  test("returns logo value for executeJSForResult after browser is set") {
    new JavaFXWebViewHelper {
      awaitJavaFXWebViewLoad()
      assert(4 == webView.executeJSForResult("2 + 2"))
    }
  }

  test("raises exception if executeJSForResult is attempted when browser is not set") {
    new JavaFXWebViewHelper {
      override lazy val loadedContent = None
      intercept[IllegalStateException] { webView.executeJSForResult("2 + 2") }
    }
  }

  test("raises exception if executeJSForResult asks bad javascript to be executed") {
    new JavaFXWebViewHelper {
      awaitJavaFXWebViewLoad()
      intercept[netscape.javascript.JSException] {
        webView.executeJSForResult("{")
      }
    }
  }

  test("binds values to window") {
    new JavaFXWebViewHelper {
      awaitJavaFXWebViewLoad()
      webView.bind("foo", "ABC", true)
      assert(webView.executeJSForResult("window.foo") == "ABC")
    }
  }

  test("binds the bridge object to NetLogo automatically") {
    val bridge = new DummyJavascriptBridge()
    val handle = JavaFXWebView.browser(bridge)
    handle.load(new File("html/index.html").toURI.toURL, true)
    assert(handle.executeJSForResult("window.NetLogo") == bridge)
  }

  test("does not bind the bridge object to NetLogo when loading remote pages") {
    val bridge = new DummyJavascriptBridge()
    val handle = JavaFXWebView.browser(bridge)
    handle.load(new URL("http://www.google.com/"), true)
    assert(handle.executeJSForResult("window.NetLogo") == "undefined")
  }

  test("binds the bridge object across page loads") {
    val bridge = new DummyJavascriptBridge()
    val handle = JavaFXWebView.browser(bridge)
    handle.load(new File("html/index.html").toURI.toURL, true)
    handle.load(new File("html/test2.html").toURI.toURL, true)
    assert(handle.executeJSForResult("window.NetLogo") == bridge)
  }

  test("doesn't hang when following a link to another page") {
    new JavaFXWebViewHelper {
      webView.load(new URL("http://example.org"))
      Thread.sleep(500)
      assert(webView.isLoaded)
    }
  }

  /* Disabled because it breaks other tests. The halting problem sucks :P
  test("raises an exception if the javascript operation times out") {
    new JavaFXWebViewHelper {
      awaitJavaFXWebViewLoad()
      intercept[java.util.concurrent.TimeoutException] {
        webView.executeJSForResult("while (true) {};")
      }
    }
  }
  */
}

