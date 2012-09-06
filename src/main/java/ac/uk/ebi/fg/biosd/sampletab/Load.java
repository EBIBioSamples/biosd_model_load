package ac.uk.ebi.fg.biosd.sampletab;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import uk.ac.ebi.arrayexpress2.magetab.datamodel.graph.Node;
import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.GroupNode;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.AbstractNodeAttributeOntology;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabSaferParser;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicType;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicValue;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;

public class Load {

    
    public MSI fromSampleData(String filename) throws ParseException{
        return fromSampleData(new File(filename));
    }
    
    public MSI fromSampleData(File file) throws ParseException{
        try {
            return fromSampleData(file.toURI().toURL());
        }
        catch (MalformedURLException e) {
            throw new ParseException("File '" + file.getAbsolutePath() + " could not be resolved to a valid URL", e);
        }
    }
    
    
    
    public MSI fromSampleData(URL url) throws ParseException{
        SampleTabSaferParser parser = new SampleTabSaferParser();
        SampleData sampledata;
        sampledata = parser.parse(url);
        return fromSampleData(sampledata);
    }

    public MSI fromSampleData(SampleData st){
        MSI msi = new MSI(st.msi.submissionIdentifier);
        msi.setUpdateDate(st.msi.submissionUpdateDate);
        msi.setReleaseDate(st.msi.submissionReleaseDate);
        //TODO other msi components
        

        for (GroupNode g : st.scd.getNodes(GroupNode.class)){
            BioSampleGroup bg = new BioSampleGroup ( g.getGroupAccession());

            //TODO add characteristics to groups
            
            msi.addSampleGroup(bg);
        }
        
        for (SampleNode s : st.scd.getNodes(SampleNode.class)){
            //TODO check is accessioned
            
            BioSample bs = new BioSample(s.getSampleAccession());
            
            bs.addPropertyValue(
                    new BioCharacteristicValue(s.getNodeName(), 
                            new BioCharacteristicType("Sample Name")));
            if (s.getSampleDescription() != null){
                bs.addPropertyValue(
                        new BioCharacteristicValue(s.getSampleDescription(), 
                                new BioCharacteristicType("Sample Description")));
            }
            
            for(SCDNodeAttribute a: s.attributes){
                BioCharacteristicType h = new BioCharacteristicType ( a.getAttributeType() );
                BioCharacteristicValue v = new BioCharacteristicValue( a.getAttributeValue(), h);
                
                if (AbstractNodeAttributeOntology.class.isInstance(a)){
                    AbstractNodeAttributeOntology ao = (AbstractNodeAttributeOntology) a;
                    
                    //ontology
                    if (ao.getTermSourceID() != null && ao.getTermSourceREF() != null){
                        //no conveinient way to get term source from name
                        //so do loop to find it
                        String termSourceURI = null;
                        String termSourceVersion = null;
                        for (TermSource t : st.msi.termSources){
                            if (t.getName().equals(ao.getTermSourceREF())){
                                termSourceURI = t.getURI();
                                termSourceVersion = t.getVersion();
                                break;
                            }
                        }
                        
                        if (termSourceURI != null && termSourceVersion != null){
                            v.addOntologyTerm ( 
                                    new OntologyEntry( ao.getTermSourceID() , 
                                            new ReferenceSource(termSourceURI, termSourceVersion) ) );
                        }
                    }   
                }
                //TODO unit
                
                bs.addPropertyValue(v);
            }
            
            msi.addSample(bs);
        }

        //put BioSamples into BioSampleGroups
        for (GroupNode g : st.scd.getNodes(GroupNode.class)){
            BioSampleGroup bg = null;
            for (BioSampleGroup bg_test : msi.getSampleGroups()){
                if (bg_test.getAcc().equals(g.getGroupAccession())){
                    bg = bg_test;
                    break;
                }
            }
            //TODO check bg is not null at this point
            
            for(Node p : g.getParentNodes()){
                //TODO check this is a sample;
                SampleNode s = (SampleNode) p;
                
                for (BioSample bs : msi.getSamples()){
                    if (bs.getAcc().equals(s.getSampleAccession())){
                        bg.addSample(bs);
                        break;
                    }
                }
            }
        }
        
        
        
        //TODO sample relationships
        
        
        return msi;
    }

}
