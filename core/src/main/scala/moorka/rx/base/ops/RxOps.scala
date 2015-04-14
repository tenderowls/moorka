package moorka.rx.base.ops

import moorka.rx.base._
import moorka.rx.death.Reaper

import scala.concurrent._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class RxOps[A](val self: Rx[A]) extends AnyVal {

  def until(f: A ⇒ Boolean): Rx[Unit] = {
    self >>= { x ⇒
      if (!f(x)) Killer
      else Dummy
    }
  }

  def zip[B](wth: Rx[B]): Rx[(A, B)] = {
    self >>= { a ⇒
      wth >>= { b ⇒
        Val((a, b))
      }
    }
  }

  def drop(num: Int): Rx[A] = {
    var drops = 0
    self >>= { x ⇒
      if (drops < num) {
        drops += 1
        Dummy
      }
      else {
        Val(x)
      }
    }
  }

  def take(num: Int): Rx[Seq[A]] = {
    val channel = Channel[Seq[A]]()
    val seq = collection.mutable.Buffer[A]()
    self foreach { value ⇒
      seq += value
      if (seq.length == num) {
        channel.update(Seq(seq: _*))
        seq.remove(0, seq.length)
      }
    }
    channel
  }

  def fold[B](z: B)(op: (B, A) => B): Rx[B] = {
    Var.withDefaultMod(z) { b =>
      self >>= { a =>
        Val(op(b, a))
      }
    }
  }

  def or[B](b: Rx[B]): Rx[Either[A, B]] = {
    val rx = Channel[Either[A, B]]()
    val left: Rx[Either[A, B]] = self >>= (x ⇒ Val(Left(x)))
    val right: Rx[Either[A, B]] = b >>= (x ⇒ Val(Right(x)))
    rx.pull(left)
    rx.pull(right)
    rx
  }

  def collect[B](pf: PartialFunction[A, B]): Rx[B] = {
    self >>= { x ⇒
      if (pf.isDefinedAt(x)) {
        Val(pf(x))
      }
      else {
        Dummy
      }
    }
  }

  def stateful: Rx[Option[A]] = {
    val state = Var[Option[A]](None)
    state.pull(self.map(Some(_)))
    state
  }

  def switch[L, R](f: A ⇒ Either[L, R]): (Rx[L], Rx[R]) = {
    val left = Channel[L]()
    val right = Channel[R]()
    val switcher = self map f
    left.pull {
      new RxOps(switcher) collect {
        case Left(x) ⇒ x
      }
    }
    right.pull {
      new RxOps(switcher) collect {
        case Right(x) ⇒ x
      }
    }
    (left, right)
  }

  def partition(f: A ⇒ Boolean): (Rx[A], Rx[A]) = {
    val left = Channel[A]()
    val right = Channel[A]()
    val switcher = zip(self.map(f))
    left.pull {
      new RxOps(switcher) collect {
        case (x, true) ⇒ x
      }
    }
    right.pull {
      new RxOps(switcher) collect {
        case (x, false) ⇒ x
      }
    }
    (left, right)
  }

  def toFuture: Future[A] = new FutureRx(self)

  def mark(implicit reaper: Reaper) = reaper.mark(self)
}
