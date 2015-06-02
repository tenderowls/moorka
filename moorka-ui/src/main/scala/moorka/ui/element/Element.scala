package moorka.ui.element

import moorka.ui.Ref

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class Element(tagName: String, children: Seq[ElementEntry]) extends ElementBase {

  val ref = Ref(tagName)

  fillSeq(children)
}
