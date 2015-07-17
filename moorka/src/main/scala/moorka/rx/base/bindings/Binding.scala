package moorka.rx.base.bindings

import moorka.rx.Mortal

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Binding[A] {

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
      case upstream: HasBindingContext[_] ⇒
        upstream.parent match {
          case binding: HasBindingContext[_]
            if binding.bindingContext == optionThis ⇒ binding.kill()
          case _ ⇒ ()
        }
        upstream.kill()
      case x ⇒ x.kill()
    }
  }
}
