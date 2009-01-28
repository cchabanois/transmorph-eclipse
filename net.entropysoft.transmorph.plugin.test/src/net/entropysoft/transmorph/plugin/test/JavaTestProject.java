/*******************************************************************************
 * Copyright (c) 2009 EntropySoft SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     EntropySoft - initial API and implementation
 *******************************************************************************/
package net.entropysoft.transmorph.plugin.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;

public class JavaTestProject {
	private IJavaProject javaProject;
	private IPackageFragmentRoot sourceFolder;
	private String projectName;

	public JavaTestProject(String projectName) {
		this.projectName = projectName;
	}

	public JavaTestProject(IJavaProject javaProject) {
		this.javaProject = javaProject;
		this.projectName = javaProject.getElementName();
	}

	/**
	 * create the java project
	 * 
	 * @throws CoreException
	 */
	public void create() throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		project.create(null);
		project.open(null);
		javaProject = JavaCore.create(project);

		IFolder binFolder = createBinFolder();

		setJavaNature();
		javaProject.setRawClasspath(new IClasspathEntry[0], null);

		createOutputFolder(binFolder);
		addSystemLibraries();
	}

	/**
	 * create the project from given template
	 * 
	 * @param templatePath
	 *            path to the content of the project
	 * @throws CoreException
	 * @throws IOException
	 */
	public void createFromTemplate(String templatePath) throws CoreException,
			IOException {
		create();
		getSourceFolder();
		IFolder folder = getProject().getFolder("src");
		TemplateManager.createFromTemplate(folder, templatePath);
	}

	public IProject getProject() {
		return javaProject.getProject();
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}

	public void addJar(IPath absolutePath) throws MalformedURLException,
			IOException, JavaModelException {
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaCore.newLibraryEntry(absolutePath,
				null, null);
		javaProject.setRawClasspath(newEntries, null);
	}

	public void removeJar(String jar) throws JavaModelException {
		List newEntries = new ArrayList();
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		for (int i = 0; i < oldEntries.length; i++) {
			IClasspathEntry entry = oldEntries[i];
			if (!entry.getPath().lastSegment().equals(jar)) {
				newEntries.add(entry);
			}
		}
		javaProject.setRawClasspath((IClasspathEntry[]) newEntries
				.toArray(new IClasspathEntry[0]), null);
	}

	public boolean hasJar(String jar) throws JavaModelException {
		IClasspathEntry[] entries = javaProject.getRawClasspath();
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			if (entry.getPath().lastSegment().equals(jar)) {
				return true;
			}
		}
		return false;
	}

	public IPackageFragment createPackage(String name) throws CoreException {
		return getSourceFolder().createPackageFragment(name, false, null);
	}

	public ICompilationUnit createCompilationUnit(String packageName,
			String className, String source) throws CoreException {
		IPackageFragmentRoot packageFragmentRoot = getSourceFolder();
		IPackageFragment packageFragment = packageFragmentRoot
				.createPackageFragment(packageName, false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package " + packageFragment.getElementName() + ";\n");
		buf.append("\n");
		buf.append(source);
		ICompilationUnit cu = packageFragment.createCompilationUnit(className
				+ ".java", buf.toString(), false, null);
		return cu;
	}

	/**
	 * add some code source before given string
	 * 
	 * @param compilationUnit
	 * @param beforeString
	 * @param sourceCode
	 * @throws JavaModelException
	 */
	public void addSourceCodeBefore(ICompilationUnit compilationUnit,
			String beforeString, String sourceCode) throws JavaModelException {
		ICompilationUnit workingCopy = compilationUnit.getWorkingCopy(null);
		try {
			IBuffer buffer = workingCopy.getBuffer();
			int index = buffer.getContents().indexOf(beforeString);
			buffer.replace(index, 0, sourceCode);
			workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
			workingCopy.commitWorkingCopy(false, null);
		} finally {
			workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * replace some code source
	 * 
	 * @param compilationUnit
	 * @param oldSourceCode
	 * @param newSourceCode
	 * @throws Exception
	 */
	public void replaceSourceCode(ICompilationUnit compilationUnit,
			String oldSourceCode, String newSourceCode) throws Exception {
		ICompilationUnit workingCopy = compilationUnit.getWorkingCopy(null);
		try {
			IBuffer buffer = workingCopy.getBuffer();
			int index = buffer.getContents().indexOf(oldSourceCode);
			if (index == -1) {
				throw new Exception("Cannot find '" + oldSourceCode + "'");
			}
			buffer.replace(index, oldSourceCode.length(), newSourceCode);
			workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
			workingCopy.commitWorkingCopy(false, null);
		} finally {
			workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * add some code source after given string
	 * 
	 * @param compilationUnit
	 * @param afterString
	 * @param sourceCode
	 * @throws JavaModelException
	 */
	public void addSourceCodeAfter(ICompilationUnit compilationUnit,
			String afterString, String sourceCode) throws JavaModelException {
		ICompilationUnit workingCopy = compilationUnit.getWorkingCopy(null);
		try {
			IBuffer buffer = workingCopy.getBuffer();
			int index = buffer.getContents().indexOf(afterString)
					+ afterString.length();
			buffer.replace(index, 0, sourceCode);
			workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
			workingCopy.commitWorkingCopy(false, null);
		} finally {
			workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * delete some source code from given compilation unit
	 * 
	 * @param compilationUnit
	 * @param begin
	 * @param end
	 * @throws JavaModelException
	 */
	public void deleteSourceCode(ICompilationUnit compilationUnit, String begin, String end) throws JavaModelException {
		ICompilationUnit workingCopy = compilationUnit.getWorkingCopy(null);
		try {
			IBuffer buffer = workingCopy.getBuffer();
			int index1 = buffer.getContents().indexOf(begin);
			int index2 = buffer.getContents().indexOf(end, index1+begin.length());
			buffer.replace(index1, index2-index1+1, "");
			workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
			workingCopy.commitWorkingCopy(false, null);
		} finally {
			workingCopy.discardWorkingCopy();
		}
	}
	
	public IType createJavaTypeFromTemplate(String templatePath,
			String className) throws CoreException, IOException {
		int pos = className.lastIndexOf('.');
		String packageName = className.substring(0, pos);
		String simpleName = className.substring(pos + 1);
		String javaSrcPath = className.replace('.', '/') + ".java";

		IPackageFragment packageFragment = getSourceFolder()
				.createPackageFragment(packageName, true, null);
		IContainer container = (IContainer) packageFragment.getResource();
		InputStream is = TemplateManager.getTemplateInputStream(templatePath,
				javaSrcPath);
		IFile file = createFile(container, simpleName + ".java", is);
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(file);
		return cu.getTypes()[0];
	}

	public IFile createFile(IContainer folder, String name, InputStream contents)
			throws JavaModelException {
		IFile file = folder.getFile(new Path(name));
		try {
			file.create(contents, IResource.FORCE, null);

		} catch (CoreException e) {
			throw new JavaModelException(e);
		}

		return file;
	}

	public void dispose() throws CoreException {
		getProject().delete(true, true, null);
	}

	private IFolder createBinFolder() throws CoreException {
		IFolder binFolder = getProject().getFolder("bin");
		binFolder.create(false, true, null);
		return binFolder;
	}

	public IFolder createLibFolder() throws CoreException {
		IFolder libFolder = getProject().getFolder("lib");
		libFolder.create(false, true, null);
		return libFolder;
	}

	private void setJavaNature() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
		getProject().setDescription(description, null);
	}

	private void createOutputFolder(IFolder binFolder)
			throws JavaModelException {
		IPath outputLocation = binFolder.getFullPath();
		javaProject.setOutputLocation(outputLocation, null);
	}

	/**
	 * get the source folder (create it if necessary)
	 * 
	 * @return
	 * @throws CoreException
	 */
	private IPackageFragmentRoot getSourceFolder() throws CoreException {
		if (sourceFolder != null) {
			return sourceFolder;
		}

		IClasspathEntry[] rawClassPath = javaProject.getRawClasspath();
		for (int i = 0; i < rawClassPath.length; i++) {
			if (rawClassPath[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				return javaProject.findPackageFragmentRoots(rawClassPath[i])[0];
			}
		}

		IFolder folder = getProject().getFolder("src");
		folder.create(false, true, null);
		sourceFolder = javaProject.getPackageFragmentRoot(folder);

		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaCore.newSourceEntry(sourceFolder
				.getPath());
		javaProject.setRawClasspath(newEntries, null);
		return sourceFolder;
	}

	private void addSystemLibraries() throws JavaModelException {
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaRuntime
				.getDefaultJREContainerEntry();
		javaProject.setRawClasspath(newEntries, null);
	}

	private Path findFileInPlugin(Plugin plugin, String file)
			throws MalformedURLException, IOException {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		URL base = null;
		base = Activator.getDefault().getBundle().getEntry("/");
		URL jarURL = new URL(base, file);
		URL localJarURL = Platform.asLocalURL(jarURL);
		return new Path(localJarURL.getPath());
	}

	/**
	 * get a method by name
	 * 
	 * @param type
	 * @param name
	 * @return
	 * @throws JavaModelException
	 */
	public IMethod getMethod(IType type, String name) throws JavaModelException {
		IMethod[] methods = type.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (name.equals(methods[i].getElementName())) {
				return methods[i];
			}
		}
		return null;
	}

}