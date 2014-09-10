package com.tenderowls.moorka.core.collection

import com.tenderowls.moorka.core.binding.Bindable

import scala.scalajs.js

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[collection] abstract class CollectionBase[A] extends CollectionView[A] {

  def map[B](f: (A) => B): CollectionView[B] = {
    new Mapped[A, B](this, f)
  }

  def filter(f: (A) => Boolean): CollectionView[A] = {
    new Filtered[A](this, f)
  }

  def foreach(f: (A) => Unit): Unit = {
    // todo optimize
    val l = length
    val range = 0 until l
    val a = new js.Array[A](l)

    for (i <- range)
      a(i) = apply(i)

    for (i <- range) {
      val e = a(i)
      f(e)
    }
  }

  def count(f: (A) => Boolean): Int = {
    var count = 0
    for (i <- 0 until length) {
      val e = apply(i)
      if (f(e)) count += 1
    }
    count
  }

  def filter(f: Bindable[(A) => Boolean]): CollectionView[A] = {
    new FilteredWithReactiveFilter(this, f)
  }

  def asSeq: Seq[A] = {
    for (i <- 0 until length) yield apply(i)
  }

  def observe(f: (A) => Bindable[Any]): CollectionView[A] = this

  def kill(): Unit = {
    added.kill()
    removed.kill()
    inserted.kill()
    updated.kill()
  }

  override def toString = {
    // Force elements (for non-strict collection)
    val values = for (x <- 0 until length) yield apply(x).toString
    s"Collection(${values.reduce(_ + ", " + _)})"
  }
}
