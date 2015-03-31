package moorka.rx.base

import moorka.rx.base.bindings.{Binding, StatefulBinding}
import moorka.rx.death.Mortal

object Var {
  def withDefaultMod[A](x: A)(f: A ⇒ Rx[A]): Var[A] = {
    val res = Var(x)
    res mod f
    res
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final case class Var[A](private[rx] var x: A) 
  extends Source[A] with StatefulSource[A] {

  override def update(v: A) = {
    x = v
    super.update(v)
  }

  private var mods = List.empty[VarHelper.Mod[A]]

  private def removeMod(x: VarHelper.Mod[A]) = {
    mods = mods.filter(_ == x)
  }

  def mod(f: A ⇒ Rx[A]): Mortal = {
    val mod = new VarHelper.Mod(f, removeMod)
    def listenMod(ignoreStatefulBehavior: Boolean): Unit = {
      addUpstream {
        val rx = mod(x) match {
          case modX: StatefulSource[A] if ignoreStatefulBehavior ⇒
            new Binding[A, A](modX, x ⇒ Val(x))
          case modX ⇒ modX
        }
        rx once { v ⇒
          x = v
          cleanupUpstreams()
          listenMod(ignoreStatefulBehavior = true)
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
        x = v
      }
    }
  }

  //  @deprecated("Use foreach() instead subscribe(). Note that foreach calls `f` immediately", "0.4.0")
  //  override def subscribe[U](f: (A) => U): Rx[Unit] = drop(1).foreach(f)

  override def flatMap[B](f: (A) => Rx[B]): Rx[B] = {
    new StatefulBinding(Some(x), this, f)
  }

  override def once[U](f: (A) => U): Rx[Unit] = {
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