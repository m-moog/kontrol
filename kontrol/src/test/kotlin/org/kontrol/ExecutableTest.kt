@file:Suppress("ClassName")

package org.kontrol

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ExecutableTest_shouldExecute {
    companion object { var executed = false }

    @Test
    fun test(){
        mockKontrol(ICanBeExecuted::class.java)

        assert(executed)
    }

    class ICanBeExecuted: Executable {
        override fun execute() {
            executed = true
        }
    }
}

class ExecutableTest_shouldInjectValuesBeforeExecuting{
    companion object{
        var executed = false
    }

    @Test
    fun test(){
        mockKontrol(
            ExecutableWithValuesToInject::class.java,
            InjectableClass1::class.java,
            InjectableClass2::class.java,
            InjectableClass3::class.java
        )

        assert(executed)
    }

    data class ExecutableWithValuesToInject(
        val ic1: InjectableClass1,
        val ic2: InjectableClass2
    ): Executable{
        private val ic3: InjectableClass3 by injection()

        override fun execute() {
            executed = true

            assertEquals("test String", ic1.s)
            assertEquals(42, ic2.i)
            assertEquals(ic1, ic3.ic1)
            assert(ic1 === ic3.ic1)
            assertEquals(ic2, ic3.ic2)
            assert(ic2 === ic3.ic2)
        }
    }
    data class InjectableClass1(val s: String = "test String"                       ): Kontrolled
    data class InjectableClass2(val i: Int = 42                                     ): Kontrolled
    data class InjectableClass3(val ic1: InjectableClass1, val ic2: InjectableClass2): Kontrolled
}