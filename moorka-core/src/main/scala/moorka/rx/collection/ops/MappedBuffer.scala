package moorka.rx.collection.ops

import moorka.rx._
import moorka.rx.collection.BufferView

import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[collection] class MappedBuffer[From, A](parent: BufferView[From],
                                          mapFunction: From => A)
  extends BufferView[A] {

  val buffer = mutable.Buffer[Option[A]]()

  0 until parent.length() foreach {
    i => buffer += None
  }

  // Mapped events
  val added = parent.added.map(mapFunction)
  val removed = parent.removed.map { x => x.copy(e = apply(x.idx)) }
  val inserted = parent.inserted.map { x => x.copy(e = mapFunction(x.e)) }
  val updated = parent.updated.map { x => x.copy(e = mapFunction(x.e)) }

  private val _length = Var(buffer.length)

  val length: State[Int] = _length

  added.subscribe { x =>
    buffer += Some(x)
    _length() = buffer.length
  }
  removed.subscribe {  x =>
    buffer.remove(x.idx)
    _length() = buffer.length
  }
  inserted.subscribe { x =>
    buffer.insert(x.idx, Some(x.e))
    _length() = buffer.length
  }
  updated.subscribe { x =>
    buffer(x.idx) = Some(x.e)
  }

  def apply(idx: Int) = {
    buffer(idx) match {
      case Some(x) => x
      case None =>
        val e = mapFunction(parent(idx))
        buffer(idx) = Some(e)
        e
    }
  }

  def indexOf(e: A): Int = {
    for (i <- 0 until length()) {
      if (apply(i) == e)
        return i
    }
    -1
  }
}
