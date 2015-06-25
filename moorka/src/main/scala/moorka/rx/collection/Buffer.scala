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

class Buffer[A](private var buffer: mutable.Buffer[A]) extends BufferView[A] {

  val added = Channel[A]

  val inserted = Channel[IndexedElement[A]]

  val updated = Channel[UpdatedIndexedElement[A]]

  val removed = Channel[IndexedElement[A]]

  private val _length = Var(buffer.length)

  val rxLength: Rx[Int] = _length

  def length: Int = _length.x

  def apply(x: Int) = buffer(x)

  def indexOf(e: A) = buffer.indexOf(e)

  def view: BufferView[A] = this

  def +=(e: A) = {
    buffer += e
    _length() = buffer.length
    added.update(e)
    this
  }

  def -=(e: A) = {
    val idx = indexOf(e)
    remove(idx)
    this
  }

  def remove(idx: Int) = {
    val e = buffer(idx)
    buffer.remove(idx)
    val tpl = IndexedElement(idx, e)
    _length() = buffer.length
    removed.update(tpl)
    e
  }

  def remove(f: (A) => Boolean) = {
    val removedElems = mutable.Buffer[IndexedElement[A]]()
    var offset = 0
    for (i <- 0 until buffer.length) {
      val j = i - offset
      removedElems += IndexedElement(j, buffer(i))
      buffer.remove(j)
      offset += 1
    }
    _length() = buffer.length
    for (x <- removedElems) {
      removed.update(x)
    }
  }

  def insert(idx: Int, e: A) = {
    buffer.insert(idx, e)
    _length() = buffer.length
    inserted.update(IndexedElement(idx, e))
  }

  def update(idx: Int, e: A): Unit = {
    if (buffer(idx) != e) {
      val prevE = buffer(idx)
      buffer(idx) = e
      _length() = buffer.length
      updated.update(UpdatedIndexedElement(idx, e, prevE))
    }
  }

  def updateElement(e: A, to: A): Unit = {
    val idx = indexOf(e)
    update(idx, to)
  }

  def updateAll(f: A => A) = {
    val elements = this.asSeq
    for (i <- 0 until _length.x) {
      val e = elements(i)
      update(i, f(e))
    }
  }
}
