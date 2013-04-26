/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph;

import java.util.Date;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.core_model.expgraph.Product;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.DBStore;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Store;


/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Mar 12, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class BioSampleNormalizer extends ProductNormalizer<BioSample>
{
	public BioSampleNormalizer ( Store store ) {
		super ( store );
	}

	@Override
	public void normalize ( BioSample smp )
	{
		if ( smp == null || smp.getId () != null ) return; 
		super.normalize ( smp );
		
		// mark the time the object creation occurs 
		if ( getStore () instanceof DBStore ) smp.setUpdateDate ( new Date () );
	}

	/**
	 * Set {@link BioSample#setUpdateDate(java.util.Date)} if the parameter is a {@link BioSample} and 
	 * {@link ProductNormalizer#timestamp}.
	 */
	@Override
	protected void setUpdateDate ( Product<?> product )
	{
		if ( !( store instanceof DBStore ) ) return;
		if ( ! ( product instanceof BioSample ) ) return;
		
		((BioSample) product).setUpdateDate ( new Date () );
	}
	
}
