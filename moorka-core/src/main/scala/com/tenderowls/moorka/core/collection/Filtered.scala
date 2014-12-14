package com.tenderowls.moorka.core.collection

import com.tenderowls.moorka.core._

import scala.scalajs.js

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[collection] class Filtered[A](parent: CollectionView[A],
                                      filterFunction: (A) => Boolean)

  extends CollectionBase[A] {

  type RxExtractor = (A) => RxState[Any]

  val buffer = new js.Array[A]
  val origBuffer = new js.Array[A]

  def refreshBuffer(): Unit = {
    buffer.splice(0)
    for (e <- origBuffer) {
      if (filterFunction(e)) {
        buffer(buffer.length) = e
      }
    }
  }

  def refreshElement(x: A) = {
    // todo optimize
    val idx = buffer.indexOf(x)
    if (idx > -1) {
      if (origBuffer.indexOf(x) < 0 || !filterFunction(x)) {
        val e = buffer.splice(idx, 1)(0)
        removed.emit(IndexedElement(idx, e))
      }
    }
    else {
      if (filterFunction(x)) {
        refreshBuffer()
        inserted.emit(IndexedElement(buffer.indexOf(x), x))
      }
    }
  }

  // Overridden events
  val added = Emitter[A]
  val removed = Emitter[IndexedElement[A]]
  val inserted = Emitter[IndexedElement[A]]
  val updated = Emitter[IndexedElement[A]]

  // Copy collection to internal buffer
  // filtered with `filterFunction`
  parent foreach { e =>
    origBuffer(origBuffer.length) = e
    if (filterFunction(e)) {
      buffer(buffer.length) = e
    }
  }

  parent.added subscribe { e =>
    origBuffer(origBuffer.length) = e
    if (filterFunction(e)) {
      buffer(buffer.length) = e
      added.emit(e)
    }
  }

  parent.removed subscribe { x =>
    origBuffer.splice(x.idx, 1)
    if (filterFunction(x.e)) {
      val idx = buffer.indexOf(x.e)
      buffer.splice(idx, 1)
      removed.emit(IndexedElement(idx, x.e))
    }
  }

  parent.inserted subscribe { x =>
    origBuffer.splice(x.idx, 0, x.e)
    refreshElement(x.e)
  }

  parent.updated subscribe { x =>
    origBuffer(x.idx) = x.e
    refreshElement(x.e)
  }

  override def kill(): Unit = {
    super.kill()
  }

  def length: Int = buffer.length

  def indexOf(e: A) = buffer.indexOf(e)

  def apply(idx: Int) = buffer(idx)
}
