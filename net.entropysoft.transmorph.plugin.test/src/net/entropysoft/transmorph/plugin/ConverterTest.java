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
import net.entropysoft.transmorph.JDTConverter;
import net.entropysoft.transmorph.IConverter;
import net.entropysoft.transmorph.converters.ArrayToArray;
import net.entropysoft.transmorph.converters.ArrayToCollection;
import net.entropysoft.transmorph.converters.CharacterArrayToString;
import net.entropysoft.transmorph.converters.CollectionToCollection;
import net.entropysoft.transmorph.converters.DateToCalendar;
import net.entropysoft.transmorph.converters.IdentityConverter;
import net.entropysoft.transmorph.converters.MapToMap;
import net.entropysoft.transmorph.converters.NumberToNumber;
import net.entropysoft.transmorph.converters.ObjectToString;
import net.entropysoft.transmorph.converters.StringToBoolean;
import net.entropysoft.transmorph.converters.StringToCharacterArray;
import net.entropysoft.transmorph.converters.StringToClass;
import net.entropysoft.transmorph.converters.StringToEnum;
import net.entropysoft.transmorph.converters.StringToFile;
import net.entropysoft.transmorph.converters.StringToNumber;
import net.entropysoft.transmorph.converters.StringToURL;
import net.entropysoft.transmorph.converters.beans.MapToBean;
import net.entropysoft.transmorph.plugin.test.JavaTestProject;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

public class ConverterTest extends TestCase {

	public void testConverter() throws Exception {
		IConverter[] converters = new IConverter[] { new NumberToNumber(),
				new StringToNumber(), new StringToBoolean(),
				new StringToEnum(), new StringToClass(),
				new ArrayToArray(), new MapToMap(),
				new ArrayToCollection(),
				new CollectionToCollection(), new StringToFile(),
				new StringToURL(), new CharacterArrayToString(),
				new StringToCharacterArray(), new ObjectToString(),
				new DateToCalendar(), new MapToBean(),
				new IdentityConverter() };

		JavaTestProject javaTestProject = new JavaTestProject("testConverter");
		javaTestProject.createFromTemplate("templates/simpleConvertTest");
		IType type = javaTestProject.getJavaProject().findType(
				"net.entropysoft.foo.MyClass");
		IMethod method = javaTestProject.getMethod(type, "setListOfInts");
		String parameterType = method.getParameterTypes()[0]; // QList<QInteger;>;

		JDTConverter eclipseConverter = new JDTConverter(ConverterTest.class.getClassLoader(), converters);
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
