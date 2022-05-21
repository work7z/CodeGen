package cc.codegen.dsl.dto.connotations

import cc.codegen.dsl.dto.mapping.DataType


class LogicRun {
    public void a() {
        println "hello, world"
        println DataType.DatabaseOriginalType.LONGTEXT
    }

    public void nnn(DataType.DatabaseOriginalType abcd) {
        println abcd
    }
}
