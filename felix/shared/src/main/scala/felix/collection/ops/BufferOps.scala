package felix.collection.ops

import felix.collection.BufferView
import moorka.death.{Mortal, Reaper}
import moorka.flow.{Context, Flow}

import scala.concurrent.ExecutionContext

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class BufferOps[A](val self: BufferView[A]) extends AnyVal {

  /**
   * Maps collection to `B` type. Note that you receives
   * a reactive version of collection. It means that if
   * you change this collection, mapped collection will
   * changed too. Also, be aware that mapped version
   * will not convert elements immediately, its will be done
   * when you'll try to get element or parent collection
   * raise the event
    *
    * @param f map function
   * @tparam B mapped element type
   * @return mapped collection
   */
  def map[B](f: (A) => B)(implicit reaper: Reaper, context: Context): BufferView[B] = {
    val mapped = new MappedBuffer[A, B](self, f)
    reaper.mark(mapped)
    mapped
  }

  /**
   * Filters collection with `f` function. Note that you receives
   * a reactive version of collection. Meaning if you change
   * this collection, filtered collection will change too.
    *
    * @param f filter function. `false` if element need to be removed
   * @return filtered collection
   */
  def filter(f: (A) => Boolean)(implicit reaper: Reaper, context: Context): BufferView[A] = {
    val filtered = new FilteredBuffer[A](self, f)
    reaper.mark(filtered)
    filtered
  }

  /**
   * Applies a binary operator to a start value and all elements of this buffer,
   * going left to right.
   */
  def foldLeft[B](z: B)(op: (B, A) => B)
                 (implicit executor: ExecutionContext): Flow[B] = {
    val channels = {
      self.added mergeUnimportant
      self.removed mergeUnimportant
      self.inserted mergeUnimportant
      self.updated
    }
    channels map { _ ⇒
      toSeq.foldLeft(z)(op)
    }
  }

  /**
   * Apply function `f` to all elements of collection
    *
    * @param f function to aplly
   */
  def foreach[B](f: (A) => Any)(implicit reaper: Reaper, context: Context): Mortal = {
    val mapped = new MappedBuffer[A, Any](self, f)
    reaper.mark(mapped)
    mapped
  }

  /**
   * Find count of elements which satisfy `f`
    *
    * @param f `true` if satisfy. `false` if not
   * @return count of elements
   */
  def count(f: (A) => Boolean): Int = {
    var count = 0
    for (i <- 0 until self.length) {
      val e = self(i)
      if (f(e)) count += 1
    }
    count
  }

  @deprecated("Use toSeq against asSeq", "0.6.0")
  @inline def asSeq: Seq[A] = toSeq

  /**
   * Converts to standard scala immutable sequence
   */
  def toSeq: Seq[A] = {
    for (i <- 0 until self.length)
      yield self(i)
  }
}
