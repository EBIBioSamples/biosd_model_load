/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph.properties;

import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.terms.FreeTextTermNormalizer;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyType;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;

/**
 * Does the job of {@link FreeTextTermNormalizer} for an {@link ExperimentalPropertyValue} and the same 
 * for {@link ExperimentalPropertyType} and {@link Unit}.
 *
 * <dl><dt>date</dt><dd>Mar 12, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class PropertyValueNormalizer extends FreeTextTermNormalizer<ExperimentalPropertyValue<?>>
{
	private final FreeTextTermNormalizer<ExperimentalPropertyType> typeNormalizer;  
	private final UnitNormalizer unitNormalizer;
	
	public PropertyValueNormalizer ( Store store ) 
	{
		super ( store );
		typeNormalizer = new FreeTextTermNormalizer<ExperimentalPropertyType> ( store );
		unitNormalizer = new UnitNormalizer ( store );
	}

	@Override
	public boolean normalize ( ExperimentalPropertyValue<?> pv )
	{
		if ( !super.normalize ( pv ) ) return false;
		typeNormalizer.normalize ( pv.getType () );
		unitNormalizer.normalize ( pv.getUnit () );
		return true;
	}
	
}
