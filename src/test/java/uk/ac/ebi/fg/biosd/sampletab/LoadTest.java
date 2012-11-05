package uk.ac.ebi.fg.biosd.sampletab;

import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import ac.uk.ebi.fg.biosd.sampletab.Loader;
import ac.uk.ebi.fg.biosd.sampletab.Persister;

public class LoadTest 
{    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private MSI doTests(String resourcePath)
    {
        URL url = getClass().getClassLoader().getResource( resourcePath );
        
        Loader loader = new Loader();
        
        MSI msi = null;
        
        try {
            msi = loader.fromSampleData(url);
        } catch (ParseException e) {
            log.error("Problem parsing", e);
            fail();
        }
        
        return new Persister ( msi ).persist ( true );
    }
    
    
    
    @Test
    public void testAE(){
        MSI msi = doTests("GAE-MTAB-27/sampletab.txt");
        //TODO finish writing test
    }

    @Test
    public void testSRA(){
        MSI msi = doTests("GEN-SRP001145/sampletab.txt");
        //TODO finish writing test
    }

    @Test
    public void testPRIDE(){
        MSI msi = doTests("GPR-3218/sampletab.txt");
        //TODO finish writing test
    }

    @Test
    public void testIMSR(){
        MSI msi = doTests("GMS-RBRC/sampletab.txt");
        //TODO finish writing test
    }
}
