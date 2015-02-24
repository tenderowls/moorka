package moorka.rx.base

import moorka.rx.death.Mortal

import scala.ref.WeakReference

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
sealed trait Rx[+A] extends Mortal {

  @inline def >>=[B](f: A ⇒ Rx[B]): Rx[B] = flatMap(f)

  def flatMap[B](f: A ⇒ Rx[B]): Rx[B]

  def foreach[U](f: A ⇒ U): Rx[Unit] = {
    flatMap { x ⇒
      f(x)
      Dummy
    }
  }

  def once[U](f: A ⇒ U): Rx[Unit] = {
    flatMap { x ⇒
      f(x)
      Killer
    }
  }

  def until(f: A ⇒ Boolean): Rx[Unit] = {
    flatMap { x ⇒
      if (!f(x)) Killer
      else Dummy
    }
  }

  def map[B](f: A ⇒ B): Rx[B] = {
    flatMap(x ⇒ Val(f(x)))
  }

  def zip[B](wth: Rx[B]): Rx[(A, B)] = {
    flatMap { a ⇒
      wth flatMap { b ⇒
        Val((a, b))
      }
    }
  }

  def filter(f: A ⇒ Boolean): Rx[A] = flatMap { x ⇒
    if (f(x)) Val(x)
    else Dummy
  }

  @inline def filterWith(f: A ⇒ Boolean): Rx[A] = filter(f)

  def drop(num: Int): Rx[A] = {
    var drops = 0
    flatMap { x ⇒
      if (drops < num) {
        drops += 1
        Dummy
      }
      else {
        Val(x)
      }
    }
  }
  
  @deprecated("Use foreach() instead subscribe()", "0.4.0")
  def subscribe[U](f: A ⇒ U): Rx[Unit] = foreach(f)

  @deprecated("Use foreach() instead observe()", "0.4.0")
  def observe[U](f: ⇒ U): Rx[Unit] = foreach(_ ⇒ f)
}

sealed trait Source[A] extends Rx[A] {

  private[rx] var bindings: List[WeakReference[Binding[A, _]]] = Nil

  def update(v: A): Unit = {
    bindings foreach { x ⇒
      x.get match {
        case Some(f) ⇒ f.run(v)
        case None ⇒
      }
    }
    bindings = bindings filter { x ⇒
      x.get match {
        case Some(_) ⇒ true
        case None ⇒ false
      }
    }
  }

  @deprecated("Use update instead emit", "0.4.0")
  def emit(v: A): Unit = {
    update(v)
  }
  
  def pull(rx: Rx[A]) = rx.foreach(update)
  
  def flatMap[B](f: A ⇒ Rx[B]): Rx[B]

  def kill() = {
    bindings = Nil
  }
}

object Channel {
  def signal() = new Channel[Unit]() {
    def fire() = update(())
  }
  def apply[T]() = new Channel[T]()
}

sealed class Channel[A]() extends Source[A] {
  
  def flatMap[B](f: (A) => Rx[B]): Rx[B] = new Binding(this, f)
}

final case class Var[A](private[rx] var x: A) extends Source[A] {

  override def update(v: A) = {
    x = v
    super.update(v)
  }

  @deprecated("Use foreach() instead subscribe(). Note that foreach calls `f` immediately", "0.4.0")
  override def subscribe[U](f: (A) => U): Rx[Unit] = drop(1).foreach(f)

  override def flatMap[B](f: (A) => Rx[B]): Rx[B] = {
    new StatefulBinding(Some(x), this, f)
  }
}

final case class Val[+A](x: A) extends Rx[A] {

  def flatMap[B](f: (A) => Rx[B]): Rx[B] = f(x)

  def kill(): Unit = ()
}

case object Dummy extends Rx[Nothing] {
  def flatMap[B](f: Nothing ⇒ Rx[B]): Rx[B] = Dummy
  def kill() = ()
}

private[rx] case object Killer extends Rx[Nothing] {
  def flatMap[B](f: Nothing ⇒ Rx[B]): Rx[B] = Dummy
  def kill() = ()
}

private[rx] class Binding[From, To](parent: Source[From],
                                     lambda: From ⇒ Rx[To]) extends Source[To] {

  var victims: List[Mortal] = Nil

  def valueHook(x: To) = ()

  val run = {
    lambda andThen { x ⇒
      victims.foreach(_.kill())
      victims = Nil
      x
    } andThen {
      case Val(x) ⇒
        update(x)
        valueHook(x)
      case source: Source[To] ⇒
        victims ::= source.foreach { x ⇒
          update(x)
          valueHook(x)
        }
      case Killer ⇒
        kill()
        println(s"Killer this = $this, parent = $parent, bindings = ${parent.bindings}")
      case Dummy ⇒ // Do nothing
    }
  }

  val ref = WeakReference(this)
  parent.bindings ::= ref

  override def kill(): Unit = {
    super.kill()
    victims.foreach(_.kill())
    parent.bindings = parent.bindings.filter(_ != ref)
  }

  def flatMap[B](f: (To) => Rx[B]): Rx[B] = new Binding(this, f)
}

private[rx] class StatefulBinding[From, To](initialValue: Option[From],
                                             parent: Source[From],
                                             lambda: From ⇒ Rx[To])
  extends Binding[From, To](parent, lambda) {

  var state: Option[To] = None

  override def valueHook(x: To): Unit = {
    state = Some(x)
  }

  override def flatMap[B](f: (To) => Rx[B]): Rx[B] = {
    new StatefulBinding(state, this, f)
  }

  override def subscribe[U](f: (To) => U): Rx[Unit] = drop(1).foreach(f)

  initialValue foreach { x ⇒
    run(x)
  }
}
