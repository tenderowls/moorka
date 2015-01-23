package moorka.rx.base.ops

import moorka.rx._
import moorka.rx.death.Reaper

import scala.concurrent.Future

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class StateOps[A](val self: State[A]) extends AnyVal {

  /**
   * Same as [[ChannelOps.subscribe]] but calls f immediately
   * @param f listener
   * @return slot
   */
  def observe(f: => Any)(implicit reaper: Reaper = Reaper.nice): Channel[A] = {
    val x = self.subscribe(_ => f)
    f; x
  }

  def flatMap[B](f: (A) => State[B])(implicit reaper: Reaper = Reaper.nice): State[B] = {
    val x = f(self())
    reaper.mark(x)
    x
  }

  def map[B](f: (A) => B)(implicit reaper: Reaper = Reaper.nice): State[B] = {
    val x = new Var[B](f(self())) {
      val rip = self subscribe { _ =>
        update(f(self()))
      }
      override def kill(): Unit = {
        rip.kill()
        super.kill()
      }
    }
    reaper.mark(x)
    x
  }

  def zip[B](another: State[B])(implicit reaper: Reaper = Reaper.nice): State[(A, B)] = {
    val x = new Var[(A, B)]((self(), another())) {
      def subscriber(x: Any): Unit = {
        update((self(), another()))
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
    reaper.mark(x)
    x
  }
}
