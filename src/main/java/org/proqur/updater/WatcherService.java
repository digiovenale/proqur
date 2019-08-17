package org.proqur.updater;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;

abstract class WatcherService {

	private WatchService watcher;
	/**
	 * Maps a WatchKey to the directory path for every watched directory.
	 */
	private Map<WatchKey, String> keyDirectoryMapper;

	public WatcherService() throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keyDirectoryMapper = new HashMap<WatchKey, String>();
	}
	
	/**
	 * Adds the directory of the file to the watched directory.
	 * @param filePath
	 */
	protected void manageDirectory(Path filePath) {
		// Retrives the absolute path of the directory
		String dirPath = filePath.toAbsolutePath().toString().substring(0,
				filePath.toAbsolutePath().toString().lastIndexOf(filePath.getFileName().toString())
						- 1);
		
		if (keyDirectoryMapper.containsValue(dirPath)) return;
		
		Path dir = Paths.get(dirPath);
		try {
			WatchKey key = dir.register(this.watcher, ENTRY_MODIFY);
			keyDirectoryMapper.put(key, dirPath);
		} catch (IOException x) {
			System.err.println(x);
		}
	}
	
	/**
	 * Works every event in queue of the watcher.
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	protected void poolAndRetriveEvent() throws InterruptedException {

		WatchKey key = watcher.take();

		for (WatchEvent<?> event : key.pollEvents()) {
			WatchEvent.Kind<?> kind = event.kind();

			if (kind == OVERFLOW) continue;
			
			WatchEvent<Path> ev = (WatchEvent<Path>) event;
			String filename = String.format("%s%s%s", keyDirectoryMapper.get(key), File.separator,
					ev.context().getFileName());
			
			updatePropertyForManagedBeans(filename);
			
			boolean valid = key.reset();
			if (!valid) break;
		}
	}

	/**
	 * Executes the update of each bean associated with the given filePath
	 * 
	 * @param filePath
	 */
	private void updatePropertyForManagedBeans(String filePath) {
		try {
			InputStream input = new FileInputStream(filePath);
			Properties prop = new Properties();
			prop.load(input);
			
			PropertyLocator[] propertyChanged = new PropertyLocator[prop.size()];
			{
				int i = 0;
				for (Object key : prop.keySet()) {
					propertyChanged[i++] = new PropertyLocator(key.toString(), prop.get(key).toString());
				}
			}
			
			updateObjects(filePath, propertyChanged);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Calls the object's method passing as argument the actual property
	 * contained in the file. The file is described by its filePath.
	 * 
	 * @param method
	 * @param obj
	 * @param filePath
	 * @param property
	 */
	protected void setProperty(Method method, Object obj, String filePath, String property) {
		try (InputStream input = new FileInputStream(filePath)) {

			Properties prop = new Properties();
			prop.load(input);

			prop.forEach((key, value) -> {
				if (property.equals(key)) {
					try {
						method.invoke(obj,value);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			});

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Updates every setter-method associated with the properties in the array listPropertyChanged.
	 * The properties must be present in the file denoted by the filePath.
	 * 
	 * @param filePath
	 * @param listPropertyChanged
	 */
	protected abstract void updateObjects(String filePath, PropertyLocator[] listPropertyChanged);
	
}
