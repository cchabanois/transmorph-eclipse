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
package net.entropysoft.transmorph.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.entropysoft.transmorph.DefaultConverters;
import net.entropysoft.transmorph.JDTTransmorph;
import net.entropysoft.transmorph.plugin.test.JavaTestProject;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

public class JDTTransmorphTest extends TestCase {

	public void testJDTTransmorph() throws Exception {

		JavaTestProject javaTestProject = new JavaTestProject("testConverter");
		javaTestProject.createFromTemplate("templates/simpleConvertTest");
		IType type = javaTestProject.getJavaProject().findType(
				"net.entropysoft.foo.MyClass");
		IMethod method = javaTestProject.getMethod(type, "setListOfInts");
		String parameterType = method.getParameterTypes()[0]; // QList<QInteger;>;

		JDTTransmorph eclipseConverter = new JDTTransmorph(JDTTransmorphTest.class
				.getClassLoader(), new DefaultConverters());
		List<Integer> listOfInts = (List<Integer>) eclipseConverter.convert(
				new String[] { "1", "2", "3" }, type, parameterType);
		assertNotNull(listOfInts);
		assertEquals(3, listOfInts.size());
		assertEquals(1, listOfInts.get(0).intValue());
		assertEquals(2, listOfInts.get(1).intValue());
		assertEquals(3, listOfInts.get(2).intValue());

		method = javaTestProject.getMethod(type, "setMyInnerClass");
		parameterType = method.getParameterTypes()[0]; // QMyInnerClass;
		Map map = new HashMap<String, Object>();
		map.put("myString", "myString value");
		Object obj = eclipseConverter.convert(map, type, parameterType);
	}

}
