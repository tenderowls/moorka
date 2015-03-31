package moorka.rx.base

import moorka.rx.death.Mortal

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Rx[+A] extends Mortal {

  def alive: Boolean 
  
  @inline def >>=[B](f: A ⇒ Rx[B]): Rx[B] = flatMap(f)

  /**
   * Creates new binding which will pull value
   * from Rx returned by `f`. 
   */
  def flatMap[B](f: A ⇒ Rx[B]): Rx[B]

  /**
   * Creates new binding which will receive value from `this`. 
   * Note: foreach stops data flow. Any combinator applied to
   * `foreach` return value will produce no result. 
   */
  def foreach[U](f: A ⇒ U): Rx[Unit] = {
    flatMap { x ⇒
      f(x)
      Dummy
    }
  }

  /**
   * Similar to foreach but call lambda once 
   * @see [[foreach]] 
   */
  def once[U](f: A ⇒ U): Rx[Unit] = {
    flatMap { x ⇒
      f(x)
      Killer
    }
  }

  def withFilter(f: A ⇒ Boolean): Rx[A] = flatMap { x ⇒
    if (f(x)) Val(x)
    else Dummy
  }

  /**
   * Creates new binding applying `f` to values.
   * @return
   */
  @inline def filter(f: A ⇒ Boolean): Rx[A] = withFilter(f)

  /**
   * Creates new binding applying `f` to values 
   */
  def map[B](f: A ⇒ B): Rx[B] = flatMap { x ⇒
    Val(f(x))
  }
}

final case class Val[+A](x: A) extends Rx[A] {

  def flatMap[B](f: (A) => Rx[B]): Rx[B] = f(x)

  def kill(): Unit = ()

  def alive: Boolean = true
}

case object Dummy extends Rx[Nothing] {
  
  def flatMap[B](f: Nothing ⇒ Rx[B]): Rx[B] = Dummy

  def kill() = ()

  def alive: Boolean = false
}

case object Killer extends Rx[Nothing] {
  
  def flatMap[B](f: Nothing ⇒ Rx[B]): Rx[B] = Dummy

  def kill() = ()

  def alive: Boolean = false
}
