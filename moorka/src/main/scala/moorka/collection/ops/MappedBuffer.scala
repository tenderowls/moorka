package moorka.collection.ops

import moorka._
import moorka.collection.BufferView

import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[collection] class MappedBuffer[From, A](parent: BufferView[From],
                                                mapFunction: From => A)
  extends BufferView[A] {

  val buffer = mutable.Buffer[Option[A]]()
  var handlers = List.empty[Rx[_]]

  0 until parent.length foreach {
    i => buffer += None
  }

  // Mapped events
  val added = parent.added.map(mapFunction)
  val removed = parent.removed.map { x => x.copy(e = apply(x.idx)) }
  val inserted = parent.inserted.map { x => x.copy(e = mapFunction(x.e)) }
  val updated = parent.updated.map { x ⇒ x.copy(e = mapFunction(x.e), prevE = this(x.idx)) }

  private[this] val privateLength = Var(buffer.length)

  val rxLength: Rx[Int] = privateLength

  def length: Int = privateLength.x

  handlers ::= added foreach { x =>
    buffer += Some(x)
    privateLength() = buffer.length
  }
  handlers ::= removed foreach { x =>
    buffer.remove(x.idx)
    privateLength() = buffer.length
  }
  handlers ::= inserted foreach { x =>
    buffer.insert(x.idx, Some(x.e))
    privateLength() = buffer.length
  }
  handlers ::= updated foreach { x =>
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
    for (i <- 0 until privateLength.x) {
      if (apply(i) == e)
        return i
    }
    -1
  }

  override def kill(): Unit = {
    super.kill()
    handlers.foreach(_.kill())
  }
}
