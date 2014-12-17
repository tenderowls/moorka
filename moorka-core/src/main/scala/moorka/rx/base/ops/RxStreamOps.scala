package moorka.rx.base.ops

import moorka.rx._
import scala.language.existentials

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class RxStreamOps[A](val self: Channel[A]) extends AnyVal {

  def until(f: A => Boolean): Channel[A] = {
    val child = new Channel[A] {
      override def kill(): Unit = {
        self.unlinkChild(this)
        super.kill()
      }
      override def emit(x: A): Unit = {
        super.emit(x)
        if (!f(x)) kill()
      }
    }
    self.linkChild(child)
    child
  }

  def subscribe(f: A => Any): Channel[A] = {
    val child = new Channel[A] {
      override def kill(): Unit = {
        self.unlinkChild(this)
        super.kill()
      }
      override def emit(x: A): Unit = {
        super.emit(x)
        f(x)
      }
    }
    self.linkChild(child)
    child
  }

  def filter(f: A => Boolean): Channel[A] = {
    val child = new Channel[A] {
      override def kill(): Unit = {
        self.unlinkChild(this)
        super.kill()
      }
      override def emit(x: A): Unit = {
        if (f(x)) super.emit(x)
      }
    }
    self.linkChild(child)
    child
  }

  def map[B](f: A => B): Channel[B] = {
    new Channel[B] {
      val rip = self subscribe { x =>
        emit(f(x))
      }
      override def kill(): Unit = {
        rip.kill()
        super.kill()
      }
    }
  }

  def merge(one: Channel[_]): Channel[Any] = {
    new Channel[Any] {
      val rip = Seq(
        one.subscribe(emit),
        self.subscribe(emit)
      )
      override def kill(): Unit = {
        rip.foreach(_.kill())
        super.kill()
      }
    }
  }
}
