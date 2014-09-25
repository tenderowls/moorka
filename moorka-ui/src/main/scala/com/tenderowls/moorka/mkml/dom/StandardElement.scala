package com.tenderowls.moorka.mkml.dom

import com.tenderowls.moorka.core._
import com.tenderowls.moorka.mkml.engine._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class StandardElement(tagName:String, id: Option[String], children:Seq[SyntheticDomNode]) extends ComponentBase {

  val ref = id match {
    case Some(x) => Ref(tagName, x)
    case None => Ref(tagName)
  }

  var observers:List[Mortal] = Nil

  children.foreach {
    case e: ComponentBase =>
      observers ::= e
      e.parent = this
      ref.appendChild(e.ref)
    case sequence: ElementSequence =>
      sequence.value.foreach { x =>
        x.parent = this
        observers ::= x
      }
      ref.appendChildren(sequence.value.map(_.ref))
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
