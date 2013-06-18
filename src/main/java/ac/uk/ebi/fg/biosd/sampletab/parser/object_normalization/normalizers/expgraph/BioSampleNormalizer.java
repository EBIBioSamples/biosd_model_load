/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph;

import java.util.Date;

import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry.Operation;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt.JobRegisterDAO;
import uk.ac.ebi.fg.core_model.expgraph.Product;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.DBStore;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Store;


/**
 * The dupe normalizer for {@link BioSample}.
 *
 * <dl><dt>date</dt><dd>Mar 12, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class BioSampleNormalizer extends ProductNormalizer<BioSample>
{
	private final JobRegisterDAO jobRegDao;

	public BioSampleNormalizer ( Store store ) 
	{
		super ( store );
		this.jobRegDao = store instanceof DBStore ? new JobRegisterDAO ( ((DBStore) store ).getEntityManager () ) : null;
	}

	/**
	 * Just update the last-update date.
	 */
	@Override
	public void normalize ( BioSample smp )
	{
		if ( smp == null || smp.getId () != null ) return; 
		super.normalize ( smp );
		
		// mark the time the object creation occurs 
		if ( store instanceof DBStore ) 
		{
			smp.setUpdateDate ( new Date () );
			jobRegDao.create ( smp, Operation.ADD, smp.getUpdateDate () );
		}
		
	}

	/**
	 * Set {@link BioSample#setUpdateDate(java.util.Date)} if the parameter is a {@link BioSample} and 
	 * {@link ProductNormalizer#timestamp}.
	 */
	@Override
	protected void setUpdateDate ( Product<?> existingProd )
	{
		if ( !( store instanceof DBStore ) ) return;
		if ( ! ( existingProd instanceof BioSample ) ) return;
		
		((BioSample) existingProd).setUpdateDate ( new Date () );
		jobRegDao.create ( existingProd, Operation.UPDATE, ((BioSample) existingProd).getUpdateDate () );
	}
	
}
