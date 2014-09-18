package com.tenderowls.moorka.mkml.dom

import com.tenderowls.moorka.core._
import com.tenderowls.moorka.mkml.dom.CreationPolicy._
import com.tenderowls.moorka.mkml.engine._
import org.scalajs.dom

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class BoundElementContainer(reactiveElement: Bindable[CreationPolicy])
  extends ElementBase {

  var previous:CreationPolicy = CreationPolicy.Empty

  val nativeElement: dom.Element = dom.document.createElement("div")

  val observer = reactiveElement observe { reactiveElement =>

    previous match {
      case CreationPolicy.Static(x) =>
        x.parent = this
        RenderContext.appendOperation(RemoveChild(nativeElement, x.nativeElement))
      case CreationPolicy.Dynamic(x) =>
        x.parent = null
        RenderContext.appendOperation(RemoveChild(nativeElement, x.nativeElement))
        x.kill()
      case _ =>
    }

    val e = reactiveElement()
    previous = reactiveElement()
    e match {
      case CreationPolicy.Static(x) =>
        x.parent = this
        RenderContext.appendOperation(AppendChild(nativeElement, x.nativeElement))
      case CreationPolicy.Dynamic(x) =>
        x.parent = null
        RenderContext.appendOperation(RemoveChild(nativeElement, x.nativeElement))
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
  case class Static(node: ElementBase) extends CreationPolicy
  case class Dynamic(node: ElementBase) extends CreationPolicy
  case object Empty extends CreationPolicy
}
