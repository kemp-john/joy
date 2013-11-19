/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kvc.joy.commons.scanner.classpath;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kvc.joy.commons.exception.SystemException;
import com.kvc.joy.commons.io.FileTool;
import com.kvc.joy.commons.lang.ClassTool;
import com.kvc.joy.commons.scanner.support.Resource;

/**
 * ClassPath scanner.
 */
public class ClassPathScanner {

	private static final Logger LOG = LoggerFactory.getLogger(ClassPathScanner.class);

	/**
	 * Scans the classpath for resources under the specified location, starting
	 * with the specified prefix and ending with the specified suffix.
	 * 
	 * @param path The path in the classpath to start searching. Subdirectories
	 *            are also searched.
	 * @param prefix The prefix of the resource names to match.
	 * @param suffix The suffix of the resource names to match.
	 * @return The resources that were found.
	 * @throws IOException when the location could not be scanned.
	 */
	public Resource[] scanForResources(String path, String prefix, String suffix) throws IOException {
		LOG.debug("Scanning for classpath resources at '" + path + "' (Prefix: '" + prefix + "', Suffix: '" + suffix
				+ "')");

		Set<Resource> resources = new TreeSet<Resource>();

		Set<String> resourceNames = findResourceNames(path, prefix, suffix);
		for (String resourceName : resourceNames) {
			resources.add(new ClassPathResource(resourceName));
			LOG.debug("Found resource: " + resourceName);
		}

		return resources.toArray(new Resource[resources.size()]);
	}

	/**
	 * Scans the classpath for concrete classes under the specified package
	 * implementing this interface. Non-instantiable abstract classes are
	 * filtered out.
	 * 
	 * @param location The location (package) in the classpath to start
	 *            scanning. Subpackages are also scanned.
	 * @param implementedInterface The interface the matching classes should
	 *            implement.
	 * @return The non-abstract classes that were found.
	 * @throws Exception when the location could not be scanned.
	 */
	public Class<?>[] scanForClasses(String location, Class<?> implementedInterface) throws Exception {
		LOG.debug("Scanning for classes at '" + location + "' (Implementing: '" + implementedInterface.getName() + "')");

		List<Class<?>> classes = new ArrayList<Class<?>>();

		Set<String> resourceNames = findResourceNames(location, "", ".class");
		for (String resourceName : resourceNames) {
			String className = toClassName(resourceName);
			Class<?> clazz = getClassLoader().loadClass(className);

			if (Modifier.isAbstract(clazz.getModifiers())) {
				LOG.debug("Skipping abstract class: " + className);
				continue;
			}

			if (!implementedInterface.isAssignableFrom(clazz)) {
				continue;
			}

			ClassTool.instantiate(className);
			classes.add(clazz);
			LOG.debug("Found class: " + className);
		}

		return classes.toArray(new Class<?>[classes.size()]);
	}

	/**
	 * Converts this resource name to a fully qualified class name.
	 * 
	 * @param resourceName The resource name.
	 * @return The class name.
	 */
	private String toClassName(String resourceName) {
		String nameWithDots = resourceName.replace("/", ".");
		return nameWithDots.substring(0, (nameWithDots.length() - ".class".length()));
	}

	/**
	 * Finds the resources names present at this location and below on the
	 * classpath starting with this prefix and ending with this suffix.
	 * 
	 * @param path The path on the classpath to scan.
	 * @param prefix The filename prefix to match.
	 * @param suffix The filename suffix to match.
	 * @return The resource names.
	 * @throws IOException when scanning this location failed.
	 */
	private Set<String> findResourceNames(String path, String prefix, String suffix) throws IOException {
		Set<String> resourceNames = new TreeSet<String>();

		List<URL> locationsUrls = getLocationUrlsForPath(path);
		for (URL locationUrl : locationsUrls) {
			LOG.debug("Scanning URL: " + locationUrl.toExternalForm());

			String protocol = locationUrl.getProtocol();
			ClassPathLocationScanner classPathLocationScanner = createLocationScanner(protocol);
			if (classPathLocationScanner == null) {
				String scanRoot = FileTool.toFile(locationUrl).getPath();
				LOG.warn("Unable to scan location: " + scanRoot + " (unsupported protocol: " + protocol + ")");
			} else {
				resourceNames.addAll(classPathLocationScanner.findResourceNames(path, locationUrl));
			}
		}

		return filterResourceNames(resourceNames, prefix, suffix);
	}

	/**
	 * Gets the physical location urls for this logical path on the classpath.
	 * 
	 * @param path The path on the classpath.
	 * @return The underlying physical URLs.
	 * @throws IOException when the lookup fails.
	 */
	private List<URL> getLocationUrlsForPath(String path) throws IOException {
		List<URL> locationUrls = new ArrayList<URL>();
		Enumeration<URL> urls = getClassLoader().getResources(path);
		if (!urls.hasMoreElements()) {
			throw new SystemException("Unable to determine URL for classpath location: " + path + " (ClassLoader: "
					+ getClassLoader() + ")");
		}

		while (urls.hasMoreElements()) {
			locationUrls.add(urls.nextElement());
		}

		return locationUrls;
	}

	/**
	 * Creates an appropriate location scanner for this url protocol.
	 * 
	 * @param protocol The protocol of the location url to scan.
	 * @return The location scanner or {@code null} if it could not be created.
	 */
	private ClassPathLocationScanner createLocationScanner(String protocol) {
		if ("file".equals(protocol)) {
			return new FileSystemClassPathLocationScanner();
		}

		if ("jar".equals(protocol) || "zip".equals(protocol) // WebLogic
				|| "wsjar".equals(protocol) // WebSphere
		) {
			return new JarFileClassPathLocationScanner();
		}

		// if (FeatureDetector.isJBossVFSv3Available() &&
		// "vfs".equals(protocol)) {
		// return new JBossVFSv3ClassPathLocationScanner();
		// }
		//
		// if (FeatureDetector.isOsgiFrameworkAvailable() && (
		// "bundle".equals(protocol) // Felix
		// || "bundleresource".equals(protocol)) //Equinox
		// ) {
		// return new OsgiClassPathLocationScanner();
		// }

		return null;
	}

	/**
	 * @return The classloader to use to scan the classpath.
	 */
	private ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	/**
	 * Filters this list of resource names to only include the ones whose
	 * filename matches this prefix and this suffix.
	 * 
	 * @param resourceNames The names to filter.
	 * @param prefix The prefix to match.
	 * @param suffix The suffix to match.
	 * @return The filtered names set.
	 */
	private Set<String> filterResourceNames(Set<String> resourceNames, String prefix, String suffix) {
		Set<String> filteredResourceNames = new TreeSet<String>();
		for (String resourceName : resourceNames) {
			String fileName = resourceName.substring(resourceName.lastIndexOf("/") + 1);
			if (fileName.startsWith(prefix) && fileName.endsWith(suffix)
					&& (fileName.length() > (prefix + suffix).length())) {
				filteredResourceNames.add(resourceName);
			} else {
				LOG.debug("Filtering out resource: " + resourceName + " (filename: " + fileName + ")");
			}
		}
		return filteredResourceNames;
	}
}
