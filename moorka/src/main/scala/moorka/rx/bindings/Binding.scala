package moorka.rx.bindings

import scala.collection.mutable

trait BindingSensitive {
  // Register this binding inside
  // current binding context
  Binding.appendDependency(this)

  def kill(): Unit
}

class BindingContext(val parent: Option[BindingContext]) {

  val children = mutable.Buffer.empty[BindingContext]
  val dependencies = mutable.Buffer.empty[BindingSensitive]

  def kill(): Unit = {
    for (obsoleteDependency ← dependencies)
      obsoleteDependency.kill()
    for (obsoleteChild ← children)
      obsoleteChild.kill()
  }
}

object Binding {

  var currentBindingContext = Option.empty[BindingContext]

  def appendDependency(dependency: BindingSensitive): Unit = {
    currentBindingContext foreach { bc ⇒
      bc.dependencies += dependency
    }
  }

  def openContext(): BindingContext = {
    val bc = currentBindingContext match {
      case optionBc @ Some(currentBc) ⇒
        val child = new BindingContext(optionBc)
        currentBc.children += child
        child
      case None ⇒ new BindingContext(None)
    }
    currentBindingContext = Some(bc)
    bc
  }

  def closeContext(): Unit = {
    currentBindingContext =
      currentBindingContext.flatMap(_.parent)
  }
}


/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Binding[A] {

  var currentBindingContext = Option.empty[BindingContext]

  def run(x: A): Unit

  def withContext[U](runLambda: ⇒ U) = {
    // Destroy all bindings created in previous run
    // Open new context
    for (bc ← currentBindingContext) bc.kill()
    currentBindingContext = Some(Binding.openContext())
    try runLambda
    finally Binding.closeContext()
  }
}
