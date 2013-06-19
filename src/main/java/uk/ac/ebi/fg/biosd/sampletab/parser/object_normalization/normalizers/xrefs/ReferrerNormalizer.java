/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.xrefs;

import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Normalizer;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import uk.ac.ebi.fg.core_model.toplevel.Identifiable;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;
import uk.ac.ebi.fg.core_model.xref.Referrer;
import uk.ac.ebi.fg.core_model.xref.XRef;

/**
 * Forward the {@link XRef}s of a {@link Referrer} to a {@link XRefNormalizer}, which will allow for re-usage of 
 * {@link ReferenceSource} in cross-references.
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 * @param <R> note that this should actually be {@link Referrer}, but it cannot be due to the upper restriction. The
 * correct type is checked at run-time.
 * 
 */
public class ReferrerNormalizer<R extends Identifiable> extends Normalizer<R>
{
	private final XRefNormalizer xrefNormalizer;
	
	public ReferrerNormalizer ( Store store )
	{
		super ( store );
		xrefNormalizer = new XRefNormalizer ( store );
	}

	/**
	 * I checks that referrer is an instance of {@link Referrer} (see above).
	 */
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
