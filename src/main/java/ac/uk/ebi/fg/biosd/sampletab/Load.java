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
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.UnitAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabSaferParser;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicType;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicValue;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;
import uk.ac.ebi.fg.core_model.expgraph.properties.UnitDimension;
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
    
    private BioCharacteristicValue convertAtttribute(SCDNodeAttribute a, SampleData st){
        
        BioCharacteristicType h = new BioCharacteristicType ( a.getAttributeType() );
        BioCharacteristicValue v = new BioCharacteristicValue( a.getAttributeValue(), h);
        
        if (AbstractNodeAttributeOntology.class.isInstance(a)){
            AbstractNodeAttributeOntology ao = (AbstractNodeAttributeOntology) a;
            
            //ontology
            if (ao.getTermSourceID() != null && ao.getTermSourceREF() != null){
                TermSource t = st.msi.getTermSource(ao.getTermSourceREF());
                if (t != null && t.getURI() != null && t.getVersion() != null){
                    v.addOntologyTerm ( 
                        new OntologyEntry( ao.getTermSourceID() , 
                            new ReferenceSource(t.getURI(), t.getVersion()) ) );
                }
            }
            
            //unit
            UnitAttribute unit = null;
            if (CommentAttribute.class.isInstance(a) ){
                CommentAttribute co = (CommentAttribute) a;
                unit = co.unit;
            }
            if (CharacteristicAttribute.class.isInstance(a) ){
                CharacteristicAttribute co = (CharacteristicAttribute) a;
                unit = co.unit;
            }
            if (unit != null){
                Unit u = new Unit();
                u.setDimension(new UnitDimension(unit.getAttributeValue()));
                v.setUnit(u);
                AbstractNodeAttributeOntology aou = (AbstractNodeAttributeOntology) unit;
                
                //unit ontology term
                if (aou.getTermSourceID() != null && aou.getTermSourceREF() != null){
                    TermSource t = st.msi.getTermSource(aou.getTermSourceREF());
                    if (t != null && t.getURI() != null && t.getVersion() != null){
                        u.addOntologyTerm ( 
                            new OntologyEntry( aou.getTermSourceID() , 
                                new ReferenceSource(t.getURI(), t.getVersion()) ) );
                    }
                }
            }
            
        }
        
        //TODO database attribute
        
        return v;
    }

    private BioSample convertSampleNode(SampleData st, MSI msi, SampleNode s){
        if (s.getSampleAccession() == null || s.getSampleAccession().length() == 0){
            throw new IllegalArgumentException("SampleNode must be accessioned");
        }
        
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
            BioCharacteristicValue v = convertAtttribute(a, st);
            bs.addPropertyValue(v);
        }
        
        //handle child nodes
        for (Node n : s.getChildNodes()){
            if (SampleNode.class.isInstance(n)){
                SampleNode sn = (SampleNode) n;
                BioSample derivedInto = convertSampleNode(st, msi, sn);
                bs.addDerivedInto(derivedInto);
            }
        }
        
        msi.addSample(bs);
        
        return bs;
        
    }
    
    public synchronized MSI fromSampleData(SampleData st){
        MSI msi = new MSI(st.msi.submissionIdentifier);
        msi.setUpdateDate(st.msi.submissionUpdateDate);
        msi.setReleaseDate(st.msi.submissionReleaseDate);
        //TODO other msi components
        

        for (GroupNode g : st.scd.getNodes(GroupNode.class)){
            BioSampleGroup bg = new BioSampleGroup ( g.getGroupAccession());
            //TODO name
            //TODO description
            
            for(SCDNodeAttribute a: g.attributes){
                BioCharacteristicValue v = convertAtttribute(a,st);
                //TODO make sure a value can be applied to a group
                //bg.addPropertyValue(v);
            }
            
            msi.addSampleGroup(bg);
        }
        
        for (Node n : st.scd.getRootNodes()){
            if (SampleNode.class.isInstance(n)){
                SampleNode sn = (SampleNode) n;
                convertSampleNode(st, msi, sn);
            }
            
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
