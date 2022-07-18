### 如何啓動CodeGen服務？

如果您使用的是Windows操作系統，請點擊**startup.bat**開始。

如果您使用的是MacOS操作系統，請點擊**startup.command**開始。

如果您使用的是Linux操作系統，請點擊**startup.sh**開始。

![3B815FB66E360B297AD335F47E1C09BF.png](https://codegen.cc/res/3B815FB66E360B297AD335F47E1C09BF.png)

### 我可以在沒有模式窗口的情況下運行CodeGen服務嗎？

目前，我們將啓動一個模式窗口來打印最新的程序輸出。很抱歉沒有集成的功能可以使用，我們將在後續版本中添加隱藏窗口功能，屆時您可以在後臺線程中享受CodeGen工具箱服務。

如果你真的想讓這個窗口立刻隱藏起來，你可以編寫一些腳本，如果你知道如何隱藏它的話。:

1. Windows  
   您可以在Windows上創建系統服務，並將其BAT腳本指定爲Startup.bat。
1. Linux/Mac OSX  
   您可以使用nohup或&> /dev/null在後臺運行CodeGen服務
