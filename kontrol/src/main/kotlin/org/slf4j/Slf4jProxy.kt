package org.slf4j

import org.kontrol.Kontrol
import org.kontrol.logging.KLog

@Suppress("unused", "UNUSED_PARAMETER")
object LoggerFactory {
    @JvmStatic
    fun getLogger(unused: Class<*>?): Logger {
        val logger = try {
            Kontrol.logger
        }catch(e: Throwable){
            KLog()
        }

        return LoggerAdapter(logger)
    }
}

@Suppress("RemoveRedundantCallsOfConversionMethods")
class LoggerAdapter(private val logger: org.kontrol.logging.Logger): Logger {
    override fun getName() = "Logger Adapter from <slf4j> to <${KLog::class.qualifiedName}>"
    override fun info (arg0: String, arg1: Any, arg2: Any              ) = logger.log(org.kontrol.logging.Logger.Level.INFO       ) {arg0.toString() + arg1.toString() + arg2.toString()                  }
    override fun info (arg0: String, arg1: Array<*>                    ) = logger.log(org.kontrol.logging.Logger.Level.INFO       ) {arg0.toString() + arg1.joinToString(separator = "")                  }
    override fun info (arg0: String, arg1: Throwable                   ) = logger.log(org.kontrol.logging.Logger.Level.INFO , arg1) {arg0.toString()                                                      }
    override fun info (arg0: String, arg1: Any                         ) = logger.log(org.kontrol.logging.Logger.Level.INFO       ) {arg0.toString() + arg1.toString()                                    }
    override fun info (arg0: String                                    ) = logger.log(org.kontrol.logging.Logger.Level.INFO       ) {arg0.toString()                                                      }
    override fun info (arg0: Marker, arg1: String, arg2: Throwable     ) = logger.log(org.kontrol.logging.Logger.Level.INFO , arg2) {arg0.toString() + arg1.toString()                                    }
    override fun info (arg0: Marker, arg1: String, arg2: Array<*>      ) = logger.log(org.kontrol.logging.Logger.Level.INFO       ) {arg0.toString() + arg1.toString() + arg2.joinToString(separator = "")}
    override fun info (arg0: Marker, arg1: String, arg2: Any, arg3: Any) = logger.log(org.kontrol.logging.Logger.Level.INFO       ) {arg0.toString() + arg1.toString() + arg2.toString() + arg3.toString()}
    override fun info (arg0: Marker, arg1: String, arg2: Any           ) = logger.log(org.kontrol.logging.Logger.Level.INFO       ) {arg0.toString() + arg1.toString() + arg2.toString()                  }
    override fun info (arg0: Marker, arg1: String                      ) = logger.log(org.kontrol.logging.Logger.Level.INFO       ) {arg0.toString() + arg1.toString()                                    }
    override fun trace(arg0: Marker, arg1: String                      ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString() + arg1.toString()                                    }
    override fun trace(arg0: Marker, arg1: String, arg2: Any           ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString() + arg1.toString() + arg2.toString()                  }
    override fun trace(arg0: Marker, arg1: String, arg2: Any, arg3: Any) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString() + arg1.toString() + arg2.toString() + arg3.toString()}
    override fun trace(arg0: String, arg1: Any                         ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString() + arg1.toString()                                    }
    override fun trace(arg0: String, arg1: Any, arg2: Any              ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString() + arg1.toString() + arg2.toString()                  }
    override fun trace(arg0: String, arg1: Array<*>                    ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString() + arg1.joinToString(separator = "")                  }
    override fun trace(arg0: String                                    ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString()                                                      }
    override fun trace(arg0: String, arg1: Throwable                   ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG, arg1) {arg0.toString()                                                      }
    override fun trace(arg0: Marker, arg1: String, arg2: Array<*>      ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString() + arg1.toString() + arg2.joinToString(separator = "")}
    override fun trace(arg0: Marker, arg1: String, arg2: Throwable     ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG, arg2) {arg0.toString() + arg1.toString()                                    }
    override fun debug(arg0: Marker, arg1: String, arg2: Array<*>      ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString() + arg1.toString() + arg2.joinToString(separator = "")}
    override fun debug(arg0: String, arg1: Array<*>                    ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString() + arg1.joinToString(separator = "")                  }
    override fun debug(arg0: String, arg1: Any                         ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString() + arg1.toString()                                    }
    override fun debug(arg0: String                                    ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString()                                                      }
    override fun debug(arg0: Marker, arg1: String                      ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString() + arg1.toString()                                    }
    override fun debug(arg0: String, arg1: Any, arg2: Any              ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString() + arg1.toString() + arg2.toString()                  }
    override fun debug(arg0: Marker, arg1: String, arg2: Any, arg3: Any) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString() + arg1.toString() + arg2.toString() + arg3.toString()}
    override fun debug(arg0: Marker, arg1: String, arg2: Any           ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG      ) {arg0.toString() + arg1.toString() + arg2.toString()                  }
    override fun debug(arg0: Marker, arg1: String, arg2: Throwable     ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG, arg2) {arg0.toString() + arg1.toString()                                    }
    override fun debug(arg0: String, arg1: Throwable                   ) = logger.log(org.kontrol.logging.Logger.Level.DEBUG, arg1) {arg0.toString()                                                      }
    override fun error(arg0: String, arg1: Any                         ) = logger.log(org.kontrol.logging.Logger.Level.ERROR      ) {arg0.toString() + arg1.toString()                                    }
    override fun error(arg0: String                                    ) = logger.log(org.kontrol.logging.Logger.Level.ERROR      ) {arg0.toString()                                                      }
    override fun error(arg0: String, arg1: Any, arg2: Any              ) = logger.log(org.kontrol.logging.Logger.Level.ERROR      ) {arg0.toString() + arg1.toString() + arg2.toString()                  }
    override fun error(arg0: String, arg1: Array<*>                    ) = logger.log(org.kontrol.logging.Logger.Level.ERROR      ) {arg0.toString() + arg1.joinToString(separator = "")                  }
    override fun error(arg0: Marker, arg1: String, arg2: Array<*>      ) = logger.log(org.kontrol.logging.Logger.Level.ERROR      ) {arg0.toString() + arg1.toString() + arg2.joinToString(separator = "")}
    override fun error(arg0: Marker, arg1: String, arg2: Any, arg3: Any) = logger.log(org.kontrol.logging.Logger.Level.ERROR      ) {arg0.toString() + arg1.toString() + arg2.toString() + arg3.toString()}
    override fun error(arg0: Marker, arg1: String, arg2: Any           ) = logger.log(org.kontrol.logging.Logger.Level.ERROR      ) {arg0.toString() + arg1.toString() + arg2.toString()                  }
    override fun error(arg0: Marker, arg1: String                      ) = logger.log(org.kontrol.logging.Logger.Level.ERROR      ) {arg0.toString() + arg1.toString()                                    }
    override fun error(arg0: String, arg1: Throwable                   ) = logger.log(org.kontrol.logging.Logger.Level.ERROR, arg1) {arg0.toString()                                                      }
    override fun error(arg0: Marker, arg1: String, arg2: Throwable     ) = logger.log(org.kontrol.logging.Logger.Level.ERROR, arg2) {arg0.toString() + arg1.toString()                                    }
    override fun warn (arg0: Marker, arg1: String, arg2: Throwable     ) = logger.log(org.kontrol.logging.Logger.Level.WARN , arg2) {arg0.toString() + arg1.toString()                                    }
    override fun warn (arg0: Marker, arg1: String, arg2: Array<*>      ) = logger.log(org.kontrol.logging.Logger.Level.WARN       ) {arg0.toString() + arg1.toString() + arg2.joinToString(separator = "")}
    override fun warn (arg0: Marker, arg1: String, arg2: Any, arg3: Any) = logger.log(org.kontrol.logging.Logger.Level.WARN       ) {arg0.toString() + arg1.toString() + arg2.toString() + arg3.toString()}
    override fun warn (arg0: String, arg1: Any                         ) = logger.log(org.kontrol.logging.Logger.Level.WARN       ) {arg0.toString() + arg1.toString()                                    }
    override fun warn (arg0: String                                    ) = logger.log(org.kontrol.logging.Logger.Level.WARN       ) {arg0.toString()                                                      }
    override fun warn (arg0: String, arg1: Array<*>                    ) = logger.log(org.kontrol.logging.Logger.Level.WARN       ) {arg0.toString() + arg1.joinToString(separator = "")                  }
    override fun warn (arg0: String, arg1: Any, arg2: Any              ) = logger.log(org.kontrol.logging.Logger.Level.WARN       ) {arg0.toString() + arg1.toString() + arg2.toString()                  }
    override fun warn (arg0: String, arg1: Throwable                   ) = logger.log(org.kontrol.logging.Logger.Level.WARN , arg1) {arg0.toString()                                                      }
    override fun warn (arg0: Marker, arg1: String, arg2: Any           ) = logger.log(org.kontrol.logging.Logger.Level.WARN       ) {arg0.toString() + arg1.toString() + arg2.toString()                  }
    override fun warn (arg0: Marker, arg1: String                      ) = logger.log(org.kontrol.logging.Logger.Level.WARN       ) {arg0.toString() + arg1.toString()                                    }
    override fun isTraceEnabled(arg0: Marker) = true
    override fun isTraceEnabled(            ) = true
    override fun isErrorEnabled(            ) = true
    override fun isErrorEnabled(arg0: Marker) = true
    override fun isDebugEnabled(arg0: Marker) = true
    override fun isDebugEnabled(            ) = true
    override fun isInfoEnabled (arg0: Marker) = true
    override fun isInfoEnabled (            ) = true
    override fun isWarnEnabled (            ) = true
    override fun isWarnEnabled (arg0: Marker) = true
}