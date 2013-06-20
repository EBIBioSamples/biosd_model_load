/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph.properties;

import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.terms.FreeTextTermNormalizer;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;
import uk.ac.ebi.fg.core_model.expgraph.properties.UnitDimension;

/**
 * Does the {@link FreeTextTermNormalizer} for a {@link Unit} and additionally for its {@link UnitDimension}.
 *
 * <dl><dt>date</dt><dd>Mar 12, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UnitNormalizer extends FreeTextTermNormalizer<Unit>
{
	private final FreeTextTermNormalizer<UnitDimension> dimNormalizer;  
	
	public UnitNormalizer ( Store store ) 
	{
		super ( store );
		dimNormalizer = new FreeTextTermNormalizer<UnitDimension> ( store );
	}

	@Override
	public boolean normalize ( Unit u )
	{
		if ( !super.normalize ( u ) ) return false;
		dimNormalizer.normalize ( u.getDimension () );
		return true;
	}
	
}
