/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.xref;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;

/**
 * Unloading pre/post processor for {@link ReferenceSource} entities.
 *
 * <dl><dt>date</dt><dd>Apr 18, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ReferenceSourceUnloadingListener extends UnloadingListener<ReferenceSource>
{
	public ReferenceSourceUnloadingListener ( EntityManager entityManager ) {
		super ( entityManager );
	}

	@Override
	public long preRemove ( ReferenceSource entity )
	{
		return 0;
	}

	/**
	 * Removes all dangling {@link ReferenceSource} records.
	 */
	@Override
	public long postRemoveGlobally ()
	{
		// We previosuly used HQL, however this seems to have bad performance, due to temporary tables 
		// (http://in.relation.to/Bloggers/MultitableBulkOperations)
		
		String sql = "DELETE FROM reference_source\n" +
			"WHERE id NOT IN ( SELECT source_id FROM onto_entry )\n" + 
			"AND id NOT IN ( SELECT xref_source_id FROM annotation )\n" + 
			"AND id NOT IN ( SELECT referencesources_id FROM msi_reference_source )";

		long result = entityManager.createNativeQuery ( sql ).executeUpdate (); 

		// TODO: AOP
		log.trace ( String.format ( "%s.postRemoveGlobally(): returning %d", this.getClass().getSimpleName (), result ));
		return result;
	}

}
