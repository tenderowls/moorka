package moorka.flow

import moorka.death.Mortal

import scala.concurrent.ExecutionContext
import scala.collection.{mutable ⇒ scm}

object Context {

  def apply[U](globalErrorHandler: Throwable ⇒ U): Context = {
    new ContextImpl(globalErrorHandler)
  }

  def apply(): Context = {
    val globalErrorHandler = { cause: Throwable ⇒
      throw cause
    }
    new ContextImpl(globalErrorHandler)
  }

}

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
trait Context extends Mortal {

  def executionContext: ExecutionContext

  def globalErrorHandler: Throwable ⇒ _

  def validationNumber: Int

  def addSink(sink: Sink[_]): () ⇒ Unit

  def addSource(channel: Source): () ⇒ Unit

  def invalidate(): Unit

  def validate(): Unit

}

private final class ContextImpl(val globalErrorHandler: Throwable ⇒ _) extends Context {

  var alive: Boolean = true

  var validationNumber: Int = 0

  var valid = false

  var sinks = List.empty[Sink[_]]

  val sources = scm.Buffer.empty[Source]

  val executionContext = new ExecutionContext {
    def execute(runnable: Runnable): Unit = {
      // Run on current thread
      runnable.run()
    }
    def reportFailure(cause: Throwable): Unit = {
      globalErrorHandler(cause)
    }
  }

  def addSink(sink: Sink[_]): () ⇒ Unit = {
    sinks ::= sink
    () ⇒ sinks = sinks.filter(_ != sink)
  }

  def addSource(source: Source): () ⇒ Unit = {
    sources.append(source)
    invalidate()
    () ⇒ {
      val index = sources.indexOf(source)
      if (index > -1) sources.remove(index)
    }
  }

  def invalidate(): Unit = {
    if (alive) {
      valid = false
    }
  }

  def kill(): Unit = {
    alive = false
    for (sink ← sinks) sink.kill()
    for (source ← sources) source.kill()
  }

  def validate(): Unit = {
    if (!valid && alive) {
      for (sink ← sinks) sink.run()
      for (source ← sources) source.validate()
      valid = true
    }
  }

}
