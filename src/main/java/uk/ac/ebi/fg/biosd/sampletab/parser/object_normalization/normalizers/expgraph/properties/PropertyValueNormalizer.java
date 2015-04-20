/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph.properties;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.terms.FreeTextTermNormalizer;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel.AnnotatableNormalizer;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyType;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;
import uk.ac.ebi.fg.core_model.expgraph.properties.dataitems.DataItem;

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
	private final AnnotatableNormalizer<DataItem> dataItemNormalizer;
	
	public PropertyValueNormalizer ( Store store ) 
	{
		super ( store );
		typeNormalizer = new FreeTextTermNormalizer<ExperimentalPropertyType> ( store );
		unitNormalizer = new UnitNormalizer ( store );
		dataItemNormalizer = new AnnotatableNormalizer<DataItem> ( store );
	}

	@Override
	public boolean normalize ( ExperimentalPropertyValue<?> pv )
	{
		if ( !super.normalize ( pv ) ) return false;
		typeNormalizer.normalize ( pv.getType () );
		unitNormalizer.normalize ( pv.getUnit () );
		linkExistingDataItems ( pv );
		
		return true;
	}

	private void linkExistingDataItems ( ExperimentalPropertyValue<?> pv )
	{
		Set<DataItem> dis = pv.getDataItems (), delDis = new HashSet<DataItem> (), addDis = new HashSet<DataItem> ();
		
		for ( DataItem di: dis )
		{
			DataItem diS = store.find ( di );
			if ( diS == null ) 
			{
				dataItemNormalizer.normalize ( di );
				continue;
			}
			if (di == diS ) continue; 
			
			delDis.add ( di ); addDis.add ( diS );
		}
		
		dis.removeAll ( delDis ); dis.addAll ( addDis );
	}
}
