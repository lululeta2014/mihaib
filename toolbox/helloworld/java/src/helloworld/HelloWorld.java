package helloworld;

import static helloworld.MyLogger.logger;

public class HelloWorld {

	/** Prints ‘Hello World!’ to standard output. */
	public static void main(String[] args) {
		logger.info("program start");
		System.out.println("Hello World!");
	}

}
