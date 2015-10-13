package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.terms;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel.AnnotatableNormalizer;
import uk.ac.ebi.fg.core_model.terms.FreeTextTerm;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;

/**
 * Normalises {@link OntologyEntry}(es) linked to a {@link FreeTextTerm}. This in turn will 
 * normalise {@link ReferenceSource}s.
 *
 * <dl><dt>date</dt><dd>Mar 12, 2013</dd></dl>
 * @author Marco Brandizi
 *
 * @param <FT>
 */
public class FreeTextTermNormalizer<FT extends FreeTextTerm> extends AnnotatableNormalizer<FT>
{
	private final OntologyEntryNormalizer oeNormalizer;
	
	public FreeTextTermNormalizer ( Store store ) {
		super ( store );
		oeNormalizer = new OntologyEntryNormalizer ( store );
	}

	/** Check re-usability of OEs and, in turn, of Ref Sources */
	@Override
	public boolean normalize ( FT term )
	{
		if ( term == null || term.getId () != null ) return false;
		if ( !super.normalize ( term ) ) return false;

		Set<OntologyEntry> delOes = new HashSet<OntologyEntry> (), addOes = new HashSet<OntologyEntry> ();
		Set<OntologyEntry> oes = term.getOntologyTerms ();
		for ( OntologyEntry oe: oes )
		{
			ReferenceSource src = oe.getSource ();
			String srcAcc, srcVer, srcUrl;
			if ( src == null )
				srcAcc = srcVer = srcUrl = null;
			else {
				srcAcc = src.getAcc ();
				srcVer = src.getVersion ();
				srcUrl = src.getUrl ();
			}
			OntologyEntry oeS = store.find ( oe, oe.getAcc (), srcAcc, srcVer, srcUrl );
			
			if ( oeS == null ) {
				oeNormalizer.normalize ( oe );
				continue;
			}
			if ( oe == oeS ) continue; 
			
			delOes.add ( oe ); addOes.add ( oeS );
		}
		oes.removeAll ( delOes );
		oes.addAll ( addOes );
		return true;
	}

}
