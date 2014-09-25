package com.tenderowls.moorka.mkml.dom

import com.tenderowls.moorka.core._
import com.tenderowls.moorka.mkml.dom.CreationPolicy._
import com.tenderowls.moorka.mkml.engine._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class BoundComponentContainer(reactiveElement: Bindable[CreationPolicy])
  extends ComponentBase {

  var previous:CreationPolicy = CreationPolicy.Empty

  val ref = Ref("div")

  val observer = reactiveElement observe { reactiveElement =>

    previous match {
      case CreationPolicy.Static(x) =>
        x.parent = this
        ref.removeChild(x.ref)
      case CreationPolicy.Dynamic(x) =>
        x.parent = null
        ref.removeChild(x.ref)
        x.kill()
      case _ =>
    }

    val e = reactiveElement()
    previous = reactiveElement()
    e match {
      case CreationPolicy.Static(x) =>
        x.parent = this
        ref.appendChild(x.ref)
      case CreationPolicy.Dynamic(x) =>
        x.parent = null
        ref.removeChild(x.ref)
      case _ =>
    }
  }

  override def kill(): Unit = {
    super.kill()
    observer.kill()
    reactiveElement() match {
      case CreationPolicy.Static(x) => x.kill()
      case CreationPolicy.Dynamic(x) => x.kill()
      case _ =>
    }
  }

  SyntheticEventProcessor.registerElement(this)
}

object CreationPolicy {
  sealed trait CreationPolicy
  case class Static(node: ComponentBase) extends CreationPolicy
  case class Dynamic(node: ComponentBase) extends CreationPolicy
  case object Empty extends CreationPolicy
}
