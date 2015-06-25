package moorka.rx.base

object Lazy {
  def apply[T](f: ⇒ T): Lazy[T] = {
    new Lazy[T] {
      lazy val x = f
      def apply() = x
    }
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
sealed trait Lazy[+T] {
  def apply(): T
  def foreach[B](f: T ⇒ B): Unit = f(apply())
  def flatMap[B](f: T ⇒ Lazy[B]): Lazy[B] = f(apply())
  def filter(f: T ⇒ Boolean) = {
    val x = apply()
    val b = f(x)
    if (b) x else LazyNone
  }
  def withFilter(f: T ⇒ Boolean) = filter(f)
  def map[B](f: T ⇒ B): B = f(apply())
}

case object LazyNone extends Lazy[Nothing] {
  def apply() = throw new NoSuchElementException("Unable to get dummies value")
}