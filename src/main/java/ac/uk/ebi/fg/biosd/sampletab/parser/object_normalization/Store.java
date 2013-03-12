/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization;


/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public interface Store
{
	public <T> T find ( T newObject, String... targetIds ); 
}
