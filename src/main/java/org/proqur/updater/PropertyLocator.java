package org.proqur.updater;

/**
 * Commodity class describing a property-value pair.
 */
class PropertyLocator {
	
	private String propertyValue;
	private String propertyName;
	
	public PropertyLocator(String propertyName,String propertyValue) {
		this.propertyName=propertyName;
		this.propertyValue=propertyValue;
	}

	/**
	 * Returns the String-value of the property.
	 * 
	 * @return propertyValue
	 */
	public String getPropertyValue() {
		return propertyValue;
	}
	
	/**
	 * Returns the name of the property.
	 * 
	 * @return propertyName
	 */
	public String getPropertyName() {
		return propertyName;
	}

}
