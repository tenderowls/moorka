package com.tenderowls.moorka.core.collection

import com.tenderowls.moorka.core._

import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[collection] class Filtered[A](parent: CollectionView[A],
                                      filterFunction: (A) => Boolean)

  extends CollectionBase[A] {

  type RxExtractor = (A) => RxState[Any]

  val buffer = mutable.Buffer[A]()
  val origBuffer = mutable.Buffer[A]()

  def refreshBuffer(): Unit = {
    buffer.remove(0, buffer.length)
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
        val e = buffer(idx)
        buffer.remove(idx, 1)
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

  private val _length = Var(buffer.length)

  val length: RxState[Int] = _length

  // Copy collection to internal buffer
  // filtered with `filterFunction`
  parent foreach { e =>
    origBuffer += e
    if (filterFunction(e)) {
      buffer += e
      _length() = buffer.length
    }
  }

  parent.added subscribe { e =>
    origBuffer += e
    if (filterFunction(e)) {
      buffer += e
      _length() = buffer.length
      added.emit(e)
    }
  }

  parent.removed subscribe { x =>
    origBuffer.remove(x.idx, 1)
    if (filterFunction(x.e)) {
      val idx = buffer.indexOf(x.e)
      buffer.remove(idx, 1)
      _length() = buffer.length
      removed.emit(IndexedElement(idx, x.e))
    }
  }

  parent.inserted subscribe { x =>
    origBuffer.insert(x.idx, x.e)
    refreshElement(x.e)
  }

  parent.updated subscribe { x =>
    origBuffer(x.idx) = x.e
    refreshElement(x.e)
  }

  override def kill(): Unit = {
    super.kill()
  }

  def indexOf(e: A) = buffer.indexOf(e)

  def apply(idx: Int) = buffer(idx)
}
