package moorka.rx.base

import moorka.rx.{Mortal, Reaper}

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
  var children = List[Channel[A]]()

  def linkChild(child: Channel[A]): Unit = {
    this.synchronized {
      children ::= child
    }
  }

  def unlinkChild(child: Channel[A]): Unit = {
    this.synchronized {
      children = children.filterNot(_ == child)
    }
  }

  def emit(x: A): Unit = {
    children.foreach(_.emit(x))
  }

  def kill(): Unit = {
    if (!dead) {
      children.foreach(_.kill())
      children = Nil
      dead = true
    }
  }
}
