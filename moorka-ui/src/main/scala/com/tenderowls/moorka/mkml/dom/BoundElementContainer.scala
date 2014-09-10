package com.tenderowls.moorka.mkml.dom

import com.tenderowls.moorka.core._
import com.tenderowls.moorka.mkml.dom.CreationPolicy._
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
        RenderContext.appendOperation(DomOperation.RemoveChild(nativeElement, x.nativeElement))
      case CreationPolicy.Dynamic(x) =>
        RenderContext.appendOperation(DomOperation.RemoveChild(nativeElement, x.nativeElement))
        x.kill()
      case _ =>
    }

    val e = reactiveElement()
    previous = reactiveElement()
    e match {
      case CreationPolicy.Static(x) =>
        RenderContext.appendOperation(DomOperation.AppendChild(nativeElement, x.nativeElement))
      case CreationPolicy.Dynamic(x) =>
        RenderContext.appendOperation(DomOperation.RemoveChild(nativeElement, x.nativeElement))
      case _ =>
    }
  }

  override def kill(): Unit = {
    observer.kill()
    reactiveElement() match {
      case CreationPolicy.Static(x) => x.kill()
      case CreationPolicy.Dynamic(x) => x.kill()
      case _ =>
    }
  }
}

object CreationPolicy {
  sealed trait CreationPolicy
  case class Static(node: ElementBase) extends CreationPolicy
  case class Dynamic(node: ElementBase) extends CreationPolicy
  case object Empty extends CreationPolicy
}
