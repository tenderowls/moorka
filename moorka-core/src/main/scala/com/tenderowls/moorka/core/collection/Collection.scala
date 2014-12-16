package com.tenderowls.moorka.core.collection

import com.tenderowls.moorka.core._
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

  val added = Emitter[A]

  val inserted = Emitter[IndexedElement[A]]

  val updated = Emitter[IndexedElement[A]]

  val removed = Emitter[IndexedElement[A]]

  private val _length = Var(buffer.length)

  val length: RxState[Int] = _length

  def apply(x: Int) = buffer(x)

  def indexOf(e: A) = buffer.indexOf(e)

  def view: CollectionView[A] = this

  def +=(e: A) = {
    buffer.push(e)
    _length() = buffer.length
    added.emit(e)
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
    _length() = buffer.length
    removed.emit(tpl)
    e
  }

  def remove(f: (A) => Boolean) = {
    val removedElems = new js.Array[IndexedElement[A]]
    var offset = 0
    for (i <- 0 until buffer.length) {
      val j = i - offset
      val x: js.UndefOr[A] = buffer(j)
      if (x.isDefined && !f(x.get)) {
        removedElems(removedElems.length) = IndexedElement(j, x.get)
        buffer.splice(j, 1)
        offset += 1
      }
    }
    _length() = buffer.length
    for (x <- removedElems) {
      removed.emit(x)
    }
  }

  def insert(idx: Int, e: A) = {
    buffer.splice(idx, 0, e)
    _length() = buffer.length
    inserted.emit(IndexedElement(idx, e))
  }

  def update(idx: Int, e: A): Unit = {
    buffer(idx) = e
    _length() = buffer.length
    updated.emit(IndexedElement(idx, e))
  }

  def updateElement(e: A, to: A): Unit = {
    val idx = indexOf(e)
    update(idx, to)
  }

  def updateAll(f: A => A) = {
    val elements = asSeq
    for (i <- 0 until length()) {
      val e = elements(i)
      update(i, f(e))
    }
  }
}