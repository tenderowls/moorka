package moorka.rx.base

import moorka.rx._

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
trait Channel[+A] extends Mortal {

  @volatile private var dead: Boolean = false
  var children = List[Channel[Any]]()

  def linkChild(child: Channel[Any]): Unit = {
    this.synchronized {
      children ::= child
    }
  }

  def unlinkChild(child: Channel[Any]): Unit = {
    this.synchronized {
      children = children.filterNot(_ == child)
    }
  }

  def emit[B >: A](x: B): Unit = {
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
