package com.tenderowls.moorka.core.binding

import com.tenderowls.moorka.core.Emitter
import com.tenderowls.moorka.core.rx.RxState

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class ExpressionBinding[A](dependencies: Seq[RxState[_]])
                          (expression: => A)
  extends Emitter[A] with RxState[A] {

  private var value = expression

  private var _valid = true

  private val subscriptions = dependencies.map {
    _ subscribe { _ =>
      if (_valid) {
        _valid = false
        // Deferred event emit cause more
        // than one dependency could be changed
        Future(emit(value))
      }
    }
  }

  def apply(): A = {
    if (!_valid) {
      value = expression
      _valid = true
    }
    value
  }

  override def kill(): Unit = {
    subscriptions.foreach(_.kill())
    super.kill()
  }
}
