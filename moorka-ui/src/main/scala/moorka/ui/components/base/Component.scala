package moorka.ui.components.base

import moorka.ui.Ref
import moorka.ui.element.{ElementBase, ElementEntry}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
abstract class Component(tagName: String = "div") extends ElementBase {
  val ref = Ref(tagName)
  @deprecated("Use component it self instead el", "0.5.0")
  final val el = this
}
