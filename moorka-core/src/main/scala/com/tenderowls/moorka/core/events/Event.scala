package com.tenderowls.moorka.core.events

import com.tenderowls.moorka.core.Mortal

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Event[A] extends Mortal {

  def subscribe(f: (A) => Unit): Event[A]

  def until(f: (A) => Boolean): Event[A]

  def filter(f: (A) => Boolean): Event[A]

  def map[B](f: (A) => B): Event[B]
}
