package moorka.rx.collection

import moorka.rx._

import scala.collection.mutable

/**
 * Default reactive collection implementation. Events will be triggered
 * for all changes which you would make.
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object Buffer {

  def apply[A](es: A*)(implicit reaper: Reaper = Reaper.nice): Buffer[A] = {
    fromSeq(es)
  }

  def fromSeq[A](es: Seq[A])(implicit reaper: Reaper = Reaper.nice): Buffer[A] = {
    val buff = new Buffer[A](mutable.Buffer.concat(es))
    reaper.mark(buff)
    buff
  }
}

class Buffer[A](private[this] var buffer: mutable.Buffer[A]) extends BufferView[A] {

  val added = Channel[A]

  val inserted = Channel[IndexedElement[A]]

  val updated = Channel[UpdatedIndexedElement[A]]

  val removed = Channel[IndexedElement[A]]

  private[this] val privateLength = Var(buffer.length)

  val rxLength: Rx[Int] = privateLength

  def length: Int = privateLength.x

  def apply(x: Int): A = buffer(x)

  def indexOf(e: A): Int = buffer.indexOf(e)

  def view: BufferView[A] = this

  def +=(e: A): Buffer[A] = {
    buffer += e
    privateLength() = buffer.length
    added.update(e)
    this
  }

  def -=(e: A): Buffer[A] = {
    val idx = indexOf(e)
    remove(idx)
    this
  }

  def remove(idx: Int): A = {
    val e = buffer(idx)
    buffer.remove(idx)
    val tpl = IndexedElement(idx, e)
    privateLength() = buffer.length
    removed.update(tpl)
    e
  }

  def remove(f: (A) => Boolean): Unit = {
    val removedElems = mutable.Buffer[IndexedElement[A]]()
    var offset = 0
    for (i <- buffer.indices) {
      val j = i - offset
      val x = buffer(j)
      if (!f(x)) {
        removedElems += IndexedElement(j, x)
        buffer.remove(j)
        offset += 1
      }
    }
    privateLength() = buffer.length
    for (x <- removedElems) {
      removed.update(x)
    }
  }

  def insert(idx: Int, e: A): Unit = {
    buffer.insert(idx, e)
    privateLength() = buffer.length
    inserted.update(IndexedElement(idx, e))
  }

  def update(idx: Int, e: A): Unit = {
    if (buffer(idx) != e) {
      val prevE = buffer(idx)
      buffer(idx) = e
      privateLength() = buffer.length
      updated.update(UpdatedIndexedElement(idx, e, prevE))
    }
  }

  def updateElement(e: A, to: A): Unit = {
    val idx = indexOf(e)
    update(idx, to)
  }

  def updateAll(f: A => A): Unit = {
    val elements = this.toSeq
    for (i <- 0 until privateLength.x) {
      val e = elements(i)
      update(i, f(e))
    }
  }
}
