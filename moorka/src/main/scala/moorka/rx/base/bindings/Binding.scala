package moorka.rx.base.bindings

import moorka.rx.Mortal
import moorka.rx.base.Source

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Binding[A] {

  val bindingContext: Option[Binding[_]] = bindingStack.headOption

  def run(x: A): Unit

  def withContext[U](dependencies: Seq[Mortal])(f: ⇒ U) = {
    // Cleanup upstreams
    val currentBindingStack = bindingStack
    currentBindingStack.push(this)
    killDependencies(dependencies)
    try {
      f
    }
    finally {
      currentBindingStack.pop()
    }
  }

  def killDependencies(dependencies: Seq[Mortal]): Unit = {
    val optionThis = Some(this)
    dependencies foreach {
      case upstream: DependentBinding[_] ⇒
        upstream.parent match {
          case binding: Binding[_] 
            if binding.bindingContext == optionThis ⇒ binding.kill()
          case _ ⇒ ()
        }
        upstream.kill()
      case x ⇒ x.kill()
    }
  }
}
