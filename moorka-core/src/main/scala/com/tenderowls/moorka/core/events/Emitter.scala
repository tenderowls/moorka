package com.tenderowls.moorka.core.events

import scala.scalajs.js

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object Emitter {
  def apply[A] = new Emitter[A]
}

class Emitter[A] extends Event[A] { self =>

  private val slots = new js.Array[Emitter[A]]

  def until(f: A => Boolean): Event[A] = {
    val slot = new Emitter[A] {
      override def kill(): Unit = {
        self.slots.splice(self.slots.indexOf(this), 1)
        super.kill()
      }
      override def emit(x: A): Unit = {
        super.emit(x)
        if (!f(x)) kill()
      }
    }
    slots.push(slot)
    slot
  }
  
  def subscribe(f: A => Unit): Event[A] = {
    val slot = new Emitter[A] {
      override def kill(): Unit = {
        self.slots.splice(self.slots.indexOf(this), 1)
        super.kill()
      }
      override def emit(x: A): Unit = {
        super.emit(x)
        f(x)
      }
    }
    slots.push(slot)
    slot
  }

  override def filter(f: A => Boolean): Event[A] = {
    val slot = new Emitter[A] {
      override def kill(): Unit = {
        self.slots.splice(self.slots.indexOf(this), 1)
        super.kill()
      }
      override def emit(x: A): Unit = {
        if (f(x)) super.emit(x)
      }
    }
    slots.push(slot)
    slot
  }

  override def map[B](f: A => B): Event[B] = {
    val subscription = new Emitter[A] { subscriptionSelf =>
      val mapped = new Emitter[B] {
        override def kill(): Unit = {
          self.slots.splice(self.slots.indexOf(subscriptionSelf), 1)
          super.kill()
        }
      }
      override def emit(x: A): Unit = {
        mapped.emit(f(x))
      }
    }
    slots.push(subscription)
    subscription.mapped
  }

  def emit(x: A): Unit = {
    for (slot <- slots)
      slot.emit(x)
  }

  def view:Event[A] = this

  override def kill(): Unit = {
    slots.foreach(_.kill())
    slots.splice(0)
  }
}