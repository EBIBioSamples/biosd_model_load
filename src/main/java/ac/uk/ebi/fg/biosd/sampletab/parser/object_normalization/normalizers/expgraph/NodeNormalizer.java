/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.fg.core_model.expgraph.Node;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel.AnnotatableNormalizer;

/**
 * Works out normalisation about the {@link Node} objects.
 *
 * <dl><dt>date</dt><dd>Jan 14, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public abstract class NodeNormalizer<N extends Node> extends AnnotatableNormalizer<N>
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

	public NodeNormalizer ( Store store ) {
		super ( store );
	}

	/**
	 * Invokes {@link #linkExistingNodes(Node)}.
	 */
	@Override
	public void normalize ( N node ) 
	{
		if ( node == null || node.getId () != null ) return;
		super.normalize ( node );
		
		linkExistingNodes ( node );
	}

	/**
	 * Checks the node's upstream/downstream nodes and replaces those nodes that already exists in the DB. This makes 
	 * nodes re-usable. Uses {@link #nodeComparator} to decide if two nodes are equivalent.
	 * 
	 * TODO: we need to check that incoming products are just the same
	 */
	private void linkExistingNodes ( Node node )
	{
		Set<Node> addLinks = new HashSet<Node> (), delLinks = new HashSet<Node> ();
		Set<Node> ups = node.getUpstreamNodes ();
		for ( Node up: ups ) 
		{
			// Do we need replacement?
			if ( up == null || up.getId () != null ) continue;
			Node upS = store.find ( up, up.getAcc () );
			if ( upS == null || up == upS || nodeComparator.compare ( upS, up ) != 0 ) continue;

			// The node needs to be replaced by an existing one, let's move the links from the node being removed to the
			// one being added
			//
			linkExistingNodes ( up );
			for ( Node upUp: (Set<Node>) up.getUpstreamNodes () ) upS.addUpstreamNode ( upUp );
			for ( Node upDown: (Set<Node>) up.getDownstreamNodes () ) upS.addDownstreamNode ( upDown );
			
			// We need to replace the link later, to avoid interference with the iterator in the loop
			addLinks.add ( upS ); delLinks.add ( up );
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
			Node downS = store.find ( down, down.getAcc () );
			if ( downS == null || down == downS || nodeComparator.compare ( downS, down ) != 0 ) continue;

			// The node needs to be replaced by an existing one, let's move the links from the node being removed to the
			// one being added
			//
			linkExistingNodes ( down );
			for ( Node upUp: (Set<Node>) down.getUpstreamNodes () ) downS.addUpstreamNode ( upUp );
			for ( Node upDown: (Set<Node>) down.getDownstreamNodes () ) downS.addDownstreamNode ( upDown );
			
			// We need to replace the link later, to avoid interference with the iterator in the loop
			addLinks.add ( downS ); delLinks.add ( down );
		}
		// Do it
		for ( Node del: delLinks ) node.removeUpstreamNode ( del );
		for ( Node add: addLinks ) node.addUpstreamNode ( add );
	}

}
