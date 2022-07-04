### How to start CodeGen service?

If you are using Windows operation system, please click **startup.bat** to start.

If you are using MacOS operation system, please click **startup.command** to start.

If you are using Linux operation system, please click **startup.sh** to start.

![3B815FB66E360B297AD335F47E1C09BF.png](https://codegen.cc/res/3B815FB66E360B297AD335F47E1C09BF.png)

### Can I run CodeGen service without a modal window?

Currently, we will launch a modal window to print latest program output. We truly sorry there' no integrated functions can be used, we will add the hide window feature in the following versions, and at that time you can enjoy CodeGen ToolBox service in the background thread.

If you really want this window to hide itself right now, you can write some script if you know how to hide it. :

1. Windows  
   You can create a system service on Windows, and specify its bat script as startup.bat.
1. Linux/Mac OSX  
   You can use nohup or &> /dev/null to run CodeGen service in the background
