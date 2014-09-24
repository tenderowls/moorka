package com.tenderowls.moorka.mkml.engine

import com.tenderowls.moorka.core.events.Emitter
import org.scalajs.dom
import org.scalajs.dom.BlobPropertyBag

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
@JSExport("Moorka")
object Moorka {

  object RenderBackendImpl {

    private val entities = mutable.Map[String, dom.Element](
      "root" -> dom.document.body
    )

    private def element(refId: String): dom.Element = {
      entities(refId)
    }

    private def copyEvent(e: dom.Event): js.Dynamic = {
      val copy = js.Dynamic.literal()
      val dynEvent = e.asInstanceOf[js.Dynamic]
      val propNames = js.Object.getOwnPropertyNames(e)
      // Iterate on properties
      propNames.foreach { x =>
        val value = dynEvent.selectDynamic(x)
        value.asInstanceOf[Any] match {
          case v: String => copy.updateDynamic(x)(v)
          case v: Boolean => copy.updateDynamic(x)(v)
          case v: Int => copy.updateDynamic(x)(v)
          case v: Float => copy.updateDynamic(x)(v)
          case v: dom.Element => copy.updateDynamic(x)(v.id)
          case _ => // do nothing
        }
      }
      copy
    }

    def receive(ops: js.Array[RenderBackendApi.Message]) = {
      ops.forEach { op: RenderBackendApi.Message =>
        op(0) match {
          case "create_ref" =>
            val factoryId = op(1).asInstanceOf[String]
            val refId = op(2).asInstanceOf[String]
            val el = dom.document.createElement(factoryId)
            el.id = refId
            entities.put(refId, el)
          case "append_child" =>
            // (to, element)
            val elementId = op(1).asInstanceOf[String]
            val newChildId = op(2).asInstanceOf[String]
            element(elementId).appendChild(element(newChildId))
          case "insert_child" =>
            val toId = op(1).asInstanceOf[String]
            val newId = op(2).asInstanceOf[String]
            val beforeId = op(3).asInstanceOf[String]
            element(toId).insertBefore(element(newId), element(beforeId))
          case "remove_child" =>
            val fromId = op(1).asInstanceOf[String]
            val elementId = op(2).asInstanceOf[String]
            element(fromId).removeChild(element(elementId))
          case "replace_child" =>
            val whereId = op(1).asInstanceOf[String]
            val newChildId = op(2).asInstanceOf[String]
            val oldChildId = op(3).asInstanceOf[String]
            element(whereId).replaceChild(element(newChildId), element(oldChildId));
          case "remove_children" =>
            val from = element(op(1).asInstanceOf[String])
            val xs = op(2).asInstanceOf[js.Array[String]]
            xs.foreach(x => from.removeChild(element(x)))
          case "append_children" =>
            val xs = op(2).asInstanceOf[js.Array[String]]
            if (xs.length > 0) {
              val fragment = dom.document.createDocumentFragment()
              val to = element(op(1).asInstanceOf[String])
              xs.foreach(x => fragment.appendChild(element(x)))
              to.appendChild(fragment)
            }
          case "update_attribute" =>
            val elementId = op(1).asInstanceOf[String]
            element(elementId).setAttribute(
              op(2).asInstanceOf[String],
              op(3).asInstanceOf[String])
          case "class_add" =>
            val elementId = op(1).asInstanceOf[String]
            element(elementId).classList.add(op(2).asInstanceOf[String])
          case "class_remove" =>
            val elementId = op(1).asInstanceOf[String]
            element(elementId).classList.remove(op(2).asInstanceOf[String])
          case "set" =>
            val elementId = op(1).asInstanceOf[String]
            val propertyName = op(2).asInstanceOf[String]
            element(elementId).asInstanceOf[js.Dynamic]
              .updateDynamic(propertyName)(op(3).asInstanceOf[js.Any])
          case "get" =>
            //(element, name, reqId)
            val elementId = op(1).asInstanceOf[String]
            val propName = op(2).asInstanceOf[String]
            val value = element(elementId).asInstanceOf[js.Dynamic].selectDynamic(propName)
            messageStream.emit(js.Array("get_response", op(3), value))
          case "call" =>
            //(element, name, reqId, args ...)
            val el = element(op(1)
              .asInstanceOf[String])
              .asInstanceOf[js.Dynamic]
            val funcName = op(2).asInstanceOf[String]
            val opCount = op.length
            val result = if (opCount > 4) {
              el.applyDynamic(funcName)(args = op.slice(4).map { arg: Any =>
                arg match {
                  case x: String if x.substring(0, 3) == "$$:" =>
                    element(x.substring(3));
                  case _ => arg
                }
              })
            }
            else {
              el.applyDynamic(funcName)()
            }
            messageStream.emit(js.Array("get_response", op(3), result))
        }
      }
    }

    val messageStream = Emitter[RenderBackendApi.Message]

    Seq("click", "dblclick", "change").foreach { eventType =>
      dom.document.addEventListener(eventType, { (e: dom.Event) =>
        messageStream.emit(js.Array("event", copyEvent(e)))
      })
    }

    // Auto-cancel submit event
    dom.document.addEventListener("submit", { (e: dom.Event) =>
      e.preventDefault()
      messageStream.emit(js.Array("event", copyEvent(e)))
    })
  }

  @JSExport
  def application(main: String) = {
    // Initialize API
    RenderBackendApi.defaultMode(
      { x: js.Array[RenderBackendApi.Message] => RenderBackendImpl.receive(x)},
      RenderBackendImpl.messageStream
    )
    // Run main class
    js.eval(s"$main()").asInstanceOf[js.Dynamic].applyDynamic("main")()
    RenderBackendImpl.messageStream.emit(js.Array("start"))
  }

  @JSExport
  def workerApplication(main: String) = {
    val blob = new dom.Blob(js.Array[js.Any](
      "importScripts('http://localhost:9090/target/scala-2.11/moorka-todomvc-fastopt.js');\n",
      "renderBackend().workerMode();\n",
      main + "().main();"
    ), js.Dynamic.literal("type" -> "application/javascript")
      .asInstanceOf[BlobPropertyBag]
    )
    val url = js.eval("window.URL").asInstanceOf[js.Dynamic]
      .applyDynamic("createObjectURL")(blob)
    val worker = new dom.Worker(url.asInstanceOf[String])
    //val worker = new dom.Worker("worker.js")
    worker.onmessage = { x: Any =>
      RenderBackendImpl.receive(x
        .asInstanceOf[js.Dynamic].data
        .asInstanceOf[js.Array[RenderBackendApi.Message]]
      )
    }
    RenderBackendImpl.messageStream subscribe {
      x => worker.postMessage(x)
    }
    worker.postMessage(js.Array("start"))
  }
}
