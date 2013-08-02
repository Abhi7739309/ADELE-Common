package fr.liglab.adele.commons.test.utils;

import org.junit.Assert;


public class TestUtils {

	/**
	 * Determines if condition is checked, fails if it takes longer than that number of milliseconds.
	 * @param condition the condition
	 * @param timeout the timeout
	 * @param attempts the attempts number
	 */
	public static void testConditionWithTimeout(Condition condition, int timeout, int attempts) {
		int period = timeout / attempts;
		for (int i = 0; i < attempts; i++) {
			if (condition.isChecked()) {
				return;
			}
			try {
				Thread.sleep(period);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Assert.fail(condition.getDescription() + " failed.");		
	}
	
	public static void testConditionWithTimeout(Condition condition) {
		testConditionWithTimeout(condition, 1000, 5);
	}
	
}
