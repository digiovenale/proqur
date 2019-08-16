package org.proqur.updater;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.proqur.annotations.UpdateProperty;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
public class UpdatePropertyPostProcessor extends WatcherService implements BeanPostProcessor {

	public UpdatePropertyPostProcessor() throws IOException {
		super();
		this.managedPropertiesFiles = new ArrayList<PropertiesFileDescriptor>();
	}
	/**
	 * List of managed and watched properties files, stored as PropertiesFileDescriptors.
	 */
	private List<PropertiesFileDescriptor> managedPropertiesFiles;
	
	/**
	 * Gets the methods annotated with @UpdateProperty and stores them.
	 * They will be called when an update of the associated properties file is detected.
	 */
	@Override
	@SuppressWarnings("unlikely-arg-type")
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

		if (bean==null) return bean;
		
		for (Method method : bean.getClass().getMethods()) {
			UpdateProperty updateProperty = method.getAnnotation(UpdateProperty.class);
			
			// If the method is annotated with @UpdateProperty, it is processed
			if (updateProperty != null) {
				try {
					Path filePath = Paths.get(updateProperty.file()).normalize();
					String path = filePath.toAbsolutePath().toString();
					PropertiesFileDescriptor retrivedPropertiesFile = null;
					for(PropertiesFileDescriptor propertiesFile: managedPropertiesFiles) {
						if(propertiesFile!= null && propertiesFile.equals(path)) {
							retrivedPropertiesFile = propertiesFile;
							break;
						}
					}
					
					// If the properties file is already managed, the method is simply added in the managedPropertiesFiles
					if (retrivedPropertiesFile!= null) {
						retrivedPropertiesFile.addMethod(updateProperty.property(),bean, method);
					}
					// If the properties file is not already managed, a new PropertiesFileDescriptor is created
					else {
						PropertiesFileDescriptor fileDescriptor = new PropertiesFileDescriptor(filePath.toAbsolutePath().toString());
						fileDescriptor.addMethod(updateProperty.property(),bean, method);
						managedPropertiesFiles.add(fileDescriptor);
						
						// The directory of the file will be added to the watched directories.
						manageDirectory(filePath);	
					}
					
					// Set the property-filed of the bean to the current value
					setProperty(method, bean, filePath.toAbsolutePath().toString(), updateProperty.property());
				} catch (IllegalArgumentException e) {
					//TODO
				}
			}
		}
		return bean;
	}
	
	/**
	 * Finds the PropertiesFileDescriptor matching the given path (if any).
	 * Than the PropertiesFileDescriptor updates every setter-method associated with
	 * the properties in the array listPropertyChanged.
	 */
	@Override
	@SuppressWarnings("unlikely-arg-type")
	protected void updateObjects(String path, PropertyLocator[] listPropertyChanged) {
		
		for(PropertiesFileDescriptor propertiesFile: managedPropertiesFiles) {
			if(propertiesFile!= null && propertiesFile.equals(path)) {
				propertiesFile.updateProperties(listPropertyChanged);
				break;
			}
		}
	}
	
	/**
	 * Scheduled check for changes in registered files.
	 */
	@Scheduled(initialDelayString = "${proqur.initial.delay:1000}", fixedDelayString = "${proqur.fixed.delay:10000}")
	public void runScheduledActovity() {
		try {
			poolAndRetriveEvent();
		} catch (InterruptedException e) {
			//TODO
		}
	}

}
