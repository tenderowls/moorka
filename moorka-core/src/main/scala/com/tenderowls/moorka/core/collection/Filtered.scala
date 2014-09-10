package com.tenderowls.moorka.core.collection

import com.tenderowls.moorka.core.{Bindable, Emitter, Event}

import scala.collection.mutable
import scala.scalajs.js

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[collection] class Filtered[A](parent: CollectionView[A],
                                      var filterFunction: (A) => Boolean)

  extends CollectionBase[A] {

  type RxExtractor = (A) => Bindable[Any]

  val buffer = new js.Array[A]
  val origBuffer = new js.Array[A]
  val observers = new mutable.HashMap[A, Event[_]]()
  var _observe: Option[RxExtractor] = scala.None

  def refreshBuffer(): Unit = {
    buffer.splice(0)
    for (e <- origBuffer) {
      if (filterFunction(e)) {
        buffer(buffer.length) = e
      }
      addObserverFor(e)
    }
  }

  def refreshElement(x: A) = {
    // todo optimize
    val idx = buffer.indexOf(x)
    if (idx > -1) {
      if (origBuffer.indexOf(x) < 0 || !filterFunction(x)) {
        val e = buffer.splice(idx, 1)(0)
        _removed.emit(IndexedElement(idx, e))
      }
    }
    else {
      if (filterFunction(x)) {
        refreshBuffer()
        _inserted.emit(IndexedElement(buffer.indexOf(x), x))
      }
    }
  }

  def removeObserverFor(x: A) = {
    observers.remove(x) match {
      case Some(o) => o.kill()
      case scala.None =>
    }
  }

  def addObserverFor(x: A) = {
    _observe match {
      case Some(f) =>
        observers.getOrElseUpdate(x, f(x) subscribe { _ =>
          refreshElement(x)
        })
      case scala.None =>
    }
  }

  // Overridden events
  val _added = Emitter[A]
  val _removed = Emitter[IndexedElement[A]]
  val _inserted = Emitter[IndexedElement[A]]
  val _updated = Emitter[IndexedElement[A]]

  val added = _added.view
  val removed = _removed.view
  val inserted = _inserted.view
  val updated = _updated.view

  // Copy collection to internal buffer
  // filtered with `filterFunction`
  parent foreach { e =>
    origBuffer(origBuffer.length) = e
    if (filterFunction(e)) {
      buffer(buffer.length) = e
    }
  }

  parent.added subscribe { e =>
    origBuffer(origBuffer.length) = e
    addObserverFor(e)
    if (filterFunction(e)) {
      buffer(buffer.length) = e
      _added.emit(e)
    }
  }

  parent.removed subscribe { x =>
    origBuffer.splice(x.idx, 1)
    removeObserverFor(x.e)
    if (filterFunction(x.e)) {
      val idx = buffer.indexOf(x.e)
      buffer.splice(idx, 1)
      _removed.emit(IndexedElement(idx, x.e))
    }
  }

  parent.inserted subscribe { x =>
    origBuffer.splice(x.idx, 0, x.e)
    addObserverFor(x.e)
    refreshElement(x.e)
  }

  parent.updated subscribe { x =>
    origBuffer(x.idx) = x.e
    addObserverFor(x.e)
    refreshElement(x.e)
  }

  override def observe(f: (A) => Bindable[Any]): CollectionView[A] = {
    _observe = Some(f)
    refreshBuffer()
    this
  }

  override def kill(): Unit = {
    observers.values.foreach(_.kill())
    super.kill()
  }

  def length: Int = buffer.length

  def indexOf(e: A) = buffer.indexOf(e)

  def apply(idx: Int) = buffer(idx)
}
