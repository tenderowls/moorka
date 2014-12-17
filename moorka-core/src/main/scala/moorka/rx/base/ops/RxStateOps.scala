package moorka.rx.base.ops

import moorka.rx._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class RxStateOps[A](val self: State[A]) extends AnyVal {

  /**
   * Same as [[RxStreamOps.subscribe]] but calls f immediately
   * @param f listener
   * @return slot
   */
  def observe(f: => Any): Channel[A] = {
    val x = self.subscribe(_ => f)
    f
    x
  }

  def map[B](f: (A) => B): State[B] = {
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

  def zip[B](another: State[B]): State[(A, B)] = {
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
