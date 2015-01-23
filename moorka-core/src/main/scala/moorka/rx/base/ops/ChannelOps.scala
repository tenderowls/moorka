package moorka.rx.base.ops

import moorka.rx._

import scala.language.existentials

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class ChannelOps[A](val self: Channel[A]) extends AnyVal {

  def until(f: A => Boolean)(implicit reaper: Reaper = Reaper.nice): Channel[A] = {
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
    reaper.mark(child)
    child
  }

  def listen(f: => Any)(implicit reaper: Reaper = Reaper.nice): Channel[A] = {
    subscribe(_ => f)
  }

  def subscribe(f: A => Any)(implicit reaper: Reaper = Reaper.nice): Channel[A] = {
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
    reaper.mark(child)
    child
  }

  def filter(f: A => Boolean)(implicit reaper: Reaper = Reaper.nice): Channel[A] = {
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
    reaper.mark(child)
    child
  }

  def map[B](f: A => B)(implicit reaper: Reaper = Reaper.nice): Channel[B] = {
    val child = new Channel[B] {
      val rip = self subscribe { x =>
        emit(f(x))
      }
      override def kill(): Unit = {
        rip.kill()
        super.kill()
      }
    }
    reaper.mark(child)
    child
  }

  def merge(one: Channel[_])(implicit reaper: Reaper = Reaper.nice): Channel[Any] = {
    val child = new Channel[Any] {
      val rip = Seq(
        one.subscribe(emit),
        self.subscribe(emit)
      )
      override def kill(): Unit = {
        rip.foreach(_.kill())
        super.kill()
      }
    }
    reaper.mark(child)
    child
  }

  def flatMap[B](f: (A) => Channel[B])(implicit reaper: Reaper = Reaper.nice): Channel[B] = {
    val child = new Channel[B] {
      self subscribe { x =>
        f(x)
        
      }
      
    }
    reaper.mark(child)
    child
  }
  
  def persist()(implicit reaper: Reaper = Reaper.nice): State[Option[A]] = {
    val child = new Var[Option[A]](None) {
      val rip = self subscribe { value =>
        update(Some(value))
      }
      override def kill(): Unit = {
        rip.kill()
        super.kill()
      }
    }
    reaper.mark(child)
    child
  }
}
