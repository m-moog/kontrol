package org.kontrol

import org.kontrol.logging.Logger

/**Logger that just does nothing*/
class DummyLogger: Logger {
    override fun log(level: Logger.Level, exception: Throwable?, message: () -> String) {}
}