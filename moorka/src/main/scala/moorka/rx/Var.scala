package moorka.rx

import moorka.death.Reaper
import moorka.rx.bindings.StatefulBinding

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

  /**
   * Warning: don't use this method without special necessity
   * @return current value of this Var
   */
  def unsafeGet: A = x

  /**
   * Updates Var directly, without Rx container
   * Warning: don't use this method without special necessity
   * @param x New value for Var
   * @param silent do not run bindings when true
   */
  @inline def unsafeSet(x: A, silent: Boolean = false): Unit = {
    update(x, silent)
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
