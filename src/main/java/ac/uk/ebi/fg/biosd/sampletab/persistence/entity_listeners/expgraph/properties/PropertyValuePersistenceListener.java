/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyType;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms.FreeTextTermPersistenceListener;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Feb 25, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class PropertyValuePersistenceListener extends FreeTextTermPersistenceListener<ExperimentalPropertyValue<?>>
{
	private final FreeTextTermPersistenceListener<ExperimentalPropertyType> typePersistenceListener;  
	private final UnitPersistenceListener unitPersistenceListener;
	
	public PropertyValuePersistenceListener ( EntityManager entityManager ) 
	{
		super ( entityManager );
		typePersistenceListener = new FreeTextTermPersistenceListener<ExperimentalPropertyType> ( entityManager );
		unitPersistenceListener = new UnitPersistenceListener ( entityManager );
	}

	@Override
	public void prePersist ( ExperimentalPropertyValue<?> pv )
	{
		if ( pv == null || pv.getId () != null ) return; 
		super.prePersist ( pv );
		typePersistenceListener.prePersist ( pv.getType () );
		unitPersistenceListener.prePersist ( pv.getUnit () );
	}
	
}
