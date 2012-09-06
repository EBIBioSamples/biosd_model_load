package uk.ac.ebi.fg.biosd.sampletab;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;

import ac.uk.ebi.fg.biosd.sampletab.Load;

import junit.framework.TestCase;

public class LoadTest extends TestCase {

    private Logger log = LoggerFactory.getLogger(getClass());
    
    private MSI doTests(String resourcePath){
        URL url = getClass().getClassLoader().getResource(resourcePath);
        
        Load load = new Load();
        
        MSI msi = null;
        
        try {
            msi = load.fromSampleData(url);
        } catch (ParseException e) {
            log.error("Problem parsing", e);
            fail();
        }
        return msi;
    }
    
    
    public void testAE(){
        MSI msi = doTests("GAE-MTAB-27/sampletab.txt");
        //TODO finish writing test
    }

    public void testSRA(){
        MSI msi = doTests("GEN-SRP001145/sampletab.txt");
        //TODO finish writing test
    }

    public void testPRIDE(){
        MSI msi = doTests("GPR-3218/sampletab.txt");
        //TODO finish writing test
    }

    public void testIMSR(){
        MSI msi = doTests("GMS-RBRC/sampletab.txt");
        //TODO finish writing test
    }
    

}
