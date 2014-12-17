package moorka.rx.base

import moorka.rx.Mortal
import scala.collection.mutable

object Channel {

  def apply[A] = new Channel[A] {
  }
}
/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Channel[A] extends Mortal {

  val children = mutable.Buffer[Channel[A]]()

  def linkChild(child: Channel[A]): Unit = {
    children += child
  }

  def unlinkChild(child: Channel[A]): Unit = {
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
