package felix.collection.ops

import felix.collection.{BufferView, IndexedElement, UpdatedIndexedElement}
import moorka.death.Reaper
import moorka.flow.mutable.{Channel, Var}
import moorka.flow.{Context, Flow, Sink}

import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[collection] class FilteredBuffer[A](parent: BufferView[A], filterFunction: (A) => Boolean)
  (implicit context: Context) extends BufferView[A] {

  val buffer = mutable.Buffer[A]()
  val origBuffer = mutable.Buffer[A]()
  implicit val reaper = Reaper()
  
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
        removed.push(IndexedElement(idx, e))
      }
    }
    else {
      if (filterFunction(x)) {
        refreshBuffer()
        inserted.push(IndexedElement(buffer.indexOf(x), x))
      }
    }
  }

  // Overridden events
  val added = Channel[A]
  val removed = Channel[IndexedElement[A]]
  val inserted = Channel[IndexedElement[A]]
  val updated = Channel[UpdatedIndexedElement[A]]

  private[this] val privateLength = Var(buffer.length)

  val rxLength: Flow[Int] = privateLength

  def length: Int = privateLength.data.head

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

  parent.added foreach { e =>
    origBuffer += e
    if (filterFunction(e)) {
      buffer += e
      privateLength() = buffer.length
      added.push(e)
    }
  }

  parent.removed foreach { x =>
    origBuffer.remove(x.idx, 1)
    if (filterFunction(x.e)) {
      val idx = buffer.indexOf(x.e)
      buffer.remove(idx, 1)
      privateLength() = buffer.length
      removed.push(IndexedElement(idx, x.e))
    }
  }

  parent.inserted foreach { x =>
    origBuffer.insert(x.idx, x.e)
    refreshElement(x.e)
  }

  parent.updated foreach { x =>
    origBuffer(x.idx) = x.e
    refreshElement(x.e)
  }

  def kill(): Unit = {
    reaper.sweep()
  }

  def indexOf(e: A): Int = buffer.indexOf(e)

  def apply(idx: Int): A = buffer(idx)
}
