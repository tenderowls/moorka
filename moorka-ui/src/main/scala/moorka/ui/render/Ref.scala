package moorka.ui.render

import moorka.rx._

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

object Ref {

  private var nextId = 0

  def apply(factoryId: String): Ref = {
    nextId += 1
    apply(factoryId, s"_mk$nextId")
  }

  def apply(factoryId: String, id: String): Ref = {
    RenderAPI ! js.Array("create_ref", factoryId, id)
    new Ref(id)
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class 
Ref(val id: String) extends Mortal {

  def kill(): Unit = {
    RenderAPI ! js.Array("kill_ref", id)
  }

  def appendChild(element: Ref) = {
    RenderAPI ! js.Array("append_child", id, element.id)
  }
  
  def insertChild(element: Ref, ref: Ref) = {
    RenderAPI ! js.Array("insert_child", id, element.id, ref.id)
  }
  
  def removeChild(element: Ref) = {
    RenderAPI ! js.Array("remove_child", id, element.id)
  }

  def replaceChild(newChild: Ref, oldChild: Ref) = {
    RenderAPI ! js.Array("replace_child", id, newChild.id, oldChild.id)
  }

  def removeChildren(elements: Seq[Ref]) = {
    val xs = new js.Array[String]
    elements.foreach(x => xs.push(x.id))
    RenderAPI ! js.Array("remove_children", id, xs)
  }
  
  def appendChildren(elements: Seq[Ref]) = {
    val xs = new js.Array[String]
    elements.foreach(x => xs.push(x.id))
    RenderAPI ! js.Array("append_children", id, xs)
  }
  
  def updateAttribute(name: String, value: Any) = {
    RenderAPI ! js.Array("update_attribute", id, name, value)
  }

  def classAdd(name: String): Unit = {
    RenderAPI ! js.Array("class_add", id, name)
  }

  def classRemove(name: String) = {
    RenderAPI ! js.Array("class_remove", id, name)
  }

  def set(name: String, value: Any) = {
    RenderAPI ! js.Array("set", id, name, value)
  }

  def get[T](name: String): Future[T] = {
    val requestId = Math.random()
    RenderAPI ? js.Array("get", id, name, requestId) flatMap {
      case "error" ⇒ Future.failed {
        new Exception("Internal error")
      }
      case x => Future.successful {
        x.asInstanceOf[T]
      }
    }
  }

  def call[T >: Null](name: String, args: Any*): Future[T] = {
    val requestId = Math.random()
    val xs = new js.Array[Any]
    args.foreach {
      case x: Ref => xs.push("$$:" + x.id)
      case any => xs.push(any)
    }
    RenderAPI ? js.Array("call", id, name, requestId).concat(xs) flatMap {
      case null ⇒ Future.successful(null)
      case "error" ⇒ Future.failed {
        new Exception("Internal error")
      }
      case x => Future.successful {
        x.asInstanceOf[T]
      }
    }
  }

  def callNoResult(name: String, args: Any*): Future[Unit] = {
    val requestId = Math.random()
    val xs = new js.Array[Any]
    args.foreach {
      case x: Ref => xs.push("$$:" + x.id)
      case any => xs.push(any)
    }
    RenderAPI ? js.Array("call", id, name, requestId).concat(xs)  flatMap {
      case "error" ⇒ Future.failed {
        new Exception("Internal error")
      }
      case x => Future.successful(())
    }
  }

}
