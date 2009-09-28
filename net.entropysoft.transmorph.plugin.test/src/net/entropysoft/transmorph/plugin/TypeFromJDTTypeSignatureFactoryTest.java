package net.entropysoft.transmorph.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.entropysoft.transmorph.DefaultConverters;
import net.entropysoft.transmorph.Transmorph;
import net.entropysoft.transmorph.plugin.test.JavaTestProject;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.junit.Test;

public class TypeFromJDTTypeSignatureFactoryTest {

	@Test
	public void testTypeFromJDTTypeSignatureFactory() throws Exception {
		JavaTestProject javaTestProject = new JavaTestProject("testConverter");
		javaTestProject.createFromTemplate("templates/simpleConvertTest");
		IType owningType = javaTestProject.getJavaProject().findType(
				"net.entropysoft.foo.MyClass");
		IMethod method = javaTestProject.getMethod(owningType, "setListOfInts");
		String parameterType = method.getParameterTypes()[0]; // QList<QInteger;>;

		TypeFromJDTTypeSignatureFactory typeFactory = new TypeFromJDTTypeSignatureFactory(this.getClass().getClassLoader());
		Transmorph transmorph = new Transmorph(new DefaultConverters());
		Type javaType = typeFactory.getType(owningType, parameterType);
		
		List<Integer> listOfInts = (List<Integer>) transmorph.convert(
				new String[] { "1", "2", "3" }, javaType);
		assertNotNull(listOfInts);
		assertEquals(3, listOfInts.size());
		assertEquals(1, listOfInts.get(0).intValue());
		assertEquals(2, listOfInts.get(1).intValue());
		assertEquals(3, listOfInts.get(2).intValue());

		method = javaTestProject.getMethod(owningType, "setMyInnerClass");
		parameterType = method.getParameterTypes()[0]; // QMyInnerClass;
		javaType = typeFactory.getType(owningType, parameterType);
		Map map = new HashMap<String, Object>();
		map.put("myString", "myString value");
		Object obj = transmorph.convert(map, javaType);
	}	
	
	
}
