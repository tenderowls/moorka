package moorka.rx.base

import moorka.rx.{Reaper, Mortal}
import scala.collection.mutable

object Channel {

  def apply[A](implicit reaper: Reaper = Reaper.nice): Channel[A] = {
    val channel = new Channel[A] {}
    reaper.mark(channel)
    channel
  }

}
/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Channel[A] extends Mortal {

  @volatile private var dead: Boolean = false

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
    if (!dead) {
      children.foreach(_.kill())
      children.remove(0, children.length)
      dead = true
    }
  }
}
