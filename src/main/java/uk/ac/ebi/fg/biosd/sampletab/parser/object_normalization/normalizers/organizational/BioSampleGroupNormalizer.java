/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.organizational;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry.Operation;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt.JobRegisterDAO;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.DBStore;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph.ProductComparator;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph.properties.PropertyValueNormalizer;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel.AnnotatableNormalizer;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;

/**
 * Works normalisation operations for {@link BioSampleGroup}.
 *
 * <dl><dt>date</dt><dd>Mar 12, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class BioSampleGroupNormalizer extends AnnotatableNormalizer<BioSampleGroup>
{
	private final PropertyValueNormalizer pvNormalizer; 
	private final ProductComparator smpCmp = new ProductComparator ();
	private final JobRegisterDAO jobRegDao;
	
	public BioSampleGroupNormalizer ( Store store ) 
	{
		super ( store );
		pvNormalizer = new PropertyValueNormalizer ( store );
		this.jobRegDao = store instanceof DBStore ? new JobRegisterDAO ( ((DBStore) store ).getEntityManager () ) : null;
	}

	/**
	 * Links to this sample group those samples that are equivalent to samples already existing in the database. 
	 * New incoming samples are discarded from {@link BioSampleGroup#getSamples() sg.getSamples()}.
	 * 
	 */
	@Override
	public void normalize ( BioSampleGroup sg )
	{
		if ( sg == null || sg.getId () != null ) return;
		super.normalize ( sg );
		
		Set<BioSample> addSmps = new HashSet<BioSample> (), delSmps = new HashSet<BioSample> ();
		Set<BioSample> samples = sg.getSamples ();
		for ( BioSample sample: samples ) 
		{
			if ( sample == null || sample.getId () != null ) continue;
			BioSample smpS = store.find ( sample, sample.getAcc () );
			if ( smpS == null || sample == smpS || smpCmp.compare ( smpS, sample ) != 0 ) continue;
			
			// sample is going to be ditched off, give its sample groups to the equivalents coming from the DB
			for ( BioSampleGroup smpSg: sample.getGroups () ) smpS.addGroup ( smpSg );
			
			addSmps.add ( smpS ); delSmps.add ( sample );
			
			// mark the time the object update occurs 
			if ( store instanceof DBStore ) 
			{
				smpS.setUpdateDate ( new Date () );
				jobRegDao.create ( sg, Operation.UPDATE, smpS.getUpdateDate () );
			}
		}
		samples.removeAll ( delSmps );
		samples.addAll ( addSmps );

		// Properties
		for ( ExperimentalPropertyValue<?> pv: sg.getPropertyValues () )
			pvNormalizer.normalize ( pv );
		
		// mark the time the object creation occurs 
		if ( store instanceof DBStore ) 
		{
			sg.setUpdateDate ( new Date () );
			jobRegDao.create ( sg, Operation.ADD, sg.getUpdateDate () );
		}
	}
}
