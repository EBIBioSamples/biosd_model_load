/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel;

import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Normalizer;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import uk.ac.ebi.fg.core_model.terms.AnnotationType;
import uk.ac.ebi.fg.core_model.toplevel.Annotation;
import uk.ac.ebi.fg.core_model.toplevel.AnnotationProvenance;

/**
 * Re-uses {@link AnnotationType}. This is used by {@link AnnotatableNormalizer}.
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AnnotationNormalizer<AN extends Annotation> extends Normalizer<AN>
{
	public AnnotationNormalizer ( Store store ) {
		super ( store );
	}

	@Override
	public boolean normalize ( Annotation ann )
	{
		if ( ann == null || ann.getId () != null ) return false;
		normalizeType ( ann );
		normalizeProvenance ( ann );
		return true;
	}

	private boolean normalizeType ( Annotation ann )
	{
		AnnotationType type = ann.getType ();
		if ( type == null || type.getId () != null ) return true; 
		
		AnnotationType typeS = store.find ( type, type.getName () );
		if ( typeS == null || type == typeS) return true;

		ann.setType ( typeS );
		return true;
	}
	

	private boolean normalizeProvenance ( Annotation ann )
	{
		AnnotationProvenance prov = ann.getProvenance ();
		if ( prov == null || prov.getId () != null ) return true; 
		
		AnnotationProvenance provS = store.find ( prov, prov.getName () );
		if ( provS == null || prov == provS) return true;

		ann.setProvenance ( provS );
		return true;
	}
	
}
