/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.organizational;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.FilterNormalizer;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel.AnnotatableNormalizer;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.xrefs.ReferrerNormalizer;
import uk.ac.ebi.fg.core_model.organizational.Publication;
import uk.ac.ebi.fg.core_model.organizational.PublicationStatus;
import uk.ac.ebi.fg.core_model.toplevel.Annotation;
import uk.ac.ebi.fg.core_model.xref.XRef;

/**
 * Re-uses the publication status. It also takes care of re-usable objects linked from {@link Annotation annotations} 
 * and {@link XRef}s, by using the correspondent specific normalisers and the logic of {@link FilterNormalizer}.
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class PublicationNormalizer extends FilterNormalizer<Publication>
{
	/**
	 * Populates {@link FilterNormalizer} with {@link AnnotatableNormalizer} and {@link ReferrerNormalizer}.
	 * @param store
	 */
	public PublicationNormalizer ( Store store ) 
	{
		super ( store );

		normalizers.add ( new AnnotatableNormalizer<Publication> ( store ) );
		normalizers.add ( new ReferrerNormalizer<Publication> ( store ) );
	}

	/**
	 * As said above, check for publication status and uses {@link AnnotatableNormalizer}, {@link ReferrerNormalizer}, 
	 * which were put into {@link FilterNormalizer} by the constructor.
	 * 
	 */
	@Override
	public boolean normalize ( Publication pub ) 
	{
		if ( !super.normalize ( pub ) ) return false;
		
		PublicationStatus status = pub.getStatus ();
		if ( status == null ) return true;
		
		String statusName = StringUtils.trimToNull ( status.getName () );
		if ( statusName == null ) return true;
			
		PublicationStatus statusS = store.find ( status, statusName );
		if ( statusS == null || status == statusS ) return true;
		
		pub.setStatus ( statusS );
		return true;
	}
	
}
