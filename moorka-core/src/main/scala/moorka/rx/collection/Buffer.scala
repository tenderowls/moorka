package moorka.rx.collection

import moorka.rx._

import scala.collection.mutable
import scala.scalajs.js

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

  val updated = Channel[IndexedElement[A]]

  val removed = Channel[IndexedElement[A]]

  private val _length = Var(buffer.length)

  val length: State[Int] = _length

  def apply(x: Int) = buffer(x)

  def indexOf(e: A) = buffer.indexOf(e)

  def view: BufferView[A] = this

  def +=(e: A) = {
    buffer += e
    _length() = buffer.length
    added.emit(e)
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
    removed.emit(tpl)
    e
  }

  def remove(f: (A) => Boolean) = {
    val removedElems = new js.Array[IndexedElement[A]]
    var offset = 0
    for (i <- 0 until buffer.length) {
      val j = i - offset
      val x: js.UndefOr[A] = buffer(j)
      if (x.isDefined && !f(x.get)) {
        removedElems(removedElems.length) = IndexedElement(j, x.get)
        buffer.remove(j)
        offset += 1
      }
    }
    _length() = buffer.length
    for (x <- removedElems) {
      removed.emit(x)
    }
  }

  def insert(idx: Int, e: A) = {
    buffer.insert(idx, e)
    _length() = buffer.length
    inserted.emit(IndexedElement(idx, e))
  }

  def update(idx: Int, e: A): Unit = {
    buffer(idx) = e
    _length() = buffer.length
    updated.emit(IndexedElement(idx, e))
  }

  def updateElement(e: A, to: A): Unit = {
    val idx = indexOf(e)
    update(idx, to)
  }

  def updateAll(f: A => A) = {
    val elements = this.asSeq
    for (i <- 0 until length()) {
      val e = elements(i)
      update(i, f(e))
    }
  }
}
