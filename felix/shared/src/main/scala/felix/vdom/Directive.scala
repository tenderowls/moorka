package felix.vdom

import moorka.death.Mortal

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Directive extends Entry with Mortal {
  def affect(element: Element)
}
