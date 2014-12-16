package com.tenderowls.moorka.core.rx.ops

import com.tenderowls.moorka.core._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class RxStateOps[A](val self: RxState[A]) extends AnyVal {

  /**
   * Same as [[RxStreamOps.subscribe]] but calls f immediately
   * @param f listener
   * @return slot
   */
  def observe(f: => Any): RxStream[A] = {
    val x = self.subscribe(_ => f)
    f
    x
  }

  def map[B](f: (A) => B): RxState[B] = {
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

  def zip[B](another: RxState[B]): RxState[(A, B)] = {
    new Var[(A, B)]((self(), another())) {
      def subscriber(x: Any): Unit = {
        value = (self(), another())
        emit(value)
      }
      val rip = Seq(
        self subscribe subscriber,
        another subscribe subscriber
      )
      override def kill(): Unit = {
        rip.foreach(_.kill())
        super.kill()
      }
    }
  }
}
