package net.entropysoft.transmorph.plugin;

import net.entropysoft.transmorph.plugin.utils.TypeUtil;
import net.entropysoft.transmorph.signature.FullTypeSignature;
import net.entropysoft.transmorph.signature.parser.ClassFileTypeSignatureParser;
import net.entropysoft.transmorph.signature.parser.ITypeSignatureParser;
import net.entropysoft.transmorph.signature.parser.InvalidSignatureException;

import org.eclipse.jdt.core.IType;

/**
 * Parse JDT type signatures ("QString;") 
 * 
 * @author cedric
 *
 */
public class JDTTypeSignatureParser implements ITypeSignatureParser {
	private IType owningType;
	private String unresolvedSignature;
	
	@Override
	public FullTypeSignature parseTypeSignature()
			throws InvalidSignatureException {
		ClassFileTypeSignatureParser typeSignatureParser = new ClassFileTypeSignatureParser();
		typeSignatureParser.setUseInternalFormFullyQualifiedName(false);
		if (owningType == null) {
			throw new InvalidSignatureException("Owning type not set", 0);
		}
		String resolvedSignature = TypeUtil.resolveTypeSignature(owningType,
				unresolvedSignature, false);
		typeSignatureParser.setTypeSignature(resolvedSignature);
		return typeSignatureParser.parseTypeSignature();
	}

	public void setOwningType(IType owningType) {
		this.owningType = owningType;
	}
	
	@Override
	public void setTypeSignature(String signature) {
		this.unresolvedSignature = signature;
	}

}
