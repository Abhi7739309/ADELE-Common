package fr.liglab.adele.commons.test.utils;

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
