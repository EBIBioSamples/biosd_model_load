/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.expgraph.Node;
import uk.ac.ebi.fg.core_model.expgraph.Product;

/**
 * Works pre-post processing operations about the {@link Product} objects.
 *
 * <dl><dt>date</dt><dd>Jan 14, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class ProductPersistenceListener extends NodePersistenceListener<Product<?>>
{
	private AccessibleDAO<Product> productDao = new AccessibleDAO<Product> ( Product.class, entityManager );  
	
	public ProductPersistenceListener ( EntityManager entityManager )
	{
		super ( entityManager );
		nodeComparator = new ProductComparator ();
	}

	/**
	 * Invokes {@link Node#prePersist} and then {@link #linkExistingProducts(Product)}.
	 */
	@Override
	public void prePersist ( Product<?> product )
	{
		super.prePersist ( (Node) product );
		linkExistingProducts ( product );
	}

	/**
	 * Works similarly to {@link Node#linkExistingNodes}, checks the node's derived-to/into nodes and replaces those 
	 * products that already exists in the DB. This makes products re-usable. Uses {@link #nodeComparator} to decide if two 
	 * products are equivalent.
	 * 
	 * TODO: we will need that incoming products are just the same
	 * 
	 */
	private void linkExistingProducts ( Product<?> node )
	{
		Set<Product<?>> addLinks = new HashSet<Product<?>> (), delLinks = new HashSet<Product<?>> ();
		Set<Product<?>> ups = node.getDerivedFrom ();
		for ( Product<?> up: ups ) 
		{
			// Do we need replacement?
			if ( up == null || up.getId () != null ) continue;
			Product<?> upDB = productDao.find ( up.getAcc () );
			if ( upDB == null || nodeComparator.compare ( upDB, up ) != 0 ) continue;

			// The node needs to be replaced by an existing one, let's move the links from the node being removed to the
			// one being added
			//
			linkExistingProducts ( up );
			for ( Product<?> upUp: up.getDerivedFrom () ) upDB.addDerivedFrom ( upUp );
			for ( Product<?> upDown: up.getDerivedInto () ) upDB.addDerivedInto ( upDown );
			
			// We need to replace the link later, to avoid interference with the iterator in the loop
			addLinks.add ( upDB ); delLinks.add ( up );
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
			Product<?> downDB = productDao.find ( down.getAcc () );
			if ( downDB == null || nodeComparator.compare ( downDB, down ) != 0 ) continue;

			// The node needs to be replaced by an existing one, let's move the links from the node being removed to the
			// one being added
			//
			linkExistingProducts ( down );
			for ( Product<?> upUp: down.getDerivedFrom () ) downDB.addDerivedFrom ( upUp );
			for ( Product<?> upDown: down.getDerivedInto () ) downDB.addDerivedInto ( upDown );
			
			// We need to replace the link later, to avoid interference with the iterator in the loop
			addLinks.add ( downDB ); delLinks.add ( down );
		}
		// Do it
		for ( Product<?> del: delLinks ) node.removeDerivedInto ( del );
		for ( Product<?> add: addLinks ) node.addDerivedInto ( add );
	}	
}
