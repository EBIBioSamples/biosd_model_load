/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.organizational;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.PersistenceListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.ProductComparator;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties.PropertyValuePersistenceListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms.FreeTextTermPersistenceListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.toplevel.AnnotatablePersistenceListener;

/**
 * Works pre/post process operations for {@link BioSampleGroup}.
 *
 * <dl><dt>date</dt><dd>Jan 17, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class BioSampleGroupPersistenceListener extends AnnotatablePersistenceListener<BioSampleGroup>
{
	private final AccessibleDAO<BioSample> smpDao;
	private final PropertyValuePersistenceListener pvListener; 
	private final ProductComparator smpCmp = new ProductComparator ();
	
	
	public BioSampleGroupPersistenceListener ( EntityManager entityManager ) 
	{
		super ( entityManager );
		pvListener = new PropertyValuePersistenceListener ( entityManager );
		smpDao = new AccessibleDAO<BioSample> ( BioSample.class, entityManager );
	}

	/**
	 * Links to this sample group those samples that are equivalent to samples already existing in the database. 
	 * New incoming samples are discarded from {@link BioSampleGroup#getSamples() sg.getSamples()}.
	 * 
	 */
	@Override
	public void prePersist ( BioSampleGroup sg )
	{
		if ( sg == null || sg.getId () != null ) return;
		super.prePersist ( sg );
		
		Set<BioSample> addSmps = new HashSet<BioSample> (), delSmps = new HashSet<BioSample> ();
		Set<BioSample> samples = sg.getSamples ();
		for ( BioSample sample: samples ) 
		{
			if ( sample == null || sample.getId () != null ) continue;
			BioSample smpDB = smpDao.find ( sample.getAcc () );
			if ( smpDB == null || smpCmp.compare ( smpDB, sample ) != 0 ) continue;
			
			// sample is going to be ditched off, give its sample groups to the equivalents coming from the DB
			for ( BioSampleGroup smpSg: sample.getGroups () ) smpDB.addGroup ( smpSg );
			
			addSmps.add ( smpDB ); delSmps.add ( sample );
		}
		samples.removeAll ( delSmps );
		samples.addAll ( addSmps );

		// Properties
		for ( ExperimentalPropertyValue<?> pv: sg.getPropertyValues () )
			pvListener.prePersist ( pv );
	}
}
