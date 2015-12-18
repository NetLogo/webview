package org.nlogo.extensions.webview

import java.io.File
import org.scalatest.{ FunSpec, OneInstancePerTest }
import org.easymock.EasyMock.{ expect => mockExpect, anyObject }
import org.scalatest.mock.EasyMockSugar

import java.net.URL

trait FxManagerBehaviors { this: FunSpec with EasyMockSugar =>
  def cannotOpen(manager: WebViewStateManager, container: Container, before: () => Unit) = {
    val barFile = new java.io.File("bar").toURI.toURL

    it("cannot open a tab or a frame") {
      before()
      intercept[IllegalStateException] { manager.tab(container, barFile) }
      intercept[IllegalStateException] { manager.frame(container, barFile) }
    }
  }

  def canClose[T <: Container](manager: WebViewStateManager, containerMock: T, before: () => Unit) = {
    it("can be closed without error") {
      implicit val mocks = MockObjects(containerMock)

      mockExpect(containerMock.show(anyObject()))
      mockExpect(containerMock.close())

      whenExecuting {
        before()
        manager.close()
      }
    }
  }

  def executesJS[T <: WebView](manager: WebViewStateManager, browserHandleMock: T, before: () => Unit) = {
    it("can execute JS") {
      implicit val mocks = MockObjects(browserHandleMock)

      mockExpect(browserHandleMock.executeJS("foobar"))

      whenExecuting {
        before()
        manager.executeJS("foobar")
      }
    }

    it("can load modules") {
      implicit val mocks = MockObjects(browserHandleMock)

      mockExpect(browserHandleMock.bind("foo", "bar", true))

      whenExecuting {
        before()
        manager.addModule("foo", "bar")
      }
    }
  }

  def canLoadPath[T <: WebView](manager: WebViewStateManager, browserHandleMock: T, before: () => Unit) = {
    it("can load a remote URL") {
      implicit val mocks = MockObjects(browserHandleMock)
      mockExpect(browserHandleMock.load(new URL("http://example.org/")))

      whenExecuting {
        before()
        manager.load(new URL("http://example.org/"))
      }
    }

    it("can reload the page") {
      implicit val mocks = MockObjects(browserHandleMock)
      mockExpect(browserHandleMock.reload())

      whenExecuting {
        before()
        manager.reload()
      }
    }
  }
}

class WebViewStateManagerSpec extends FunSpec with OneInstancePerTest with EasyMockSugar with FxManagerBehaviors {
  val browserHandleMock = mock[WebView]
  val indexDocument = new java.io.File("foobar").toURI.toURL
  val browserFactory = new WebViewFactory {
    def browser(bridge: JavascriptBridge) = browserHandleMock
  }

  describe("WebViewStateManager") {
    describe("When not started") {
      val containerMock = mock[Container]
      val bridgeMock    = mock[JavascriptBridge]
      val manager = new WebViewStateManager(bridgeMock, browserFactory)

      it("can be closed without error") {
        manager.close()
      }

      it("can open a frame") {
        manager.frame(containerMock, indexDocument)
      }

      it("can open a tab") {
        manager.tab(containerMock, indexDocument)
      }

      it("errors when executing JS") {
        intercept[IllegalStateException] {
          manager.executeJS("return 2 + 2;")
        }
      }
    }

    describe("When a tab has been opened") {
      val bridgeMock    = mock[JavascriptBridge]
      val manager = new WebViewStateManager(bridgeMock, browserFactory)
      val containerMock = mock[Container]
      val before = { () => manager.tab(containerMock, indexDocument) }

      it should behave like canClose(manager, containerMock, before)
      it should behave like cannotOpen(manager, containerMock, before)
      it should behave like canLoadPath(manager, browserHandleMock, before)
      it should behave like executesJS(manager, browserHandleMock, before)
    }

    describe("After a tab has been closed") {
      val bridgeMock    = mock[JavascriptBridge]
      val manager = new WebViewStateManager(bridgeMock, browserFactory)
      val containerMock = mock[Container]
      manager.tab(containerMock, indexDocument)
      manager.close()

      it("can open a frame") {
        manager.frame(containerMock, indexDocument)
      }
    }

    describe("When a frame has been opened") {
      val bridgeMock    = mock[JavascriptBridge]
      val manager = new WebViewStateManager(bridgeMock, browserFactory)
      val containerMock = mock[Container]
      val before = { () => manager.frame(containerMock, indexDocument) }

      it should behave like canClose(manager, containerMock, before)
      it should behave like cannotOpen(manager, containerMock, before)
      it should behave like canLoadPath(manager, browserHandleMock, before)
      it should behave like executesJS(manager, browserHandleMock, before)
    }
  }
}
