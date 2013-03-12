/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.organizational;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.core_model.organizational.Publication;
import uk.ac.ebi.fg.core_model.organizational.PublicationStatus;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.FilterNormalizer;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel.AnnotatableNormalizer;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.xrefs.ReferrerPersistenceListener;

/**
 * 
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class PublicationNormalizer extends FilterNormalizer<Publication>
{
	public PublicationNormalizer ( Store store ) 
	{
		super ( store );

		normalizers.add ( new AnnotatableNormalizer<Publication> ( store ) );
		normalizers.add ( new ReferrerPersistenceListener<Publication> ( store ) );
	}

	@Override
	public void normalize ( Publication pub ) 
	{
		if ( pub == null || pub.getId () != null ) return;
		super.normalize ( pub );
		
		PublicationStatus status = pub.getStatus ();
		if ( status == null ) return;
		
		String statusName = StringUtils.trimToNull ( status.getName () );
		if ( statusName == null ) return;
			
		PublicationStatus statusS = store.find ( status, statusName );
		if ( statusS == null ) return;
		
		pub.setStatus ( statusS );
	}
	
}
