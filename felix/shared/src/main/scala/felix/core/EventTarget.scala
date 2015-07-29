package felix.core

import scala.annotation.tailrec

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait EventTarget {

  private[felix] var parent = Option.empty[EventTarget]

  def refId: String
  
  /**
   * @return Parents of this. From the top to the bottom.
   */
  def parents: List[EventTarget] = {
    @tailrec
    def rec(e: EventTarget, xs: List[EventTarget]): List[EventTarget] = {
      e.parent match {
        case Some(x) ⇒ rec(x, x :: xs)
        case _ ⇒ xs
      }
    }
    rec(this, Nil)
  }

  /**
   * Get the closest parent that satisfies some condition
   * @param f condition
   */
  def findParent(f: EventTarget ⇒ Boolean): Option[EventTarget] = {
    @tailrec def rec(it: EventTarget): Option[EventTarget] = it.parent match {
      case Some(x) if f(x) ⇒ Some(x)
      case Some(x) ⇒ rec(x)
      case None ⇒ None
    }
    rec(this)
  }
}
