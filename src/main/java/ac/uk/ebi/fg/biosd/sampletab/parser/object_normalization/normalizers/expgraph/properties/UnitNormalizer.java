/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph.properties;

import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;
import uk.ac.ebi.fg.core_model.expgraph.properties.UnitDimension;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.terms.FreeTextTermNormalizer;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Mar 12, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UnitNormalizer extends FreeTextTermNormalizer<Unit>
{
	private final FreeTextTermNormalizer<UnitDimension> dimPersistenceListener;  
	
	public UnitNormalizer ( Store store ) 
	{
		super ( store );
		dimPersistenceListener = new FreeTextTermNormalizer<UnitDimension> ( store );
	}

	@Override
	public void normalize ( Unit u )
	{
		if ( u == null || u.getId () != null ) return;
		super.normalize ( u );
		dimPersistenceListener.normalize ( u.getDimension () );
	}
	
}
