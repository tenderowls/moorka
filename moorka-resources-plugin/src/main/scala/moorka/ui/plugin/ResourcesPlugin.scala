package moorka.ui.plugin

import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.util.zip.{ZipEntry, ZipInputStream}

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import moorka.ui.plugin.ResourcesPlugin.ResolveStrategy.{FireException, Rewrite}
import org.apache.ivy.util.FileUtil
import sbt._
import sbt.Keys._

/**
 * Created by Cyril on 16.02.2015.
 */
object ResourcesPlugin extends AutoPlugin {
  override def requires = sbt.plugins.JvmPlugin

  // This plugin is automatically enabled for projects which are JvmPlugin.
  override def trigger = allRequirements

  val autoImport = AutoImport
  object AutoImport {
    val collectResources = taskKey[Unit]("Gather resource files from the project dependencies")
    val collectArtifactGroups = settingKey[Option[Seq[String]]]("Group constraint on the artifacts which are going " +
      "to be processed (whole classpath) - if wasn't specified)")
    val collectOutputPath = settingKey[File]("Directory where collected resources will be placed")
    val conflictsResolveStrategy = settingKey[ResolveStrategy]("Chosen strategy defines how conflicts (if any) will be resolved")
    val collectJsResources = settingKey[Boolean]("Do JS resources collection")
    val collectCssResources = settingKey[Boolean]("Do CSS resources collection")
    val collectBinaryResources = settingKey[Boolean]("Do binary resources (images) collection")

    lazy val collectResourcesSettings: Seq[Def.Setting[_]] = Seq(
      collectResources := collectTaskResources(collectResources).value,
      collectArtifactGroups in collectResources := None,
      collectOutputPath in collectResources := (classDirectory in Compile).value,
      conflictsResolveStrategy in collectResources := ResolveStrategy.Rewrite,
      collectBinaryResources in collectResources := true,
      collectCssResources in collectResources := true,
      collectJsResources in collectResources := true,
      (compile in Compile) <<= (compile in Compile) dependsOn collectResources
    )
  }

  import autoImport._

  override val projectSettings =
    inConfig(Compile)(collectResourcesSettings) ++
      inConfig(Test)(collectResourcesSettings)

  private def collectTaskResources(key:TaskKey[Unit]): Def.Initialize[Task[Unit]] = Def.task {
    CollectResourcesTask((collectJsResources in collectResources).value,
      (collectCssResources in collectResources).value,
      (collectBinaryResources in collectResources).value,
      (externalDependencyClasspath in collectResources).value,
      (collectArtifactGroups in collectResources).value,
      (conflictsResolveStrategy in collectResources).value,
      (collectOutputPath in collectResources).value)
  }

  class ResolveException(cause: String) extends Exception(cause)

  sealed trait ResolveStrategy
  object ResolveStrategy {
    case object Rewrite extends ResolveStrategy
    case object Discard extends ResolveStrategy
    case object FireException extends ResolveStrategy
  }

  object CollectResourcesTask {
    val binaryExtensions: Seq[String] = Seq("png", "jpeg", "jpg", "svg")

    private def isImage( resource: String ) = !binaryExtensions.filter( e => resource.endsWith(e) ).isEmpty

    def apply(js: Boolean, css: Boolean, binary: Boolean, projectClassPath: Seq[Attributed[File]],
               artifactGroup: Option[Seq[String]],
               resolveStrategy: ResolveStrategy,
               output: File): Unit = {
      projectClassPath.map {
        f =>
          val zi = new ZipInputStream(Files.newInputStream(f.data.toPath))
          var entry : ZipEntry = zi.getNextEntry
          while ( entry != null ) {
            (entry.getName match {
              case x if x.endsWith(".css") && css => true
              case x if x.endsWith(".js") && js => true
              case x if isImage(x) && binary => true
              case _ => false
            }) match {
              case true =>
                var data: Array[Byte] = null
                if ( entry.getSize >= 0 ) {
                  data = new Array[Byte](entry.getSize.toInt)
                  zi.read(data)
                } else {
                  val out = new ByteOutputStream()
                  var read = zi.read()
                  while ( read != -1 ) {
                    out.write(read)
                    read = zi.read()
                  }
                  data = out.toByteArray
                }

                def doResourceCopy(resourceFile: File) {
                  FileUtil.copy(new ByteArrayInputStream(data), resourceFile, null)
                  println(f"Resource ${resourceFile.getAbsolutePath} has been collected")
                }

                val resName = if (entry.getName.lastIndexOf("/")>= 0) entry.getName.substring(entry.getName.lastIndexOf("/"))
                  else entry.getName
                    
                 val file = new File(output, resName)
                 file match {
                  case resourceFile if resourceFile.exists()=> {
                     resolveStrategy match {
                      case ResolveStrategy.Rewrite => {
                        doResourceCopy(resourceFile)
                      }
                      case ResolveStrategy.FireException => {
                        throw new ResolveException(f"resource conflict: $output")
                      }
                      case ResolveStrategy.Discard => {
                        println(f"Ignoring conflict file: ${resourceFile.getAbsolutePath}")
                      }
                    }
                  }
                  case resourceFile =>
                    resourceFile.createNewFile()
                    doResourceCopy(resourceFile)
                }
              case false =>
            }

            entry = zi.getNextEntry
          }
      }
    }
  }

}
