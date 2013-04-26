/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.core_model.expgraph.Node;
import uk.ac.ebi.fg.core_model.expgraph.Product;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph.properties.PropertyValueNormalizer;

/**
 * Works out normalisation operations about the {@link Product} objects.
 *
 * <dl><dt>date</dt><dd>Mar 12, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProductNormalizer
  <P extends Product<?>> 
	extends NodeNormalizer<P>
{
	private final PropertyValueNormalizer pvListener;
	
	public ProductNormalizer ( Store store )
	{
		super ( store );
		pvListener = new PropertyValueNormalizer ( store );
		nodeComparator = new ProductComparator ();
	}

	/**
	 * Invokes {@link Node#prePersist} and then {@link #linkExistingProducts(Product)}.
	 */
	@Override
	public void normalize ( P product )
	{
		if ( product == null || product.getId () != null ) return; 
		
		super.normalize ( product );
		linkExistingProducts ( product );

		// Properties
		for ( ExperimentalPropertyValue<?> pv: product.getPropertyValues () )
			pvListener.normalize ( pv );
	}

	/**
	 * Works similarly to {@link Node#linkExistingNodes}, checks the node's derived-to/into nodes and replaces those 
	 * products that already exists in the DB. This makes products re-usable. Uses {@link #nodeComparator} to decide if two 
	 * products are equivalent.
	 * 
	 * TODO: we need to check that incoming products are just the same
	 * 
	 */
	@SuppressWarnings ( "unchecked" )
	private void linkExistingProducts ( Product<?> node )
	{
		Set<Product<?>> addLinks = new HashSet<Product<?>> (), delLinks = new HashSet<Product<?>> ();
		Set<Product<?>> ups = node.getDerivedFrom ();
		for ( Product<?> up: ups ) 
		{
			// Do we need replacement?
			if ( up == null || up.getId () != null ) continue;
			Product<?> upS = store.find ( up, up.getAcc () );
			if ( upS == null || up == upS || nodeComparator.compare ( upS, up ) != 0 ) continue;

			// The node needs to be replaced by an existing one, let's move the links from the node being removed to the
			// one being added
			//
			linkExistingProducts ( up );
			for ( Product<?> upUp: up.getDerivedFrom () ) upS.addDerivedFrom ( upUp );
			for ( Product<?> upDown: up.getDerivedInto () ) upS.addDerivedInto ( upDown );
			
			// We need to replace the link later, to avoid interference with the iterator in the loop
			addLinks.add ( upS ); delLinks.add ( up );
			
			// mark the time the object update occurs 
			setUpdateDate ( upS );
		}
		// Do it
		for ( Product<?> del: delLinks ) node.removeDerivedFrom ( del );
		for ( Product<?> add: addLinks ) node.addDerivedFrom ( add );
		
		// Do it again for derivedInto
		addLinks = new HashSet<Product<?>> (); delLinks = new HashSet<Product<?>> ();
		Set<Product<?>> donws = node.getDerivedInto ();
		for ( Product<?> down: donws ) 
		{
			if ( down == null || down.getId () != null ) continue;
			Product<?> downS = store.find ( down, down.getAcc () );
			if ( downS == null || downS == down || nodeComparator.compare ( downS, down ) != 0 ) continue;

			// The node needs to be replaced by an existing one, let's move the links from the node being removed to the
			// one being added
			//
			linkExistingProducts ( down );
			for ( Product<?> upUp: down.getDerivedFrom () ) downS.addDerivedFrom ( upUp );
			for ( Product<?> upDown: down.getDerivedInto () ) downS.addDerivedInto ( upDown );
			
			// We need to replace the link later, to avoid interference with the iterator in the loop
			addLinks.add ( downS ); delLinks.add ( down );
			
			// mark the time the object update occurs 
			setUpdateDate ( downS );
		}
		// Do it
		for ( Product<?> del: delLinks ) node.removeDerivedInto ( del );
		for ( Product<?> add: addLinks ) node.addDerivedInto ( add );
	}	

	/**
	 * Does nothing by default. {@link BioSampleNormalizer} overrides this by setting the update date for that kind of
	 * object. 
	 */
	protected void setUpdateDate ( Product <?> node )
	{
	}
}
