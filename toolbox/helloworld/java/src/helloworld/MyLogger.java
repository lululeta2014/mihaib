package helloworld;

import java.util.logging.Logger;

/**
 * MyLogger has a static field to the global logger. The rest of the code can
 * use this field. Currently (Sun JDK 7u45) Logger.getGlobal() doesn't log
 * anything but Logger.getLogger(Logger.GLOBAL_LOGGER_NAME) does. Remove this
 * class and just use Logger.getGlobal() when this issue is resolved.
 */
class MyLogger {

	/** The global logger, obtained as described above so that it logs. */
	static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

}
