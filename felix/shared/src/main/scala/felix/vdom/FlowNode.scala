package felix.vdom

import felix.core.{EventTarget, FelixSystem}
import felix.vdom.directives.AttributeDirective
import moorka.death.{Mortal, Reaper}
import moorka.flow.Flow

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class FlowNode(rx: Flow[NodeLike], system: FelixSystem) extends NodeLike with Mortal {

  var parent = Option.empty[RefHolder with EventTarget]

  var currentNode = Option.empty[NodeLike]

  implicit val reaper = Reaper()
  implicit val context = system.flowContext

  rx foreach { node ⇒
    // If it is not fist call of this
    // function replace element in DOM
    currentNode foreach { previousNode ⇒
      previousNode.setParent(None)
      parent.foreach(_.ref.call[Unit]("replaceChild", node.ref, previousNode.ref))
    }
    // Anyway update current node
    node.setParent(parent)
    currentNode = Some(node)
  }

  // If rx was stateless create dummy
  // element to hold element place
  if (currentNode.isEmpty) {
    val element = new Element("div", system)
    val styleAttr = new AttributeDirective.Simple("style", "display: none")
    element.append(Seq(styleAttr))
    currentNode = Some(element)
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
