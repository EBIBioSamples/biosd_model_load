/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.xrefs;

import uk.ac.ebi.fg.core_model.toplevel.Identifiable;
import uk.ac.ebi.fg.core_model.xref.Referrer;
import uk.ac.ebi.fg.core_model.xref.XRef;
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
public class ReferrerPersistenceListener<R extends Identifiable> extends Normalizer<R>
{
	private final XRefNormalizer xrefNormalizer;
	
	public ReferrerPersistenceListener ( Store store )
	{
		super ( store );
		xrefNormalizer = new XRefNormalizer ( store );
	}

	@Override
	public void normalize ( R referrer )
	{
		if ( referrer == null || referrer.getId () != null ) return;
		if ( ! ( referrer instanceof Referrer ) ) throw new RuntimeException (
			"Internal Error: " + referrer.getClass ().getName () + " can only be used with an instance of Annotatable"
		);
		
		for ( XRef xref: ((Referrer) referrer).getReferences () )
			xrefNormalizer.normalize ( xref );
	}

}
