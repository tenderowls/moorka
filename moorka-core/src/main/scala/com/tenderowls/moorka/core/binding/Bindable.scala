package com.tenderowls.moorka.core.binding

import com.tenderowls.moorka.core.events.Event

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Bindable[A] extends Event[Bindable[A]] {

  def apply(): A

  def map[B](f: (A) => B): Bindable[B]

  def observe(f: (Bindable[A]) => Unit): Event[Bindable[A]]
}