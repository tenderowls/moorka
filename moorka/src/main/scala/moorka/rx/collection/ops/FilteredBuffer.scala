package moorka.rx.collection.ops

import moorka.rx._
import moorka.rx.collection.{UpdatedIndexedElement, BufferView, IndexedElement}

import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[collection] class FilteredBuffer[A](parent: BufferView[A],
                                      filterFunction: (A) => Boolean)
  extends BufferView[A] {

  val buffer = mutable.Buffer[A]()
  val origBuffer = mutable.Buffer[A]()
  var handlers = List.empty[Rx[_]]
  
  def refreshBuffer(): Unit = {
    buffer.remove(0, buffer.length)
    for (e <- origBuffer) {
      if (filterFunction(e)) {
        buffer(buffer.length) = e
      }
    }
  }

  def refreshElement(x: A): Unit = {
    // todo optimize
    val idx = buffer.indexOf(x)
    if (idx > -1) {
      if (origBuffer.indexOf(x) < 0 || !filterFunction(x)) {
        val e = buffer(idx)
        buffer.remove(idx, 1)
        removed.update(IndexedElement(idx, e))
      }
    }
    else {
      if (filterFunction(x)) {
        refreshBuffer()
        inserted.update(IndexedElement(buffer.indexOf(x), x))
      }
    }
  }

  // Overridden events
  val added = Channel[A]
  val removed = Channel[IndexedElement[A]]
  val inserted = Channel[IndexedElement[A]]
  val updated = Channel[UpdatedIndexedElement[A]]

  private[this] val privateLength = Var(buffer.length)

  val rxLength: Rx[Int] = privateLength

  def length: Int = privateLength.x

  // Copy collection to internal buffer
  // filtered with `filterFunction`
  for (i ← 0 until parent.length) {
    val e = parent(i)
    origBuffer += e
    if (filterFunction(e)) {
      buffer += e
      privateLength() = buffer.length
    }
  }

  handlers ::= parent.added foreach { e =>
    origBuffer += e
    if (filterFunction(e)) {
      buffer += e
      privateLength() = buffer.length
      added.update(e)
    }
  }

  handlers ::= parent.removed foreach { x =>
    origBuffer.remove(x.idx, 1)
    if (filterFunction(x.e)) {
      val idx = buffer.indexOf(x.e)
      buffer.remove(idx, 1)
      privateLength() = buffer.length
      removed.update(IndexedElement(idx, x.e))
    }
  }

  handlers ::= parent.inserted foreach { x =>
    origBuffer.insert(x.idx, x.e)
    refreshElement(x.e)
  }

  handlers ::= parent.updated foreach { x =>
    origBuffer(x.idx) = x.e
    refreshElement(x.e)
  }

  override def kill(): Unit = {
    super.kill()
    handlers.foreach(_.kill())
  }

  def indexOf(e: A): Int = buffer.indexOf(e)

  def apply(idx: Int): A = buffer(idx)
}
