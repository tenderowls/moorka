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

  type RxExtractor = (A) => State[Any]

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
  val added = Channel[A]
  val removed = Channel[IndexedElement[A]]
  val inserted = Channel[IndexedElement[A]]
  val updated = Channel[UpdatedIndexedElement[A]]

  private val _length = Var(buffer.length)

  val length: State[Int] = _length

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
