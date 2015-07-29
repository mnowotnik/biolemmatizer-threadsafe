/**
 * 
 */
package edu.ucdenver.ccp.nlp.biolemmatizer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;


/**
 * @author Center for Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 *
 */
public class ClasspathUtil {

	/**
	 * Helper method for grabbing a file from the classpath and returning an InputStream
	 * 
	 * @param clazz
	 * @param resourceName
	 * @return
	 */
	public static InputStream getResourceStreamFromClasspath(Class<?> clazz, String resourceName) {
		InputStream is = clazz.getResourceAsStream(resourceName);
		if (is == null) {
			is = clazz.getClassLoader().getResourceAsStream(resourceName);
			if (is == null)
				throw new RuntimeException("resource not found in classpath: " + resourceName + " class is: "
						+ clazz.getCanonicalName());
		}
		return is;
	}

	/**
	 * Extracts the contents of a resource on the classpath and returns them as a String
	 * 
	 * @param clazz
	 * @param resourceName
	 * @return
	 * @throws IOException
	 */
	public static String getContentsFromClasspathResource(Class<?> clazz, String resourceName,
			String encoding) throws IOException {
		return convertStream(getResourceStreamFromClasspath(clazz, resourceName), encoding);
	}

	/**
	 * Converts the input InputStream to a String
	 * 
	 * @param is
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String convertStream(InputStream is, String encoding) throws IOException {
		StringWriter sw = new StringWriter();
		try {
			IOUtils.copy(is, sw, encoding);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(sw);
		}
		return sw.toString();
	}
	
	/**
	 * Copies the contents of the InputStream to the specified File
	 * 
	 * @param is
	 * @param file
	 * @throws IOException
	 */
	public static void copy(InputStream is, File file) throws IOException {
		BufferedOutputStream outStream=null;
		try {
			outStream = new BufferedOutputStream(new FileOutputStream(file));
			copy(is, outStream);
		}
		finally {
			IOUtils.closeQuietly(outStream);
		}
	}
	

	/**
	 * Copies the specified InputStream to the specified OutputStream
	 * 
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	public static void copy(InputStream is, OutputStream os) throws IOException {
		try {
			IOUtils.copyLarge(is, os);
		} finally {
			IOUtils.closeQuietly(os);
		}
	}
	
	/**
	 * Copies a resource (file on the classpath) to the specified file
	 * 
	 * @param resourceName
	 * @param directory
	 * @return
	 * @throws IOException
	 */
	public static File copyClasspathResourceToFile(Class<?> clazz, String resourceName, File file) throws IOException {
		copy(getResourceStreamFromClasspath(clazz, resourceName), file);
		return file;
	}

	/**
	 * Copies a resource (file on the classpath) to the specified directory
	 * 
	 * @param clazz
	 * @param resourceName
	 * @param directory
	 * @throws IOException
	 */
	public static File copyClasspathResourceToDirectory(Class<?> clazz, String resourceName, File directory)
			throws IOException {
		return copyClasspathResourceToFile(clazz, resourceName, new File(directory, resourceName));
	}
	
}
