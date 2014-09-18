package com.tenderowls.moorka.mkml.dom

import com.tenderowls.moorka.core._
import com.tenderowls.moorka.mkml.engine._
import org.scalajs.dom

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class Element(tagName:String, children:Seq[Node]) extends ElementBase {

  val nativeElement: dom.Element = dom.document.createElement(tagName)

  var observers:List[Mortal] = Nil

  children.foreach {
    case e: ElementBase =>
      observers ::= e
      e.parent = this
      RenderContext.appendOperation(
        AppendChild(nativeElement, e.nativeElement)
      )
    case sequence: ElementSequence =>
      sequence.value.foreach { x =>
        x.parent = this
        observers ::= x
      }
      RenderContext.appendOperation(
        AppendChildren(
          nativeElement,
          sequence.value.map(_.nativeElement)
        )
      )
    case processor: ElementExtension =>
      observers ::= processor
      processor.assignElement(this)
  }

  override def kill(): Unit = {
    super.kill()
    observers.foreach(_.kill())
    observers = Nil
  }

  SyntheticEventProcessor.registerElement(this)
}
