package cc.codegen.dsl.dto.test

import cc.codegen.dsl.dto.connotations.LogicRun

class TestFunc {
    public void test() {
        System.out.println("java lang logic")

        LogicRun logicRun = new LogicRun()
        logicRun.a()
    }

    static void main(String[] args) {
        println "hello, world"

    }
}
