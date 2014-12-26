package moorka.ui.render

import moorka.rx.Mortal

import scala.concurrent.{Future, Promise}
import scala.scalajs.js

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
class Ref(val id: String) extends Mortal {

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
  
  def get(name: String): Future[Any] = {
    val requestId = Math.random()
    val promise = Promise[Any]()
    // todo this operation produce a lot of garbage 
    RenderAPI.onMessage until { x =>
      if (x(0) == "get_response" && x(1) == requestId) {
        x(2) match {
          case "error" => promise.failure(new Exception("Can't get " + name + " from " + id))
          case any => promise.success(any) 
        }
        false
      }
      else true
    }
    RenderAPI ! js.Array("get", id, name, requestId)
    promise.future
  }

  def call(element: Ref, name: String, args: Any*): Future[Any] = {
    val requestId = Math.random()
    val promise = Promise[Any]()
    // todo this operation produce a lot of garbage 
    RenderAPI.onMessage until { x =>
      if (x(0) == "call_response" && x(1) == requestId) {
        x(2) match {
          case "error" => promise.failure(new Exception("Can't get " + name + " from " + element.id))
          case any => promise.success(any)
        }
        false
      }
      else true
    }
    val xs = new js.Array[Any]
    args.foreach {
      case x: Ref => xs.push("$$:" + x.id)
      case any => xs.push(any)
    }
    RenderAPI ! js.Array("call", element.id, name, requestId).concat(xs)
    promise.future
  }
}
