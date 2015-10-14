package felix.specials

import felix._
import felix.specials.ElementsPipe.Direction
import felix.vdom.{ElementRef, Directive, NodeLike}
import moorka._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class ElementsPipe(direction: Direction, pipe: Rx[NodeLike], system: FelixSystem) extends Mortal {

  implicit val reaper = Reaper()

  val insert = {
    val functionName = direction match {
      case ElementsPipe.Left ⇒ "insertBefore"
      case ElementsPipe.Right ⇒ "insertAfter"
    }
    system.utils.call[Unit](functionName, _: ElementRef, _: ElementRef)
  }

  def affect(element: NodeLike): Unit = {
    FSM(element: NodeLike) {
      case prev ⇒ pipe map { next ⇒
        insert(prev.ref, next.ref)
        reaper.mark(next)
      }
    }
  }

  def kill(): Unit = reaper.sweep()
}

object ElementsPipe {

  sealed trait Direction

  case object Left extends Direction

  case object Right extends Direction
}
