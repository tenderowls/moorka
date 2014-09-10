package com.tenderowls.moorka.core.collection

import scala.scalajs.js

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[collection] class Mapped[From, A](parent: CollectionView[From],
                                          mapFunction: From => A)

  extends CollectionBase[A] {

  val buffer = new js.Array[A](parent.length)

  // Mapped events
  val added = parent.added.map(mapFunction)
  val removed = parent.removed.map { x => x.copy(e = apply(x.idx))}
  val inserted = parent.inserted.map(x => x.copy(e = mapFunction(x.e)))
  val updated = parent.updated.map(x => x.copy(e = mapFunction(x.e)))

  added.subscribe { x => buffer.push(x)}
  removed.subscribe { x => buffer.splice(x.idx, 1)}
  inserted.subscribe(x => buffer.splice(x.idx, 0, x.e))
  updated.subscribe(x => buffer(x.idx) = x.e)

  def length: Int = parent.length

  def apply(idx: Int) = {
    val e: js.UndefOr[A] = buffer(idx)
    if (e.isDefined) {
      e.get
    }
    else {
      val e = mapFunction(parent(idx))
      buffer(idx) = e
      e
    }
  }

  def indexOf(e: A): Int = {
    for (i <- 0 until length) {
      if (apply(i) == e)
        return i
    }
    -1
  }
}
