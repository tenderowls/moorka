package com.tenderowls.moorka.mkml.engine

import com.tenderowls.moorka.core.Mortal

import scala.concurrent.{Future, Promise}
import scala.scalajs.js

object Ref {

  private var nextId = 0

  def apply(factoryId: String): Ref = {
    nextId += 1
    apply(factoryId, s"_mk$nextId")
  }

  def apply(factoryId: String, id: String): Ref = {
    RenderBackendApi ! js.Array("create_ref", factoryId, id)
    new Ref(id)
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class Ref(val id: String) extends Mortal {

  def kill(): Unit = {
    RenderBackendApi ! js.Array("kill_ref", id)
  }

  def appendChild(element: Ref) = {
    RenderBackendApi ! js.Array("append_child", id, element.id)
  }
  
  def insertChild(element: Ref, ref: Ref) = {
    RenderBackendApi ! js.Array("insert_child", id, element.id, ref.id)
  }
  
  def removeChild(element: Ref) = {
    RenderBackendApi ! js.Array("remove_child", id, element.id)
  }

  def replaceChild(newChild: Ref, oldChild: Ref) = {
    RenderBackendApi ! js.Array("replace_child", id, newChild.id, oldChild.id)
  }

  def removeChildren(elements: Seq[Ref]) = {
    val xs = new js.Array[String]
    elements.foreach(x => xs.push(x.id))
    RenderBackendApi ! js.Array("remove_children", id, xs)
  }
  
  def appendChildren(elements: Seq[Ref]) = {
    val xs = new js.Array[String]
    elements.foreach(x => xs.push(x.id))
    RenderBackendApi ! js.Array("append_children", id, xs)
  }
  
  def updateAttribute(name: String, value: Any) = {
    RenderBackendApi ! js.Array("update_attribute", id, name, value)
  }

  def classAdd(name: String) = {
    RenderBackendApi ! js.Array("class_add", id, name)
  }

  def classRemove(name: String) = {
    RenderBackendApi ! js.Array("class_remove", id, name)
  }

  def set(name: String, value: Any) = {
    RenderBackendApi ! js.Array("set", id, name, value)
  }
  
  def get(name: String): Future[Any] = {
    val requestId = Math.random()
    val promise = Promise[Any]()
    // todo this operation produce a lot of garbage 
    RenderBackendApi.onMessage until { x =>
      if (x(0) == "get_response" && x(1) == requestId) {
        x(2) match {
          case "error" => promise.failure(new Exception("Can't get " + name + " from " + id))
          case any => promise.success(any) 
        }
        false
      }
      else true
    }
    RenderBackendApi ! js.Array("get", id, name, requestId)
    promise.future
  }

  def call(element: Ref, name: String, args: Any*): Future[Any] = {
    val requestId = Math.random()
    val promise = Promise[Any]()
    // todo this operation produce a lot of garbage 
    RenderBackendApi.onMessage until { x =>
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
    RenderBackendApi ! js.Array("call", element.id, name, requestId).concat(xs)
    promise.future
  }
}
