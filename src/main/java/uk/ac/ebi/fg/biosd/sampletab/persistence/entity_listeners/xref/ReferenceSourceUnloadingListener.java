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
	public long postRemove ( ReferenceSource entity )
	{
		// We previosuly used HQL, however this seems to have bad performance, due to temporary tables 
		// (http://in.relation.to/Bloggers/MultitableBulkOperations)
		
		String sql = "DELETE FROM reference_source\n" +
			"WHERE id NOT IN ( SELECT source_id FROM onto_entry )\n" + 
			"AND id NOT IN ( SELECT source_id FROM xref )\n" + 
			"AND id NOT IN ( SELECT source_id FROM xref )";

		long result = entityManager.createNativeQuery ( sql ).executeUpdate (); 

		sql = "DELETE FROM db_ref_src\n" +
			"WHERE id NOT IN ( SELECT source_id FROM onto_entry )\n" + 
			"AND id NOT IN ( SELECT source_id FROM xref )\n" + 
			"AND id NOT IN ( SELECT database_id FROM msi_database )\n" +
			"AND id NOT IN ( SELECT database_id FROM sample_database )\n" +
			"AND id NOT IN ( SELECT database_id FROM sg_database )\n";

		result += entityManager.createNativeQuery ( sql ).executeUpdate (); 

		// TODO: AOP
		log.trace ( String.format ( "%s.postRemove( null ): returning %d", ReferenceSource.class.getSimpleName (), result ));
		return result;
	}

}
