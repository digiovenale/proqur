package org.proqur.updater;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Class representing a single properties file.
 * It associates each property to the setter method, which
 * is to be called when the property must be updated.
 * 
 */
class PropertiesFileDescriptor {
	
	private Map<String, List<BeanMethod>> propertiesMap;
	private String filePath;
	
	/**
	 * <p>Constructor for the class PropertiesFileDescriptor.
	 * The constructors requires a String representing the path to the properties file.
	 * Since a PropertiesFileDescriptor stands for an existing file,
	 * passing a <b>null</b> value to the constructor will throw a
	 * IllegalStateException.</p>
	 * @param filePath
	 */
	PropertiesFileDescriptor(String filePath){
		if(filePath == null) throw new IllegalStateException("FilePath for PropertiesFileDescritor cannot be null");
		
		this.filePath=filePath;
		this.propertiesMap = new HashMap<String,List<BeanMethod>>();
	}
	
	/**
	 * Adds the given bean's Method to the list of the methods called
	 * when a change in the property is detected.
	 * The method passed in <em>addMethod</em> must be the setter method for
	 * the given property.
	 * The property itself must be present in the properties file
	 * represented by the PropertiesFileDescriptor object.
	 * 
	 * @param property
	 * @param bean
	 * @param method
	 */
	void addMethod(String property,Object bean,Method method){
		List<BeanMethod> listAssociatedWithAGivenProperty = propertiesMap.get(property);
		if(listAssociatedWithAGivenProperty!= null) {
			listAssociatedWithAGivenProperty.add(new BeanMethod(bean,method));
		} else {
			listAssociatedWithAGivenProperty = new ArrayList<BeanMethod>();
			listAssociatedWithAGivenProperty.add(new BeanMethod(bean,method));
			propertiesMap.put(property, listAssociatedWithAGivenProperty);
		}
	}
	
	/**
	 * Calls all the setter methods associated with the given PropertyLocators.
	 * The methods called update the fields with the new properties' values.
	 *  
	 * @param listPropertiesChanged
	 */
	void updateProperties(PropertyLocator[] listPropertiesChanged) {
		for(PropertyLocator property : listPropertiesChanged) {
			List<BeanMethod> methodsToUpdate = propertiesMap.get(property.getPropertyName());
			if(methodsToUpdate == null) continue;
			
			for(BeanMethod beanMethod : methodsToUpdate) {
				beanMethod.updateProperty(property.getPropertyValue());
			}
		}
		return;
	}
	
	/**
	 * Returns the filePath of the current PropertiesFileDescriptor.
	 * @return filePath
	 */
	String getFilePath() {
		return filePath;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(obj instanceof PropertiesFileDescriptor) {
			return ((PropertiesFileDescriptor) obj).getFilePath().equals(this.getFilePath());
		} else if (obj instanceof String) {
			return obj.equals(this.getFilePath());
		}
		return false;
	}
	
	/**
	 * Representation of a bean's property-setter method.
	 *
	 */
	private class BeanMethod {

		private Object bean;
		private Method method;
		
		public BeanMethod(Object bean,Method method) {
			this.bean = bean;
			this.method = method;
		}
		
		/**
		 * Updates the property filed in the bean by calling the method stored.
		 * The newPropertyValue is passed as String argument to the method.
		 * 
		 * @param newPropertyValue
		 */
		public void updateProperty(String newPropertyValue) {
			try {
				this.method.invoke(bean, newPropertyValue);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				//TODO
			}
		}
		
	}
}
