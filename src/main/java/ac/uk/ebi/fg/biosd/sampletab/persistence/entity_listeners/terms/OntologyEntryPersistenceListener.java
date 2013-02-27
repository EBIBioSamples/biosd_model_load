/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.xref.ReferenceSourceDAO;
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
public class OntologyEntryPersistenceListener extends PersistenceListener<OntologyEntry>
{
	private final ReferenceSourceDAO<ReferenceSource> srcdao;

	public OntologyEntryPersistenceListener ( EntityManager entityManager ) {
		super ( entityManager );
		srcdao = new ReferenceSourceDAO<ReferenceSource> ( ReferenceSource.class, entityManager );
	}

	/** Check re-usability of OEs and, in turn, of Ref Sources */
	@Override
	public void prePersist ( OntologyEntry oe )
	{
		if ( oe == null || oe.getId () != null ) return;
		
		ReferenceSource src = oe.getSource ();
		if ( src == null ) return; // This is actually an error and will pop-up later.
		ReferenceSource srcDB = srcdao.find ( src.getAcc (), src.getVersion () );
		if ( srcDB == null ) return;
		
		Exception theEx = null;
		try
		{
			Method setPropM = oe.getClass ().getMethod ( "setSource", ReferenceSource.class );
			setPropM.setAccessible ( true );
			setPropM.invoke ( oe, srcDB );
		} 
		catch ( SecurityException ex ) { theEx = ex; }
		catch ( NoSuchMethodException ex ) { theEx = ex; }
		catch ( IllegalArgumentException ex ) { theEx = ex; }
		catch ( IllegalAccessException ex ) { theEx = ex; }
		catch ( InvocationTargetException ex ) { theEx = ex; }
		finally {
			if ( theEx != null )
				throw new RuntimeException ( "Internal error while persisting " + oe + ": " + theEx.getMessage (), theEx );
		}
	}

}
