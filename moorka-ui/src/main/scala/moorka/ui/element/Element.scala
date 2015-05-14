package moorka.ui.element

import moorka.rx._
import moorka.ui.Ref
import moorka.ui.event.EventProcessor

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class Element(tagName: String, children: Seq[ElementEntry]) extends ElementBase {

  val ref = Ref(tagName)

  fillSeq(children)
}
