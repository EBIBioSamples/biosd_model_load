/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel;

import uk.ac.ebi.fg.core_model.toplevel.Annotatable;
import uk.ac.ebi.fg.core_model.toplevel.Annotation;
import uk.ac.ebi.fg.core_model.toplevel.Identifiable;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Normalizer;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Store;

/**
 * 
 * TODO: Comment me!
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
	public void normalize ( A annotatable )
	{
		if ( annotatable == null || annotatable.getId () != null ) return;
		if ( ! ( annotatable instanceof Annotatable ) ) throw new RuntimeException (
			"Internal Error: " + annotatable.getClass ().getName () + " can only be used with an instance of Annotatable"
		);
		
		for ( Annotation ann: ( (Annotatable) annotatable ).getAnnotations () )
			annNormalizer.normalize ( ann );
	}
}
