package com.tenderowls.moorka.core.binding

import com.tenderowls.moorka.core.rx.RxState

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object BindingMacro {

  def apply[A](f: => A): RxState[A] = macro macroBindingImpl[A]

  def macroBindingImpl[A](c: whitebox.Context)(f: c.Tree) = {
    import c.universe._
    def processTree(t:c.Tree):List[c.Tree] = {
      val withoutFunctions = t.children.filter {
        case Function(_) => false
        case _ => true
      }
      // todo fomkin: make something smarter
      withoutFunctions ++ withoutFunctions.map( x => processTree(x)).flatten
    }
    val bindingTrait = "com.tenderowls.moorka.core.RxState"
    val bindingConstructor = c.mirror.staticClass(bindingTrait).toTypeConstructor
    val bindings = processTree(f).filter(_.tpe.typeConstructor <:< bindingConstructor)
    q"new com.tenderowls.moorka.core.binding.ExpressionBinding(Seq(..$bindings))($f)"
  }
}
