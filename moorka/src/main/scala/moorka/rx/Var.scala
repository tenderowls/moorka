package moorka.rx

import moorka.death.Mortal
import moorka.rx.bindings.{Binding, StatefulBinding}
import moorka.death.Reaper

object Var {

  @inline
  @deprecated("Use withMod instead", "0.5.0")
  def withDefaultMod[A](x: A)(f: A ⇒ Rx[A])
                       (implicit reaper: Reaper = Reaper.nice): Var[A] = {
    withMod(x)(f)
  }

  def withMod[T](initialValue: T)(mod: T ⇒ Rx[T])
                (implicit reaper: Reaper = Reaper.nice): Var[T] = {
    
    new Var(initialValue) with Binding[T] {
      
      var dependencies = List.empty[Mortal]

      override def run(x: T): Unit = {
        withContext(dependencies) {
          mod(x) match {
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

      override def kill(): Unit = {
        super.kill()
        killDependencies(dependencies)
      }

      attachBinding(this)
      run(x)
    }
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
sealed case class Var[A](private[moorka] var x: A)
                        (implicit reaper: Reaper = Reaper.nice)
  extends Source[A] with StatefulSource[A] {

  reaper.mark(this)

  override private[moorka] def update(v: A, silent: Boolean = false) = {
    if (isAlive && x != v) {
      x = v
      super.update(v, silent)
    }
  }

  def modOnce(f: A ⇒ Rx[A]): Unit = {
    f(x).once(update(_, silent = false))
  }

  override def flatMap[B](f: (A) => Rx[B])(implicit reaper: Reaper = Reaper.nice): Rx[B] = {
    if (isAlive) {
      new StatefulBinding(Some(x), this, f)
    }
    else {
      f(x)
    }
  }

  override def once[U](f: (A) => U)(implicit reaper: Reaper = Reaper.nice): Rx[Unit] = {
    // We don't need to create binding to
    // get value just once.
    f(x)
    Dummy
  }

  override def kill(): Unit = {
    super.kill()
  }
}
