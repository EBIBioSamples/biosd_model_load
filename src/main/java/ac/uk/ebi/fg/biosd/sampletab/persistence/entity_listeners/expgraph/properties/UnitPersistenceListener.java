/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;
import uk.ac.ebi.fg.core_model.expgraph.properties.UnitDimension;

import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms.FreeTextTermPersistenceListener;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Feb 25, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UnitPersistenceListener extends FreeTextTermPersistenceListener<Unit>
{
	private final FreeTextTermPersistenceListener<UnitDimension> dimPersistenceListener;  
	
	public UnitPersistenceListener ( EntityManager entityManager ) 
	{
		super ( entityManager );
		dimPersistenceListener = new FreeTextTermPersistenceListener<UnitDimension> ( entityManager );
	}

	@Override
	public void prePersist ( Unit u )
	{
		if ( u == null || u.getId () != null ) return;
		super.prePersist ( u );
		dimPersistenceListener.prePersist ( u.getDimension () );
	}
	
}
