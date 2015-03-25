package moorka.ui.plugin

import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.util.zip.{ZipEntry, ZipInputStream}
import scala.collection.mutable.Queue

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
    val log = (streams in collectResources).value.log
    val task = new CollectResourcesTask(
      (collectJsResources in collectResources).value,
      (collectCssResources in collectResources).value,
      (collectBinaryResources in collectResources).value,
      (conflictsResolveStrategy in collectResources).value,
      (collectOutputPath in collectResources).value,
      log)
    log.info("Collecting external resources")
    task.collectExternalResources((externalDependencyClasspath in collectResources).value)

    log.info("Collecting dependend project resources")
    // Next line causes weird and shitty error. Do not uncomment
    //task.collectInternalResources((internalDependencyClasspath in collectResources).value)
    // Next three lines and corresponding functions really look like a pile of GOVNOKOD
    // But an idea is follows: list project dependencies, get resources from deps directories and collect it
    // TODO: Of course, it works. But it MUST be rewritten ASAP.
    val deps = uniqueBuildDeps((buildDependencies in collectResources).value)
    val resDirs = deps flatMap { d => depResources(d) }
    task.collectDirectoryResources(resDirs)

    log.info("Collecting internal resources")
    task.collectDirectoryResources((resourceDirectories in collectResources).value)


  }

  private def uniqueBuildDeps(defs: BuildDependencies): Seq[File] = {
    val deps = defs.classpath.keys.map(_.build.toString)
    val set = scala.collection.mutable.Set[String]()
    deps foreach { p =>
      if(!set.contains(p))
        set.add(p)
    }
    set.toList.map(s => new File(new URI(s)))
  }

  private def depResources(d: File): Seq[File] = {
    var l = List[File]()
    val q = new Queue[File]()
    q.enqueue(d)
    while(!q.isEmpty) {
      val e = q.dequeue()
      if(e.isDirectory) {
        if(e.getName == "resources") {
          l = e :: l
        } else {
          e.listFiles foreach { f => q.enqueue(f) }
        }
      }
    }
    l
  }

  class ResolveException(cause: String) extends Exception(cause)

  sealed trait ResolveStrategy
  object ResolveStrategy {
    case object Rewrite extends ResolveStrategy
    case object Discard extends ResolveStrategy
    case object FireException extends ResolveStrategy
  }

  class CollectResourcesTask(collectJs: Boolean,
                             collectCss: Boolean,
                             collectBinary: Boolean,
                             resolveStrategy: ResolveStrategy,
                             outputDirectory: File,
                             log: Logger){
    val binaryExtensions: Seq[String] = Seq("png", "jpeg", "jpg", "svg")

    private def isImage( resource: String ) = !binaryExtensions.filter( e => resource.endsWith(e) ).isEmpty

    private def shouldCollect(fileName: String) = {
      (collectJs && fileName.endsWith(".css")) ||
        (collectCss && fileName.endsWith(".js") && collectCss) ||
        (collectBinary && isImage(fileName))
    }

    private def readZipData(zi: ZipInputStream, entry: ZipEntry) = {
      var data: Array[Byte] = null
      if(entry.getSize >= 0) {
        data = new Array[Byte](entry.getSize.toInt)
        zi.read(data)
      } else {
        val out = new ByteOutputStream()
        var read = zi.read()
        while(read != -1) {
          out.write(read)
          read = zi.read()
        }
        data = new Array[Byte](out.getCount)
        System.arraycopy(out.getBytes, 0, data, 0, out.getCount)
      }
      data
    }

    private def getFileName(name: String) = {
      val separatorPos = name.lastIndexOf('/')
      if(separatorPos >= 0) name.substring(separatorPos) else name
    }

    private def writeFileData(data: Array[Byte], resourceFile: File) {
      FileUtil.copy(new ByteArrayInputStream(data), resourceFile, null)

    }

    private def writeFile(data: Array[Byte], fileName: String) = {
      val file = new File(outputDirectory, fileName)
      if(file.exists) {
        resolveStrategy match {
          case ResolveStrategy.Rewrite =>
            log.debug(f"Overwriting resource ${fileName}")
            writeFileData(data, file)
          case ResolveStrategy.FireException =>
            throw new ResolveException(s"Resource conflict: ${file}")
          case ResolveStrategy.Discard =>
            log.debug(s"Ignoring conflict file: ${file.getAbsolutePath}")
        }
      } else {
        writeFileData(data, file)
        log.debug(f"Resource ${fileName} has been collected")
      }
    }

    private def copyFile(source: File, fileName: String) = {
      var file = new File(outputDirectory, fileName)
      if(file.exists) {
        resolveStrategy match {
          case ResolveStrategy.Rewrite =>
            log.debug(s"Overwriting file ${file.getName} with ${source}")
            FileUtil.copy(source, file, null, true)
          case ResolveStrategy.FireException =>
            throw new ResolveException(s"Resource conflict ${file}")
          case ResolveStrategy.Discard =>
            log.debug(s"Ignoring conflict file: ${source}")
        }
      } else {
        FileUtil.copy(source, file, null, true)
        log.debug(s"Resource ${source} has been collected")
      }
    }

    private def collectDirectoryResources(entry: File) = {
      val q = new Queue[File]
      q.enqueue(entry)
      while(!q.isEmpty) {
        val e = q.dequeue()
        if(e.isDirectory) {
          log.debug(s"Collecting resources from directory ${e}")
          e.listFiles foreach { q.enqueue(_) }
        } else {
          val fileName = getFileName(e.getName)
          if(shouldCollect(fileName)) {
            copyFile(e, fileName)
          }
        }
      }
    }

    private def ensureOutputDirectory() = {
      if(!outputDirectory.exists)
        log.debug(s"Creating output directory ${outputDirectory}")
        outputDirectory.mkdirs()
    }

    def collectExternalResources(projectClassPath: Seq[Attributed[File]]): Unit = {
      ensureOutputDirectory()
      projectClassPath foreach { f =>
        val zi = new ZipInputStream(Files.newInputStream(f.data.toPath))
        var entry : ZipEntry = zi.getNextEntry
        while ( entry != null ) {
          val fileName = getFileName(entry.getName)
          if(shouldCollect(fileName)) {
            var data = readZipData(zi, entry)
            writeFile(data, fileName)
          }
          entry = zi.getNextEntry
        }
      }
    }

    def collectInternalResources(paths: Seq[Attributed[File]]): Unit = {
      ensureOutputDirectory()
      paths foreach { f => collectDirectoryResources(f.data) }
    }

    def collectDirectoryResources(directories: Seq[File]): Unit = {
      ensureOutputDirectory()
      directories foreach { d => collectDirectoryResources(d) }
    }
  }
}
