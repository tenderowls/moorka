package com.tenderowls.moorka.core.binding

import com.tenderowls.moorka.core.events.Event
import org.scalajs.dom

import scala.scalajs.js

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class ExpressionBinding[A](dependencies: Seq[Bindable[_]])(expr: => A) extends BindingBase[A] {

  private var subscriptions: List[Event[_]] = Nil

  private var value: A = expr

  private var _valid: Boolean = true

  dependencies.foreach {
    subscriptions ::= _ subscribe { _ =>
      if (_valid) {
        _valid = false
        // Deferred event emit cause more
        // than one dependency could be changed
        dom.setTimeout( { () => emit(this) }: js.Function , 1)
      }
    }
  }

  def apply(): A = {
    if (!_valid) {
      value = expr
      _valid = true
    }
    value
  }

  override def kill(): Unit = {
    subscriptions.foreach(_.kill())
    super.kill()
  }
}
