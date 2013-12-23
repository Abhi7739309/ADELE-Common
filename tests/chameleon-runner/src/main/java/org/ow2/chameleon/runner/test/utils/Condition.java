package org.ow2.chameleon.runner.test.utils;

/**
 * 
 * @author Gabriel
 *
 */
public interface Condition {
	
	/**
	 * Verifies if the condition is checked
	 * @return
	 */
   boolean isChecked();

   /**
    * Gets the description of the condition
    * @return
    */
   String getDescription();
}
