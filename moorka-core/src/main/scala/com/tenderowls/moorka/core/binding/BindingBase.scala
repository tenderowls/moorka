package com.tenderowls.moorka.core.binding

import com.tenderowls.moorka.core.events.{Emitter, Event}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[binding] abstract class BindingBase[A] extends Emitter[Bindable[A]] with Bindable[A] {  self =>

  def observe(f: (Bindable[A]) => Unit): Event[Bindable[A]] = {
    val x = subscribe(f)
    f(this)
    x
  }

  def map[B](f: (A) => B): Bindable[B]  = {
    new Var[B](f(apply())) {
      self subscribe { _ =>
        this.value = f(self())
        emit(this)
      }
    }
  }
}
