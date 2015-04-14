package moorka.rx.base

import moorka.rx.base.bindings.Binding

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object Channel {

  def listener[T](f: T ⇒ Unit): Channel[T] = {
    new Channel[T]() {
      val subscription = foreach(f)

      override def kill(): Unit = {
        super.kill()
        subscription.kill()
      }
    }
  }

  def signal(): Signal = new Signal()

  def apply[T]() = new Channel[T]()
}

sealed class Signal extends Channel[Unit] {

  def fire(): Unit = update(())
}

sealed class Channel[A]() extends Source[A] {

  def flatMap[B](f: (A) => Rx[B]): Rx[B] = new Binding(this, f)
}
