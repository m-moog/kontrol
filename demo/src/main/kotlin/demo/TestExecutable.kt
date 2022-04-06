package demo

import org.kontrol.Executable
import org.kontrol.Kontrol
import org.kontrol.Kontrolled
import org.kontrol.injection

@Suppress("unused")
class TestExecutable: Executable {

    private val utilizesConstructorInjection: IUtilizeConstructorInjection by injection()


    private val testData: TestData by injection()
    private val testInterface: TestInterface by injection()


    init {
        Kontrol.logger.info { "this value was injected via field injection: '${testData.s}'" }
        Kontrol.logger.info { "this is a field injected value which itself was created using constructor injection: '$utilizesConstructorInjection'" }
    }

    override fun execute() {
        Kontrol.logger.debug { "This is a debug message. This will be hidden by default." }
        Kontrol.logger.info  { "this is from an interface: <${testInterface.s}>" }
    }
}

data class TestData(val s: String = "String"): Kontrolled

data class IUtilizeConstructorInjection(val td: TestData): Kontrolled

class TestClass(
    override val s: String = "test"
): Kontrolled, TestInterface
interface TestInterface { val s: String }