@file:Suppress("ClassName")

package org.kontrol

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BasicInjectionTest_shouldInjectAll {

    @Test
    fun test() {
        mockKontrol(
            EmptyClass::class.java,
            ClassWithZeroArgConstructor::class.java,
            ClassThatSupportsConstructorInjection::class.java,
            ClassWithPropertyInjection::class.java,
            ClassWithConstructorAndPropertyInjection::class.java
        )

        assertNotNull( Kontrol.findInstance<EmptyClass>(Thread.currentThread()) )
        val zeroArgConst = Kontrol.findInstance<ClassWithZeroArgConstructor>(Thread.currentThread())
        assertEquals("testString", zeroArgConst.s)
        assertEquals(42, zeroArgConst.i)

        val constInject = Kontrol.findInstance<ClassThatSupportsConstructorInjection>(Thread.currentThread())
        assertEquals("testString", constInject.clazz.s)
        assertEquals(42, constInject.clazz.i)

        val propInject = Kontrol.findInstance<ClassWithPropertyInjection>(Thread.currentThread())
        assertNotNull(propInject.clazz)

        val propAndConst = Kontrol.findInstance<ClassWithConstructorAndPropertyInjection>(Thread.currentThread())


        assertEquals(zeroArgConst, propAndConst.clazz)
        assert( zeroArgConst === propAndConst.clazz )
        assertEquals( constInject, propAndConst.otherClazz)
        assert( constInject === propAndConst.otherClazz)
    }

    class EmptyClass: Kontrolled
    data class ClassWithZeroArgConstructor(
        val s: String = "testString",
        val i: Int = 42
    ): Kontrolled
    data class ClassThatSupportsConstructorInjection(
        val clazz: ClassWithZeroArgConstructor
    ): Kontrolled
    class ClassWithPropertyInjection: Kontrolled{
        val clazz: EmptyClass by injection()
    }
    data class ClassWithConstructorAndPropertyInjection(
        val clazz: ClassWithZeroArgConstructor
    ): Kontrolled{
        val otherClazz: ClassThatSupportsConstructorInjection by injection()
    }
}

class InterfaceInjectionTest_shouldInjectDerivedClasses{
    @Test
    fun test(){
        mockKontrol(TestClass::class.java)

        assertEquals( "test String", Kontrol.findInstance<TestInterface>(TestInterface::class.java, Thread.currentThread()).s )
    }

    interface TestInterface {
        val s: String
    }
    class TestClass(override val s: String = "test String") : TestInterface, Kontrolled
}

class InjectionTest_classWithoutValidConstructor_shouldThrowException{
    @Test
    fun test(){
        mockKontrolWithoutCatchingExceptions(NoValidConstructor::class.java, InvalidPropertyInjection::class.java)
        assertThrows<ConstructorInjectionException> {
            Kontrol.findInstance<NoValidConstructor>(Thread.currentThread())
        }

        val v = Kontrol.findInstance<InvalidPropertyInjection>(Thread.currentThread())
        assertThrows<FieldInjectionException>{
            v.s
        }
    }

    class NoValidConstructor(val i: Int): Kontrolled
    class InvalidPropertyInjection: Kontrolled{
        val s: Interface by injection()
    }
    interface Interface
}