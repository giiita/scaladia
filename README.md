# scaladia

Scaladia is a lightweight DI container with peripheral tools.

## How to use

```
libraryDependencies += "com.github.giiita" %% "scaladia" % "0.7.0"
````

## Examples

### The simplest Injection

```
object A extends A {
  depends[A](this).acceptedGlobal
}

trait A extends AutoInjector
```

```
object TestA extends Injector {
  def test = {
    println(inject[A]) // B
  }
}
```

Classes that inherit AutoInjector are registered automatically when DI container is initialized.<br/>
Resolve dependencies from the class loader of the thread as seen from Scaladia 's static initializer.<br/>
However, when automatically injecting the object definition, it must be defined in a statically accessible hierarchy, such as top level.<br/>
In AutoInjector, when multiple dependencies of the same type are registered, it is not guaranteed which one is injected.
Please use Injector in such a case.
You can overwrite settings injected with AutoInjector with Injector.<br/>
<br/>
Currently, AutoInjector is an interface for injecting self type.<br/>

Even if you set injection settings other than self type, it may not initialize to the correct order and may not behave as intended.<br/><br/><br/>



AutoInjectorを継承したクラスは、DIコンテナの初期化時に自動的に登録されます。<br/>
Scaladiaのstatic initializerから見たスレッドのクラスローダーから依存関係を解決します。<br/>
ただし、object定義を自動注入する場合、トップレベルなど、静的にアクセスできる階層に定義しなければなりません。<br/>
AutoInjectorでは、同じタイプの複数の依存関係が登録されている場合、どちらが注入されるかは保証されません。
その場合、Injectorを使用してください。
AutoInjectorで注入した設定をInjectorで上書きすることができます。

現在、AutoInjectorは自己タイプを注入するためのインターフェースであり、
自己タイプ以外の注入設定をしても、正しい順序に初期化されず、意図した挙動をしないことがあります。


### Custom usage

```
  trait A
  object B extends A
  object C extends A
  
  trait BInjector extends Injector {
    depends[A](B)
  }
  
  object Test extends BInjector {
    def test = {
      println(inject[A]) // B
    }
  }
  
```

### This will not work

```
  trait A
  object B extends A
  object C extends A
  
  trait X
  object Y extends X
  object Z extends X
  
  trait BInjector extends Injector {
    depends[A](B)
  }
  
  trait ZInjector extends Injector {
    depends[X](Z)
  }
  
  object TestA extends BInjector {
    def test = {
      println(inject[A]) // B
    }
  }
  
  object TestX extends ZInjector {
    def test = {
      println(inject[A]) // Uninjectable object. trait A
    }
  }
```

### It corresponds to linearization.

```
  trait A
  object B extends A
  object C extends A
  
  trait BInjector extends Injector {
    depends[A](B)
  }
  
  trait CInjector extends Injector {
    depends[A](C)
  }
  
  object TestA extends BInjector with CInjector {
    def test = {
      println(inject[A]) // C
    }
  }
```
