package org.nlogo.extensions.webview

import javafx.embed.swing.JFXPanel
import javafx.scene.web.WebEngine
import javafx.application.Platform.runLater

import org.scalatest.FunSuite
import org.scalatest.concurrent.AsyncAssertions
import org.scalatest.time.{ Millis, Span }

import org.nlogo.api.LogoList

class JsTranslatorTest extends FunSuite with AsyncAssertions {
  val _ = new JFXPanel()

  def testConversion(description: String)(expectedVal: AnyRef, js: String) = {
    test(s"converts $description") {
      val w = new Waiter()
      runLater(new Runnable() {
        override def run(): Unit = {
          val we = new WebEngine()
          val translator = new JsTranslator(we)
          val convertedValue = translator.convert(we.executeScript(js))
          w { assert(expectedVal == convertedValue, s"value class: ${convertedValue.getClass.getCanonicalName}") }
          w.dismiss()
        }
      })
      w.await(timeout(Span(100, Millis)))
    }
  }

  testConversion("numbers to doubles")(Double.box(0.0),           "0.0")
  testConversion("strings to strings")("abc",                     "\"abc\"")
  testConversion("booleans to booleans")(true: java.lang.Boolean, "true")
  testConversion("empty js objects to empty logolist")(LogoList(), "var o = {}; o")
  testConversion("js objects to lists of lists")(
    LogoList(LogoList("abc", Double.box(123.0))), "var o = {\"abc\": 123.0}; o")
  testConversion("empty arrays to empty logolists")(LogoList(), "[]")
  testConversion("arrays to logolists")(LogoList(Double.box(0.0)), "[0.0]")
  testConversion("mixed arrays to logolists")(LogoList(Double.box(0.0), "abc"), "[0.0, \"abc\"]")
  testConversion("nested arrays to logolists")(LogoList(Double.box(0.0), LogoList("abc", "123")), "[0.0, [\"abc\", \"123\"]]")
  testConversion("arrays with objects to logolists")(LogoList(Double.box(0.0), LogoList(LogoList("abc", "123"))), "[0.0, {\"abc\": \"123\"}]")
}
