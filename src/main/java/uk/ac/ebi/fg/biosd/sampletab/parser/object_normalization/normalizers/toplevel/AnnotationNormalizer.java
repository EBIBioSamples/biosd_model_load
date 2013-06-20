/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel;

import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Normalizer;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import uk.ac.ebi.fg.core_model.terms.AnnotationType;
import uk.ac.ebi.fg.core_model.toplevel.Annotation;

/**
 * Re-uses {@link AnnotationType}. This is used by {@link AnnotatableNormalizer}.
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AnnotationNormalizer extends Normalizer<Annotation>
{
	public AnnotationNormalizer ( Store store ) {
		super ( store );
	}

	@Override
	public boolean normalize ( Annotation ann )
	{
		if ( ann == null || ann.getId () != null ) return false;
		
		AnnotationType type = ann.getType ();
		if ( type == null || type.getId () != null ) return true; 
		
		AnnotationType typeS = store.find ( type, type.getName () );
		if ( typeS == null || type == typeS) return true;
		
		ann.setType ( typeS );
		return true;
	}
	
}
