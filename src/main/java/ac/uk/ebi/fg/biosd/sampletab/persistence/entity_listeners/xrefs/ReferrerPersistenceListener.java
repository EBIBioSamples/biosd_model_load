/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.xrefs;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.toplevel.Identifiable;
import uk.ac.ebi.fg.core_model.xref.Referrer;
import uk.ac.ebi.fg.core_model.xref.XRef;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.PersistenceListener;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Feb 25, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ReferrerPersistenceListener<R extends Identifiable> extends PersistenceListener<R>
{
	private final XRefPersistenceListener xrefPersistenceListener;
	
	public ReferrerPersistenceListener ( EntityManager entityManager )
	{
		super ( entityManager );
		xrefPersistenceListener = new XRefPersistenceListener ( entityManager );
	}

	@Override
	public void prePersist ( R referrer )
	{
		if ( referrer == null || referrer.getId () != null ) return;
		if ( ! ( referrer instanceof Referrer ) ) throw new RuntimeException (
			"Internal Error: " + referrer.getClass ().getName () + " can only be used with an instance of Annotatable"
		);
		
		for ( XRef xref: ((Referrer) referrer).getReferences () )
			xrefPersistenceListener.prePersist ( xref );
	}

}
