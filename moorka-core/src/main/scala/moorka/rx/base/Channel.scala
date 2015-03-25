package moorka.rx.base

import moorka.rx.base.bindings.Binding

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object Channel {
  
  def signal() = new Channel[Unit]() {
    def fire() = update(())
  }

  def apply[T]() = new Channel[T]()
}

sealed class Channel[A]() extends Source[A] {

  def flatMap[B](f: (A) => Rx[B]): Rx[B] = new Binding(this, f)
}
