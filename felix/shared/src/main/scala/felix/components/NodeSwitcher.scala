package felix.components

import felix.FelixSystem
import felix.core.EventTarget
import felix.vdom.{ElementRef, NodeLike, RefHolder}
import moorka.death.{Mortal, Reaper}
import moorka.flow.Flow

object NodeSwitcher {

  sealed trait Policy

  object Policy {

    case object DropPrevious extends Policy

    case object KeepPrevious extends Policy

  }

  type Switch = (Policy, Option[NodeLike])

  def apply(rx: Flow[Switch])(implicit system: FelixSystem): NodeSwitcher = {
    new NodeSwitcher(rx)
  }
}

import felix.components.NodeSwitcher._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class NodeSwitcher(rx: Flow[Switch])(implicit system: FelixSystem) extends NodeLike with Mortal {

  var parent = Option.empty[RefHolder with EventTarget]

  var currentNode = Option.empty[NodeLike]

  implicit val reaper = Reaper()
  implicit val context = system.flowContext

  private[this] def swap(oldNodeOpt: Option[NodeLike],
                         newNodeOpt: Option[NodeLike]): Unit = {
    oldNodeOpt match {
      case Some(oldNode) ⇒
        oldNode.setParent(None)
        val node = newNodeOpt.getOrElse(EmptyComponent())
        parent.foreach(_.ref.call[Unit]("replaceChild", node.ref, oldNode.ref))
        node.setParent(parent)
        currentNode = Some(node)
      case None ⇒
        newNodeOpt foreach { node ⇒
          node.setParent(parent)
          currentNode = Some(node)
        }
    }
  }

  rx foreach {
    case (Policy.DropPrevious, newNode) ⇒
      val savedCurrentNode = currentNode
      swap(currentNode, newNode)
      savedCurrentNode.foreach(_.kill())
    case (Policy.KeepPrevious, newNode) ⇒
      swap(currentNode, newNode)
  }

  if (currentNode.isEmpty) {
    val node = EmptyComponent()
    node.setParent(parent)
    currentNode = Some(node)
  }

  def ref: ElementRef = {
    // Should always be defined
    currentNode.get.ref
  }

  private[felix] def setParent(value: Option[RefHolder with EventTarget]): Unit = {
    currentNode.foreach(_.setParent(value))
    parent = value
  }

  def kill(): Unit = {
    reaper.sweep()
  }
}
