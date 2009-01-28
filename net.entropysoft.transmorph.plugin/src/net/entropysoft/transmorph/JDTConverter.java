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
package net.entropysoft.transmorph;

import net.entropysoft.transmorph.plugin.utils.TypeUtil;
import net.entropysoft.transmorph.type.TypeFactory;

import org.eclipse.jdt.core.IType;

/**
 * JDTConverter adds a method to Converter that can convert an object to an
 * unresolved eclipse signature in the context of an owningType
 * 
 * <p>
 * By default, JDTConverter does not use internal form fully qualified names
 * but the dotted form qualified names
 * </p>
 * 
 * @author Cedric Chabanois (cchabanois at gmail.com)
 * 
 */
public class JDTConverter extends Converter {

	public JDTConverter(ClassLoader classLoader, IConverter converters[]) {
		super(classLoader, converters);
		setUseInternalFormFullyQualifiedName(false);
	}

	public JDTConverter(IConverter converters[]) {
		super(converters);
		setUseInternalFormFullyQualifiedName(false);
	}

	public JDTConverter(TypeFactory typeFactory, IConverter converters[]) {
		super(typeFactory, converters);
		setUseInternalFormFullyQualifiedName(false);
	}

	public Object convert(Object source, IType owningType,
			String parameterizedTypeSignature) throws ConverterException {
		parameterizedTypeSignature = TypeUtil.resolveTypeSignature(owningType,
				parameterizedTypeSignature, false);
		return convert(source, parameterizedTypeSignature);
	}
}
