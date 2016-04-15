package org.nlogo.extensions.webview

import javafx.scene.web.WebEngine

import netscape.javascript.JSObject

import org.nlogo.core.LogoList

class JsTranslator(webEngine: WebEngine) {
  val ObjectProto =
    webEngine.executeScript("Object").asInstanceOf[JSObject]

  val unrollJsArray: PartialFunction[JSObject, Seq[AnyRef]] = {
    case j: JSObject if arrayLengthOption(j).nonEmpty =>
      for (i <- 0 until arrayLengthOption(j).get) yield convert(j.getSlot(i))
  }

  val unrollJsObject: PartialFunction[JSObject, Seq[(AnyRef, AnyRef)]] = {
    case j: JSObject if objectKeys(j).nonEmpty =>
      objectKeys(j).get.map(k => (convert(k), convert(j.getMember(k.asInstanceOf[String]))))
  }

  def convert(x: AnyRef): AnyRef = x match {
    case j: JSObject          =>
      (unrollJsArray orElse
        (unrollJsObject andThen (_.map(pairToLogoList))))
          .lift(j)
          .map(s => LogoList.fromIterator(s.toIterator))
          .getOrElse(LogoList())
    case i: java.lang.Integer => Double.box(i.toDouble)
    case _                    => x
  }

  private def arrayLengthOption(j: JSObject): Option[Int] =
    j.getMember("length") match {
      case i: java.lang.Integer => Some(i)
      case _ => None
    }

  private def objectKeys(j: JSObject): Option[Seq[AnyRef]] =
    unrollJsArray.lift(ObjectProto.call("keys", j).asInstanceOf[JSObject])

  private def pairToLogoList(t: (AnyRef, AnyRef)): LogoList =
    LogoList(t._1, t._2)
}
