package felix.collection.ops

import felix.collection.BufferView
import moorka.death.Reaper
import moorka.flow.mutable.Var
import moorka.flow.{Context, Flow}

import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[collection] class MappedBuffer[From, A](parent: BufferView[From],
                                                mapFunction: From => A)
  (implicit context: Context) extends BufferView[A] {

  val buffer = mutable.Buffer[Option[A]]()
  implicit val reaper = Reaper()

  0 until parent.length foreach {
    i => buffer += None
  }

  // Mapped events
  val added = parent.added.map(mapFunction)
  val removed = parent.removed.map { x => x.copy(e = apply(x.idx)) }
  val inserted = parent.inserted.map { x => x.copy(e = mapFunction(x.e)) }
  val updated = parent.updated.map { x ⇒ x.copy(e = mapFunction(x.e), prevE = this(x.idx)) }

  private[this] val privateLength = Var(buffer.length)

  val rxLength: Flow[Int] = privateLength

  def length: Int = privateLength.data.head

  added foreach { x =>
    buffer += Some(x)
    privateLength() = buffer.length
  }
  removed foreach { x =>
    buffer.remove(x.idx)
    privateLength() = buffer.length
  }
  inserted foreach { x =>
    buffer.insert(x.idx, Some(x.e))
    privateLength() = buffer.length
  }
  updated foreach { x =>
    buffer(x.idx) = Some(x.e)
  }

  def apply(idx: Int): A = {
    buffer(idx) match {
      case Some(x) => x
      case None =>
        val e = mapFunction(parent(idx))
        buffer(idx) = Some(e)
        e
    }
  }

  def indexOf(e: A): Int = {
    for (i <- buffer.indices) {
      if (apply(i) == e)
        return i
    }
    -1
  }

  def kill(): Unit = {
    reaper.sweep()
  }
}
