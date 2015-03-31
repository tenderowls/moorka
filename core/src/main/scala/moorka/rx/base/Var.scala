package moorka.rx.base

import moorka.rx.base.bindings.{Binding, StatefulBinding}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final case class Var[A](private[rx] var x: A) extends Source[A] {

  override def update(v: A) = {
    x = v
    super.update(v)
  }

  def mod(f: A ⇒ Rx[A]): Var[A] = {
    // If you have any idea how 
    // to made it pretty tell me please
    // aleksey.fomkin@gmail.com
    def updateCMod(drop: Boolean): Unit = {
      def checkDrop(fx: Source[A]): Rx[A] = {
        if (drop) {
          new Binding[A, A](fx, x ⇒ Val(x)) 
        }
        else {
          fx
        }
      }
      val rx = f(x) match {
        case fx: Var[A] ⇒ checkDrop(fx)
        case fx: StatefulBinding[_, A] ⇒ checkDrop(fx)
        case fx ⇒ fx
      } 
      rx once { v ⇒
        x = v
        updateCMod(drop = true)
      }
    }
    updateCMod(drop = false)
    this
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
}
