/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.CreationListener;

import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.expgraph.Node;

/**
 * Works pre-post processing operations about the {@link Node} objects.
 *
 * <dl><dt>date</dt><dd>Jan 14, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public abstract class NodePersistenceListener<N extends Node> extends CreationListener<N>
{
	/**
	 * This is used to re-use nodes in the DB. If a node in the DB and a new node being submitted are equivalent according
	 * to this comparator, then the existing node will replace the incoming one. For example, two samples are equivalent
	 * if they have the same accession, the same set of attributes and the same set of source samples 
	 * (see {@link ProductComparator}).
	 * 
	 * @see ProductComparator
	 */
	protected Comparator nodeComparator;

	private AccessibleDAO<Node> nodeDao = new AccessibleDAO<Node> ( Node.class, entityManager );  

	public NodePersistenceListener ( EntityManager entityManager ) {
		super ( entityManager );
	}

	/**
	 * Invokes {@link #linkExistingNodes(Node)}.
	 */
	@Override
	public void prePersist ( Node node ) {
		linkExistingNodes ( node );
	}

	/**
	 * Checks the node's upstream/downstream nodes and replaces those nodes that already exists in the DB. This makes 
	 * nodes re-usable. Uses {@link #nodeComparator} to decide if two nodes are equivalent.
	 * 
	 * TODO: we will need that incoming nodes are just the same
	 */
	private void linkExistingNodes ( Node node )
	{
		Set<Node> addLinks = new HashSet<Node> (), delLinks = new HashSet<Node> ();
		Set<Node> ups = node.getUpstreamNodes ();
		for ( Node up: ups ) 
		{
			// Do we need replacement?
			if ( up == null || up.getId () != null ) continue;
			Node upDB = nodeDao.find ( up.getAcc () );
			if ( upDB == null || nodeComparator.compare ( upDB, up ) != 0 ) continue;

			// The node needs to be replaced by an existing one, let's move the links from the node being removed to the
			// one being added
			//
			linkExistingNodes ( up );
			for ( Node upUp: (Set<Node>) up.getUpstreamNodes () ) upDB.addUpstreamNode ( upUp );
			for ( Node upDown: (Set<Node>) up.getDownstreamNodes () ) upDB.addDownstreamNode ( upDown );
			
			// We need to replace the link later, to avoid interference with the iterator in the loop
			addLinks.add ( upDB ); delLinks.add ( up );
		}
		// Do it
		for ( Node del: delLinks ) node.removeUpstreamNode ( del );
		for ( Node add: addLinks ) node.addUpstreamNode ( add );
		
		// Same for downstream nodes
		addLinks = new HashSet<Node> (); delLinks = new HashSet<Node> ();
		Set<Node> downs = node.getDownstreamNodes ();
		for ( Node down: downs ) 
		{
			// Is to be replaced by an existing one?
			if ( down == null || down.getId () != null ) continue;
			Node downDB = nodeDao.find ( down.getAcc () );
			if ( downDB == null || nodeComparator.compare ( downDB, down ) != 0 ) continue;

			// The node needs to be replaced by an existing one, let's move the links from the node being removed to the
			// one being added
			//
			linkExistingNodes ( down );
			for ( Node upUp: (Set<Node>) down.getUpstreamNodes () ) downDB.addUpstreamNode ( upUp );
			for ( Node upDown: (Set<Node>) down.getDownstreamNodes () ) downDB.addDownstreamNode ( upDown );
			
			// We need to replace the link later, to avoid interference with the iterator in the loop
			addLinks.add ( downDB ); delLinks.add ( down );
		}
		// Do it
		for ( Node del: delLinks ) node.removeUpstreamNode ( del );
		for ( Node add: addLinks ) node.addUpstreamNode ( add );
	}

}
