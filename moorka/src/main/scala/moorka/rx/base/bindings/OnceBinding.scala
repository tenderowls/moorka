package moorka.rx.base.bindings

import moorka.rx.base.Source
import moorka.rx.{Dummy, Reaper, Rx}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class OnceBinding[A, U](val parent: Source[A], lambda: A ⇒ U) 
  extends Rx[Unit] 
  with HasBindingContext[A]
  with Binding[A] {
 
  var isAlive = true
 
  def alive: Boolean = isAlive

  def flatMap[B](f: Unit => Rx[B])(implicit reaper: Reaper): Rx[B] = Dummy

  def run(x: A): Unit = {
    if (isAlive) {
      kill()
      lambda(x)
    }
  }

  def kill(): Unit = {
    isAlive = false
    parent.detachBinding(this)
  }

  parent.attachBinding(this)
}

