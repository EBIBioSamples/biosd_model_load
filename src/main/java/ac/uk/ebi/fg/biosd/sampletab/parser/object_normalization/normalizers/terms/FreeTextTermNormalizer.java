/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.terms;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.fg.core_model.terms.FreeTextTerm;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.ObjectNormalizer;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Store;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Mar 12, 2013</dd></dl>
 * @author Marco Brandizi
 *
 * @param <FT>
 */
public class FreeTextTermNormalizer<FT extends FreeTextTerm> extends ObjectNormalizer<FT>
{
	private final OntologyEntryNormalizer oeNormalizer;
	
	public FreeTextTermNormalizer ( Store store ) {
		super ( store );
		oeNormalizer = new OntologyEntryNormalizer ( store );
	}

	/** Check re-usability of OEs and, in turn, of Ref Sources */
	@Override
	public void normalize ( FT term )
	{
		if ( term == null || term.getId () != null ) return;
		
		Set<OntologyEntry> delOes = new HashSet<OntologyEntry> (), addOes = new HashSet<OntologyEntry> ();
		Set<OntologyEntry> oes = term.getOntologyTerms ();
		for ( OntologyEntry oe: oes )
		{
			ReferenceSource src = oe.getSource ();
			if ( src == null ) continue; // This is actually an error and will pop-up later.
			OntologyEntry oeS = store.find ( oe, oe.getAcc (), src.getAcc (), src.getVersion () );
			
			if ( oeS == null ) {
				oeNormalizer.normalize ( oe );
				continue;
			}
			delOes.add ( oe ); addOes.add ( oeS );
		}
		oes.removeAll ( delOes ); oes.addAll ( addOes );
	}

}
