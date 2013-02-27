/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.xrefs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.xref.ReferenceSourceDAO;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;
import uk.ac.ebi.fg.core_model.xref.XRef;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.PersistenceListener;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Feb 13, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class XRefPersistenceListener extends PersistenceListener<XRef>
{
	private final ReferenceSourceDAO<ReferenceSource> srcdao;

	public XRefPersistenceListener ( EntityManager entityManager ) 
	{
		super ( entityManager );
		srcdao = new ReferenceSourceDAO<ReferenceSource> ( ReferenceSource.class, entityManager );
	}

	/** Check re-usability of Ref Sources */
	@Override
	public void prePersist ( XRef xref )
	{
		if ( xref == null || xref.getId () != null ) return;
		
		ReferenceSource src = xref.getSource ();
		if ( src == null ) return; // This is actually an error and will pop-up later.
		ReferenceSource srcDB = srcdao.find ( src.getAcc (), src.getVersion () );
		if ( srcDB == null ) return;
		
		Exception theEx = null;
		try
		{
			Method setPropM = xref.getClass ().getMethod ( "setSource", ReferenceSource.class );
			setPropM.setAccessible ( true );
			setPropM.invoke ( xref, srcDB );
		} 
		catch ( SecurityException ex ) { theEx = ex; }
		catch ( NoSuchMethodException ex ) { theEx = ex; }
		catch ( IllegalArgumentException ex ) { theEx = ex; }
		catch ( IllegalAccessException ex ) { theEx = ex; }
		catch ( InvocationTargetException ex ) { theEx = ex; }
		finally {
			if ( theEx != null )
				throw new RuntimeException ( "Internal error while persisting " + xref + ": " + theEx.getMessage (), theEx );
		}
	}

}
