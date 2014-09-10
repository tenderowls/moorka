package com.tenderowls.moorka.mkml.dom

import com.tenderowls.moorka.core._
import org.scalajs.dom

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class Element(tagName:String, children:Seq[Node]) extends ElementBase {

  val nativeElement: dom.Element = dom.document.createElement(tagName)

  var observers:List[Mortal] = Nil

  children.foreach {
    case component: ElementBase =>
      observers ::= component  
      RenderContext.appendOperation(
        DomOperation.AppendChild(nativeElement, component.nativeElement)
      )
    case sequence: ElementSequence =>
      sequence.value.foreach(observers ::= _)
      RenderContext.appendOperation(
        DomOperation.AppendChildren(nativeElement, sequence.value.map(_.nativeElement))
      )
    case processor: ElementExtension =>
      observers ::= processor
      processor.assignElement(this)
  }

  override def kill(): Unit = {
    observers.foreach(_.kill())
    observers = Nil
  }
}
