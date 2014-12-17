package moorka.rx.binding

import moorka.rx.base.State

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object BindingMacro {

  def apply[A](f: => A): State[A] = macro macroBindingImpl[A]

  def macroBindingImpl[A](c: whitebox.Context)(f: c.Tree) = {
    import c.universe._
    def processTree(t: c.Tree): List[c.Tree] = {
      val withoutFunctions = t.children.filter {
        case Function(_, _) => false
        case x if x.isDef => false
        case _ => true
      }
      // https://issues.scala-lang.org/browse/SI-4751
      val deep = withoutFunctions.map(processTree(_))
      withoutFunctions ++ deep.flatten
    }
    val bindingTrait = "moorka.rx.State"
    val bindingConstructor = c.mirror.staticClass(bindingTrait).toTypeConstructor
    val bindings = processTree(f).filter(_.tpe.typeConstructor <:< bindingConstructor)
    q"new moorka.rx.binding.ExpressionBinding(Seq(..$bindings))($f)"
  }
}
