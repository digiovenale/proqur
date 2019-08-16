package org.proqur.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * <p>Annotation to be put on one-argument setters methods.
 * The method annotated will be called when a modification in the
 * properties file is detected.
 * The associated field will be consequentially updated as the property
 * is changed in the file.</p>
 * 
 * <p>Two values are mandatory:
 * <ol>
 * <li><b>file</b>: the path to the properties file;</li>
 * <li><b>property</b>:the property in the properties file to watch.</li>
 * </ol></p>
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface UpdateProperty {
	
	String file();
	String property();

}
