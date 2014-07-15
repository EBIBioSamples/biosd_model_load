package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.terms;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Normalizer;
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
			if ( src == null ) continue; // This is actually an error and will pop-up later.
			OntologyEntry oeS = store.find ( oe, oe.getAcc (), src.getAcc (), src.getVersion (), src.getUrl () );
			
			if ( oeS == null ) {
				oeNormalizer.normalize ( oe );
				continue;
			}
			if ( oe == oeS ) continue; 
			
			delOes.add ( oe ); addOes.add ( oeS );
		}
		return oes.removeAll ( delOes ) | oes.addAll ( addOes );
	}

}
