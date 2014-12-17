package moorka.rx.collection

import moorka.rx.binding.ExpressionBinding
import moorka.rx._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[collection] abstract class BufferBase[A] extends BufferView[A] {

  def map[B](f: (A) => B): BufferView[B] = {
    new Mapped[A, B](this, f)
  }

  def filter(f: (A) => Boolean): BufferView[A] = {
    new Filtered[A](this, f)
  }

  def foldLeft[B](z: B)(op: (B, A) => B): State[B] = {
    new ExpressionBinding(Seq(added, removed, inserted, updated))({
      var result = z
      foreach (x => result = op(result, x))
      result
    })
  }

  def foreach(f: A => Any): Unit = {
    val elements = asSeq
    for (e <- elements) {
      f(e)
    }
  }

  def count(f: (A) => Boolean): Int = {
    var count = 0
    for (i <- 0 until length()) {
      val e = apply(i)
      if (f(e)) count += 1
    }
    count
  }

  def asSeq: Seq[A] = {
    for (i <- 0 until length()) yield apply(i)
  }

  def observe(f: (A) => State[Any]): BufferView[A] = this

  def kill(): Unit = {
    added.kill()
    removed.kill()
    inserted.kill()
    updated.kill()
  }

  override def toString = {
    // Force elements (for non-strict collection)
    val values = for (x <- 0 until length()) yield apply(x).toString
    s"Collection(${values.reduce(_ + ", " + _)})"
  }
}
