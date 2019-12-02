package refuel.container

import java.net.{URI, URL}

import refuel.exception.DIAutoInitializationException
import refuel.injector.scope.IndexedSymbol
import refuel.injector.{AutoInject, InjectionPool, Injector}
import refuel.internal.ClassTypeAcceptContext
import refuel.runtime.InjectionReflector

import scala.annotation.tailrec
import scala.reflect.runtime.universe


object RuntimeReflector extends InjectionReflector with Injector {

  def mirror: universe.Mirror = universe.runtimeMirror(getClass.getClassLoader)

  /**
    * Create injection applyment.
    *
    * @param symbols module symbols
    * @tparam T injection type
    * @return
    */
  override def reflectClass[T](clazz: Class[_], ip: InjectionPool)(c: Container)(symbols: Set[universe.ClassSymbol])(implicit wtt: universe.WeakTypeTag[T]): Set[IndexedSymbol[T]] = {
    symbols.map { x =>
      val pcInject = x.primaryConstructor.asMethod.paramLists.flatten.map { prm =>
        val tpe: universe.WeakTypeTag[_] = universe.WeakTypeTag(wtt.mirror, new reflect.api.TypeCreator {
          def apply[U <: reflect.api.Universe with Singleton](m: reflect.api.Mirror[U]) = {
            assert(m eq mirror, s"TypeTag[$prm] defined in $mirror cannot be migrated to $m.")
            prm.typeSignature.asInstanceOf[U#Type]
          }
        })
        c.find(clazz)(tpe, ClassTypeAcceptContext).orElse {
          ip.collect(clazz)(tpe).apply(c).toVector
            .sortBy(_.priority)(Ordering.Int.reverse)
            .headOption
            .map(_.value)
        } match {
          case Some(r) => r
          case None => throw new DIAutoInitializationException(s"Injectable parameter ${tpe.tpe} of ${implicitly[universe.WeakTypeTag[T]].tpe} constructor not found", null)
        }
      }

      mirror.reflectClass(x)
        .reflectConstructor(x.primaryConstructor.asMethod)
        .apply(pcInject: _*)
        .asInstanceOf[AutoInject[T]] match {
        case ai => ai.flush(c)
      }
    }
  }

  /**
    * Create injection applyment.
    *
    * @param symbols module symbols
    * @tparam T injection type
    * @return
    */
  override def reflectModule[T: universe.WeakTypeTag](c: Container)(symbols: Set[universe.ModuleSymbol]): Set[IndexedSymbol[T]] = {
    symbols.map { x =>
      mirror.reflectModule(x)
        .instance
        .asInstanceOf[AutoInject[T]] match {
        case ai => ai.flush(c)
      }
    }
  }

  /**
    * Reflect to a runtime class.
    *
    * @param t Type symbol.
    * @return
    */
  override def reflectClass(t: universe.Type): universe.RuntimeClass = {
    mirror.runtimeClass(t)
  }

  @tailrec
  private[this] def getClassLoaderUrls(cl: ClassLoader): Seq[URL] = {
    cl match {
      case null => Nil
      case x: java.net.URLClassLoader => x.getURLs.toSeq
      case x => getClassLoaderUrls(x.getParent)
    }
  }

  final def classpathUrls: List[URL] = {

    val FILE_SCHEME = "file:%s"
    val JAR_SCHEME = "jar:file:%s!/"
    val IGNORE_PATHS = Seq(
      " ",
      "scala-reflect.jar",
      "scala-library.jar",
      "sbt-launch.jar"
    )

    import collection.JavaConverters._

    System.getProperty("java.class.path")
      .split(":")
      .++(this.getClass.getClassLoader.getResources("").asScala.map(_.getPath))
      .++(getClassLoaderUrls(this.getClass.getClassLoader).map(_.getPath))
      .distinct
      .withFilter(x => IGNORE_PATHS.forall(!x.contains(_)))
      .map {
        case x if x.endsWith(".jar") => JAR_SCHEME.format(x)
        case x => FILE_SCHEME.format(x)
      }.map(new URI(_).toURL).toList
  }
}
