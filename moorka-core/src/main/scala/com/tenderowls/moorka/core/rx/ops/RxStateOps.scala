package com.tenderowls.moorka.core.rx.ops

import com.tenderowls.moorka.core._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class RxStateOps[A](val self: RxState[A]) extends AnyVal {

  def observe(f: => Any): RxStream[A] = {
    val x = self.subscribe(_ => f)
    f
    x
  }

  def map[B](f: (A) => B): RxState[B]  = {
    new Var[B](f(self())) {
      val rip = self subscribe { _ =>
        this.value = f(self())
        emit(value)
      }
      override def kill(): Unit = {
        rip.kill()
        super.kill()
      }
    }
  }

}
