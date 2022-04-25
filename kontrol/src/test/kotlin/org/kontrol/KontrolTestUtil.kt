package org.kontrol

import org.kontrol.logging.Logger
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.reflections.Reflections

/**
 * @return an instance of [Reflections] mocked using [org.mockito.kotlin]
 */
fun mockedReflections(vararg classesToReturn: Class<out Kontrolled>): Reflections =
    mock {
        on { getSubTypesOf(Kontrolled::class.java) } doReturn classesToReturn.toSet()
    }

/**
 * Will initialize [Kontrol] with a mocked instance of [Reflections]
 *
 * Actual scanning for classes will not happen. Instead, the supplied classes are returned.
 */
fun mockKontrol(vararg injectableClasses: Class<out Kontrolled>, logger: Logger = DummyLogger()){
    kontrol {
        packages = arrayOf()
        this.logger = logger
        reflections = mockedReflections(*injectableClasses)
    }
}

/**
 * same as [mockKontrol], but without catching Exceptions
 */
fun mockKontrolWithoutCatchingExceptions(vararg injectableClasses: Class<out Kontrolled>, logger: Logger = DummyLogger()){
    val kontrolConfig = KontrolConfig(
        packages = listOf(),
        logger = logger,
        reflections = mockedReflections(*injectableClasses)
    )
    Kontrol.init(kontrolConfig)
}