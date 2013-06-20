/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel;

import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Normalizer;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import uk.ac.ebi.fg.core_model.toplevel.Annotatable;
import uk.ac.ebi.fg.core_model.toplevel.Annotation;
import uk.ac.ebi.fg.core_model.toplevel.Identifiable;

/**
 * Forwards all the annotations in an {@link Annotatable} to an {@link AnnotationNormalizer}.
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AnnotatableNormalizer<A extends Identifiable> extends Normalizer<A>
{
	private final AnnotationNormalizer annNormalizer;
	
	public AnnotatableNormalizer ( Store store )
	{
		super ( store );
		annNormalizer = new AnnotationNormalizer ( store );
	}

	@Override
	public boolean normalize ( A annotatable )
	{
		if ( annotatable == null || annotatable.getId () != null ) return false;
		if ( ! ( annotatable instanceof Annotatable ) ) throw new RuntimeException (
			"Internal Error: " + annotatable.getClass ().getName () + " can only be used with an instance of Annotatable"
		);
		
		for ( Annotation ann: ( (Annotatable) annotatable ).getAnnotations () )
			annNormalizer.normalize ( ann );
		
		return true;
	}
}
