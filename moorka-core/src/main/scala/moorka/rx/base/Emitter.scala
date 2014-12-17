package moorka.rx.base

import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object Emitter {
  def apply[A] = new Emitter[A]
}

class Emitter[A] extends RxStream[A] {

  val children = mutable.Buffer[RxStream[A]]()

  def linkChild(child: RxStream[A]): Unit = {
    children += child
  }

  def unlinkChild(child: RxStream[A]): Unit = {
    children -= child
  }

  def emit(x: A): Unit = {
    for (slot <- children)
      slot.emit(x)
  }

  def kill(): Unit = {
    children.foreach(_.kill())
    children.remove(0, children.length)
  }
}
