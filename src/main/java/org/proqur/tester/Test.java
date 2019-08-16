package org.proqur.tester;

import org.proqur.annotations.UpdateProperty;

public class Test {

	private String property;

	@UpdateProperty(file=".\\config.properties",property="test")
	public void setProperty(String property) {
		this.property = synchronizedProperty(property);
		System.out.printf("Property set to %s%n",property);
	}
	
	public String getProperty() {
		return synchronizedProperty(null);
	}
	
	private synchronized String synchronizedProperty(String s) {
		return s==null? this.property : s;
	}
	
}
