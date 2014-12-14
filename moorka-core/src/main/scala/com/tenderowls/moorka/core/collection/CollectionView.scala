package com.tenderowls.moorka.core.collection

import com.tenderowls.moorka.core._

/**
 * Reactive collection presentation. You can't modify
 * element list from this trait
 * @tparam A type of elements
 * @author Aleksey Fomkin <fomkin@tenderowls.com>
 */
trait CollectionView[A] extends Mortal {

  //---------------------------------------------------------------------------
  //
  //  Events
  //
  //---------------------------------------------------------------------------

  /**
   * Element in `idx` updated. Note that this event doesn't
   * raise when reactive variable inside element change its
   * value
   */
  val updated: RxStream[IndexedElement[A]]

  /**
   * Element added into end of collection
   */
  val added: RxStream[A]

  /**
   * Element removed
   */
  val removed: RxStream[IndexedElement[A]]

  /**
   * Element inserted into `idx`. Collection shifted to right
   */
  val inserted: RxStream[IndexedElement[A]]

  /**
   * Length of collection
   */
  val length: RxState[Int]

  //---------------------------------------------------------------------------
  //
  // Methods
  //
  //---------------------------------------------------------------------------

  /**
   * @param idx index of the element
   * @return Element in `idx`
   */
  def apply(idx: Int): A

  /**
   * Finds element in collection
   * @param e element
   * @return index of element `e`. -1 if not fount
   */
  def indexOf(e: A): Int

  /**
   * Removes all links to another collections or any reactive
   * value. Meaning if you have collection `a` and collection
   * `b` which were mapped to `a`, `b.kill()` would stop `a` -> `b`
   * event propagation.
   */
  def kill(): Unit

  /**
   * Find count of elements which satisfy `f`
   * @param f `true` if satisfy. `false` if not
   * @return count of elements
   */
  def count(f: (A) => Boolean): Int

  //---------------------------------------------------------------------------
  //
  // Combinators
  //
  //---------------------------------------------------------------------------

  /**
   * Apply function `f` to all elements of collection
   * @param f function to ally
   */
  def foreach(f: A => Any): Unit

  /**
   * Maps collection to `B` type. Note that you receives
   * a reactive version of collection. It means that if
   * you change this collection, mapped collection will
   * changed too. Also, be aware that mapped version
   * will not convert elements immediately, its will be done
   * when you'll try to get element or parent collection
   * raise the event
   * @param f map function
   * @tparam B mapped element type
   * @return mapped collection
   */
  def map[B](f: (A) => B): CollectionView[B]

  /**
   * Filters collection with `f` function. Note that you receives
   * a reactive version of collection. Meaning if you change
   * this collection, filtered collection will change too.
   * @param f filter function. `false` if element need to be removed
   * @return filtered collection
   */
  def filter(f: (A) => Boolean): CollectionView[A]

 /**
  * Applies a binary operator to a start value and all elements of this $coll,
  * going left to right.
  */
  def foldLeft[B](z: B)(op: (B, A) => B): RxState[B]

  /**
   * Converts to standard scala immutable sequence
   */
  def asSeq: Seq[A]
}

case class IndexedElement[A](idx: Int, e: A)
