package com.tenderowls.moorka.core.rx.ops

import com.tenderowls.moorka.core._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class RxStreamOps[A](val self: RxStream[A]) extends AnyVal {

  def until(f: A => Boolean): RxStream[A] = {
    val child = new Emitter[A] {
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

  def subscribe(f: A => Any): RxStream[A] = {
    val child = new Emitter[A] {
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

  def filter(f: A => Boolean): RxStream[A] = {
    val child = new Emitter[A] {
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

  def map[B](f: A => B): RxStream[B] = {
    new Emitter[B] {
      val rip = self subscribe { x =>
        emit(f(x))
      }
      override def kill(): Unit = {
        rip.kill()
        super.kill()
      }
    }
  }
}
