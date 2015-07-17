package moorka.rx.base

import moorka.rx.base.bindings.{Binding, StatefulBinding, StatelessBinding}
import moorka.rx.death.{Mortal, Reaper}

object Var {

  @inline
  @deprecated("Use withMod instead", "0.5.0")
  def withDefaultMod[A](x: A)(f: A ⇒ Rx[A])
                       (implicit reaper: Reaper = Reaper.nice): Var[A] = {
    withMod(x)(f)
  }

  def withMod[A](x: A)(f: A ⇒ Rx[A])
                (implicit reaper: Reaper = Reaper.nice): Var[A] = {
    val res = Var(x)
    res mod f
    res
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final case class Var[A](private[rx] var x: A)
                       (implicit reaper: Reaper = Reaper.nice)
  extends Source[A] with StatefulSource[A] with Binding[A] {

  reaper.mark(this)

  def run(x: A): Unit = ()

  override private[rx] def update(v: A) = {
    if (_alive && x != v) {
      x = v
      super.update(v)
    }
  }

  private var currentModBindings = List.empty[Mortal]

  private[rx] def mod(f: A ⇒ Rx[A])(implicit reaper: Reaper = Reaper.nice): Unit = {
    def listenMod(ignoreStatefulBehavior: Boolean): Unit = {
      withContext(currentModBindings) {
        currentModBindings = List {
          val rx = f(x) match {
            case modX: StatefulSource[A] if ignoreStatefulBehavior ⇒
              new StatelessBinding[A, A](modX, x ⇒ Val(x))
            case modX ⇒ modX
          }
          rx match {
            case Killer ⇒
              kill()
              Dummy
            case _ ⇒
              rx once { v ⇒
                update(v)
                listenMod(ignoreStatefulBehavior = true)
              }
          }
        }
      }
    }
    listenMod(ignoreStatefulBehavior = false)
  }

  def modOnce(f: A ⇒ Rx[A]): Unit = {
    currentModBindings = List(f(x).once(update))
  }

  override def flatMap[B](f: (A) => Rx[B])(implicit reaper: Reaper = Reaper.nice): Rx[B] = {
    if (_alive) {
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
    currentModBindings.foreach(_.kill())
  }
}
