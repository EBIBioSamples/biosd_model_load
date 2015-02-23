/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Normalizer;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import uk.ac.ebi.fg.core_model.toplevel.Annotatable;
import uk.ac.ebi.fg.core_model.toplevel.Annotation;

/**
 * Forwards all the annotations in an {@link Annotatable} to an {@link AnnotationNormalizer}.
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AnnotatableNormalizer<A extends Annotatable> extends Normalizer<A>
{
	private final AnnotationNormalizer<Annotation> annNormalizer;
	
	public AnnotatableNormalizer ( Store store )
	{
		super ( store );
		annNormalizer = new AnnotationNormalizer<Annotation> ( store );
	}

	/**
	 * Try to reuse existing annotations.
	 */
	@Override
	public boolean normalize ( A annotatable )
	{
		if ( annotatable == null || annotatable.getId () != null ) return false;
		
		Set<Annotation> annotations = ( (Annotatable) annotatable ).getAnnotations (), 
										delAnns = new HashSet<Annotation> (), addAnns = new HashSet<Annotation> ();
		
		for ( Annotation ann: annotations )
		{
			if ( ann == null ) {
				delAnns.add ( ann );
				continue; 
			}
			
			Annotation annS = store.find ( ann );
			if ( annS == null )
				// It's new, just work out its links
				annNormalizer.normalize ( ann );
			else if ( ann != annS ) 
			{
				// There is indeed another equivalent object, let's normalise the situation 
				delAnns.add ( ann );
				addAnns.add ( annS );
			}
		}
		
		annotations.removeAll ( delAnns );
		annotations.addAll ( addAnns );

		return true; 
	}
}
