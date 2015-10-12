package moorka.rx

import moorka.death.{Mortal, Reaper}
import moorka.rx.bindings.{Binding, StatefulBinding}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object FSM {
  def apply[T](initialValue: T, ignoreStateEquality: Boolean)(f: T ⇒ Rx[T]): Rx[T] = {
    new FSM(initialValue, ignoreStateEquality, f)
  }
  
  def apply[T](initialValue: T)(f: T ⇒ Rx[T]): Rx[T] = {
    new FSM(initialValue, false, f)
  }
}

final private[moorka] class FSM[T](var x: T, ignoreStateEquality: Boolean, f: T ⇒ Rx[T])
  extends StatefulSource[T] with Binding[T] {

  private[moorka] var dependencies = List.empty[Mortal]

  override def run(x: T): Unit = {
    withContext(dependencies) {
      f(x) match {
        case Val(some) ⇒ update(some, silent = false)
        case Silent(some) ⇒ update(some, silent = true)
        case Killer ⇒ kill()
        case Dummy ⇒ // Do nothing
        case value ⇒
          val dependency = value.foreach(update(_, silent = false))
          dependencies = dependency :: dependencies
      }
    }
  }

  override private[moorka] def update(v: T, silent: Boolean = false) = {
    if (isAlive && (ignoreStateEquality || x != v)) {
      x = v
      super.update(v, silent)
    }
  }

  def flatMap[B](f: (T) => Rx[B])(implicit reaper: Reaper): Rx[B] = {
    if (isAlive) {
      reaper.mark(new StatefulBinding(Some(x), this, f))
    }
    else {
      f(x)
    }
  }

  override def kill(): Unit = {
    super.kill()
    killDependencies(dependencies)
  }

  attachBinding(this)
  run(x)
}
