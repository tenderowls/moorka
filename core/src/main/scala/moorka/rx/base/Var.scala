package moorka.rx.base

import moorka.rx.base.bindings.{Binding, StatefulBinding}
import moorka.rx.death.{Reaper, Mortal}

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
  extends Source[A] with StatefulSource[A] {

  reaper.mark(this)
  
  override private[rx] def update(v: A) = {
    if (_alive && x != v) {
      x = v
      super.update(v)
    }
  }

  private var mods = List.empty[VarHelper.Mod[A]]

  private def removeMod(x: VarHelper.Mod[A]) = {
    mods = mods.filter(_ == x)
  }

  private[rx] def mod(f: A ⇒ Rx[A])(implicit reaper: Reaper = Reaper.nice): Mortal = {
    val mod = new VarHelper.Mod(f, removeMod)
    def listenMod(ignoreStatefulBehavior: Boolean): Unit = {
      addUpstream {
        val rx = mod(x) match {
          case modX: StatefulSource[A] if ignoreStatefulBehavior ⇒
            new Binding[A, A](modX, x ⇒ Val(x))
          case modX ⇒ modX
        }
        rx match {
          case Killer ⇒
            kill()
            Dummy
          case _ ⇒
            rx once { v ⇒
              update(v)
              cleanupUpstreams()
              listenMod(ignoreStatefulBehavior = true)
            }
        }
      }
    }
    mods ::= mod
    listenMod(ignoreStatefulBehavior = false)
    mod
  }

  def modOnce(f: A ⇒ Rx[A]): Unit = {
    addUpstream {
      f(x) once { v ⇒
        cleanupUpstreams()
        update(v)
      }
    }
    cleanupUpstreams()
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
    mods = Nil
  }
}

private object VarHelper {

  final class Mod[A](f: A ⇒ Rx[A], killF: Mod[A] ⇒ Unit)
    extends (A => Rx[A]) with Mortal {

    @inline def apply(x: A) = f(x)

    def kill() = killF(this)
  }

}
