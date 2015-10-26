package org.nlogo.extensions.webview.util

// this was copied over from the "vid" extension, should be made into
// a shared dependency at some point

import scala.language.implicitConversions

import java.util.concurrent.Callable

import javafx.beans.value.{ ChangeListener, ObservableValue }

object FunctionToCallback {
  implicit def function2Runnable(f: () => Any): Runnable = {
    new Runnable {
      override def run(): Unit = {
        try {
          f()
        } catch {
          case e: Exception =>
            println("Exception on thread: " + Thread.currentThread.getName)
            println(e.getMessage)
            throw e
        }
      }
    }
  }

  implicit def function2Callable[T](f: () => T): Callable[T] = {
    new Callable[T] {
      override def call(): T = f()
    }
  }

  implicit def function2ChangeListener[T](f: (T, T) => Unit): ChangeListener[T] =
    new ChangeListener[T] {
      def changed(obs: ObservableValue[_ <: T], oldVal: T, newVal: T) = f(oldVal, newVal)
    }
}
