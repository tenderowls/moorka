package moorka.rx.base

import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
package object bindings {

  private val _bindingStack = new ThreadLocal[mutable.Stack[Rx[_]]]()
  
  private[bindings] def bindingStack = {
    val value = _bindingStack.get()
    if (value == null) {
      val newValue = mutable.Stack[Rx[_]]()
      _bindingStack.set(newValue)
      newValue
    }
    else value
  }
}
