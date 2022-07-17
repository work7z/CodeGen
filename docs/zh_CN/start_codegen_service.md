### 如何启动CodeGen服务？

如果您使用的是Windows操作系统，请点击**startup.bat**开始。

如果您使用的是MacOS操作系统，请点击**startup.command**开始。

如果您使用的是Linux操作系统，请点击**startup.sh**开始。

![3B815FB66E360B297AD335F47E1C09BF.png](https://cloud.codegen.cc/res/3B815FB66E360B297AD335F47E1C09BF.png)

### 我可以在没有模式窗口的情况下运行CodeGen服务吗？

目前，我们将启动一个模式窗口来打印最新的程序输出。很抱歉没有集成的功能可以使用，我们将在后续版本中添加隐藏窗口功能，届时您可以在后台线程中享受CodeGen工具箱服务。

如果你真的想让这个窗口立刻隐藏起来，你可以编写一些脚本，如果你知道如何隐藏它的话。:

1. Windows  
   您可以在Windows上创建系统服务，并将其BAT脚本指定为Startup.bat。
1. Linux/Mac OSX  
   您可以使用nohup或&> /dev/null在后台运行CodeGen服务
