package com.tenderowls.moorka.core.collection

import com.tenderowls.moorka.core.Emitter

import scala.scalajs.js

/**
 * Default reactive collection implementation. Events will be triggered
 * for all changes which you would make.
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object Collection {

  def apply[A](es: A*): Collection[A] = {
    fromSeq(es)
  }

  def fromSeq[A](es: Seq[A]): Collection[A] = {
    val array = new js.Array[A](es.length)
    for (i <- 0 until es.length)
      array(i) = es(i)
    new Collection[A](array)
  }

  def apply[A] = new Collection[A](new js.Array[A])
}

class Collection[A](private var buffer: js.Array[A]) extends CollectionBase[A] {

  private val _added = Emitter[A]

  private val _inserted = Emitter[IndexedElement[A]]

  private val _updated = Emitter[IndexedElement[A]]

  private val _removed = Emitter[IndexedElement[A]]

  val added = _added.view

  val inserted = _inserted.view

  val updated = _updated.view

  val removed = _removed.view

  def apply(x: Int) = buffer(x)

  def length: Int = buffer.length

  def indexOf(e: A) = buffer.indexOf(e)

  def view: CollectionView[A] = this

  def +=(e: A) = {
    buffer.push(e)
    _added.emit(e)
    this
  }

  def -=(e: A) = {
    val idx = indexOf(e)
    remove(idx)
    this
  }

  def remove(idx: Int) = {
    val e = buffer.splice(idx, 1)(0)
    val tpl = IndexedElement(idx, e)
    _removed.emit(tpl)
    e
  }

  def remove(f: (A) => Boolean) = {
    val removed = new js.Array[IndexedElement[A]]
    var offset = 0
    for (i <- 0 until buffer.length) {
      val j = i - offset
      val x: js.UndefOr[A] = buffer(j)
      if (x.isDefined && !f(x.get)) {
        removed(removed.length) = IndexedElement(j, x.get)
        buffer.splice(j, 1)
        offset += 1
      }
    }
    for (x <- removed) {
      _removed.emit(x)
    }
  }

  def insert(idx: Int, e: A) = {
    buffer.splice(idx, 0, e)
    _inserted.emit(IndexedElement(idx, e))
  }

  def update(idx: Int, e: A) = {
    buffer(idx) = e
    _updated.emit(IndexedElement(idx, e))
  }
}