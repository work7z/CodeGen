package cc.codegen.dsl.dto.spec.motion

class OutputFile {
    // The field below will finalize the name of generated file
    // By default, the formatting would be upperFirst style.
    String fileName;
    // The field below will determine in which the file will be putted.
    // by default it will be putted in the root folder
    String subFolderName;

    OutputFile(String fileName) {
        this.fileName = fileName
    }

    OutputFile(String fileName, String subFolderName) {
        this.fileName = fileName
        this.subFolderName = subFolderName
    }
}
