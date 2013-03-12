/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel;

import uk.ac.ebi.fg.core_model.terms.AnnotationType;
import uk.ac.ebi.fg.core_model.toplevel.Annotation;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.ObjectNormalizer;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Store;

/**
 * 
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AnnotationNormalizer extends ObjectNormalizer<Annotation>
{
	public AnnotationNormalizer ( Store store ) {
		super ( store );
	}

	@Override
	public void normalize ( Annotation ann )
	{
		if ( ann == null || ann.getId () != null ) return;
		AnnotationType type = ann.getType ();
		if ( type == null || type.getId () != null ) return; 
		
		AnnotationType typeS = store.find ( type, type.getName () );
		if ( typeS == null ) return;
		
		ann.setType ( typeS );
	}
	
}
