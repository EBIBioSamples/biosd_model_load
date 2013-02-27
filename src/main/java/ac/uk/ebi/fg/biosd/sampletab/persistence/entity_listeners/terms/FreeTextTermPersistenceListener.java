/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.OntologyEntryDAO;
import uk.ac.ebi.fg.core_model.terms.FreeTextTerm;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.PersistenceListener;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Feb 13, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class FreeTextTermPersistenceListener<FT extends FreeTextTerm> extends PersistenceListener<FT>
{
	private final OntologyEntryDAO<OntologyEntry> oedao;
	private final OntologyEntryPersistenceListener oeListener;
	
	public FreeTextTermPersistenceListener ( EntityManager entityManager ) {
		super ( entityManager );
		oedao = new OntologyEntryDAO<OntologyEntry> ( OntologyEntry.class, entityManager );
		oeListener = new OntologyEntryPersistenceListener ( entityManager );
	}

	/** Check re-usability of OEs and, in turn, of Ref Sources */
	@Override
	public void prePersist ( FT term )
	{
		if ( term == null || term.getId () != null ) return;
		
		Set<OntologyEntry> delOes = new HashSet<OntologyEntry> (), addOes = new HashSet<OntologyEntry> ();
		Set<OntologyEntry> oes = term.getOntologyTerms ();
		for ( OntologyEntry oe: oes )
		{
			ReferenceSource src = oe.getSource ();
			if ( src == null ) continue; // This is actually an error and will pop-up later.
			OntologyEntry oeDB = oedao.find ( oe.getAcc (), src.getAcc (), src.getVersion () );
			
			if ( oeDB == null ) {
				oeListener.prePersist ( oe );
				continue;
			}
			delOes.add ( oe ); addOes.add ( oeDB );
		}
		oes.removeAll ( delOes ); oes.addAll ( addOes );
	}

}
