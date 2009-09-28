package net.entropysoft.transmorph.plugin;

import java.lang.reflect.Type;

import org.eclipse.jdt.core.IType;

import net.entropysoft.transmorph.signature.FullTypeSignature;
import net.entropysoft.transmorph.signature.TypeFactory;

/**
 * Creates a {@link Type} from a JDT type signature ("QString;") if possible (ie
 * if corresponding class is present in the classpath)
 * 
 * @author cedric
 * 
 */
public class TypeFromJDTTypeSignatureFactory {

	private TypeFactory typeFactory;

	public TypeFromJDTTypeSignatureFactory(ClassLoader classLoader) {
		this.typeFactory = new TypeFactory(classLoader);
	}

	public ClassLoader getClassLoader() {
		return typeFactory.getClassLoader();
	}

	public Type getType(IType owningType, String typeSignature)
			throws ClassNotFoundException {
		JDTTypeSignatureParser jdtTypeSignatureParser = new JDTTypeSignatureParser();
		jdtTypeSignatureParser.setOwningType(owningType);
		jdtTypeSignatureParser.setTypeSignature(typeSignature);
		FullTypeSignature fullTypeSignature = jdtTypeSignatureParser
				.parseTypeSignature();
		return typeFactory.getType(fullTypeSignature);
	}

}
