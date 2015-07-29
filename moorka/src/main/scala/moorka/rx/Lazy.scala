package moorka.rx

object Lazy {
  def apply[T](f: ⇒ T): Lazy[T] = {
    new Lazy[T] {
      lazy val x = f
      def eval() = x
    }
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
sealed trait Lazy[+A] {
  def eval(): A
  def flatMap[B](f: A ⇒ Lazy[B]): Lazy[B] = Lazy(f(eval()).eval())
  def map[B](f: A ⇒ B): Lazy[B] = Lazy(f(eval()))
}
