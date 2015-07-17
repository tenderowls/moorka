package felix.components

import felix.core.FelixSystem
import felix.vdom.{Element, Entry}

object Component {
  def apply(entries: Entry*)(implicit system: FelixSystem): Component = {
    new Component() {
      append(entries)
    }
  }

  def apply(tag: String, entries: Entry*)(implicit system: FelixSystem): Component = {
    new Component(tag) {
      append(entries)
    }
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class Component(tag: String = "div")(implicit system: FelixSystem)
  extends Element(tag, system) {
}
