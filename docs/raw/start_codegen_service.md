### ${t('How to start CodeGen service?')}

${t("If you are using Windows operation system, please click {0} to start.",'**startup.bat**')}

${t("If you are using MacOS operation system, please click {0} to start.",'**startup.command**')}

${t("If you are using Linux operation system, please click {0} to start.",'**startup.sh**')}

![3B815FB66E360B297AD335F47E1C09BF.png](https://cloud.codegen.cc/res/3B815FB66E360B297AD335F47E1C09BF.png)

### ${t("Can I run CodeGen service without a modal window?")}

${t("Currently, we will launch a modal window to print latest program output. We truly sorry there' no integrated functions can be used, we will add the hide window feature in the following versions, and at that time you can enjoy CodeGen ToolBox service in the background thread.")}

${t("If you really want this window to hide itself right now, you can write some script if you know how to hide it. ")}:

1. Windows  
   ${t("You can create a system service on Windows, and specify its bat script as startup.bat.")}
1. Linux/Mac OSX  
   ${t("You can use nohup or {0} to run CodeGen service in the background", "&> /dev/null")}
