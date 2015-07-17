package moorka.rx.base

import moorka.rx.base.bindings.StatelessBinding
import moorka.rx.death.Reaper

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object Channel {

  def listener[T](f: T ⇒ Unit)(implicit reaper: Reaper = Reaper.nice): Channel[T] = {
    reaper mark {
      new Channel[T]() {
        val subscription = foreach(f)(Reaper.nice)
        override def kill(): Unit = {
          super.kill()
          subscription.kill()
        }
      }
    }
  }

  def signal()(implicit reaper: Reaper = Reaper.nice): Signal = {
    reaper.mark(new Signal())
  }

  def apply[T]()(implicit reaper: Reaper = Reaper.nice) = {
    reaper.mark(new Channel[T]())
  }
}

class Signal extends Channel[Unit] {

  def fire(): Unit = update(())
}

class Channel[A]() extends Source[A] {

  def flatMap[B](f: (A) => Rx[B])(implicit reaper: Reaper = Reaper.nice): Rx[B] = {
    if (_alive) {
      reaper.mark(new StatelessBinding(this, f))
    }
    else {
      Dummy
    }
  }
}
