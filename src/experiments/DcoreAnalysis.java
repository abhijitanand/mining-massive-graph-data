package experiments;

import gnu.trove.TIntObjectHashMap;
import input.io.GraphLoaderToJGraphT;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedSubgraph;

/**
 *
 * @author avishekanand
 */
public class DcoreAnalysis {

    public String COLOR_TOKENIZER = ":";
    
    public int SAMPLE_SIZE = 5;
    
    private static final Logger log = Logger.getLogger( DcoreAnalysis.class.getName() );    
    
    private void colorConnectedComponents(DirectedSubgraph<Integer, DefaultEdge>[] connectedComponents, 
                            int[] representatives, TIntObjectHashMap<String> colors) {
        
        
        for (int i = 0; i < connectedComponents.length; i++) {
            int color = representatives[i];
            
            for (int vertex : connectedComponents[i].vertexSet()) {
                if (!colors.contains(vertex)) {
                    colors.put(vertex, color + "");
                    continue;
                }
                
                String colorString = colors.get(vertex);
                
                int lastColor = -1;
                if (colorString.contains(COLOR_TOKENIZER)) {
                    String[] oldColor = colorString.split(COLOR_TOKENIZER);
                    lastColor = Integer.parseInt(oldColor[oldColor.length-1]);
                } else{
                    lastColor = Integer.parseInt(colorString);
                }
                
                //check if the last is the same as the new color
                if (lastColor != color) {
                    colors.put(vertex, colorString + COLOR_TOKENIZER + color);
                }else{
                   //break;
                }
            }
        }
    }

    public DcoreAnalysis() {

    }

    private void findCoreWiseSizeDistribution(ExamineStructuralGraphProperties g, TIntObjectHashMap<String> labels) {

        TIntObjectHashMap<String> colors = new TIntObjectHashMap<>();
        
        long time = System.currentTimeMillis();
        for (int k = 6; k < 2000; k++) {
            log.log(Level.INFO, "Finding cores for k = " + 6 + " time taken :  "
                            + (time - System.currentTimeMillis())/1000 + " seconds");
            
            if (g.graph.vertexSet().isEmpty()) {
                break;
            }
            g.findCores(k, k);

            DirectedSubgraph<Integer, DefaultEdge>[] connectedComponents
                = g.computeConnectedComponents();

            int[] representatives = findRepresentativeNodes(connectedComponents);

            //color connected components
            colorConnectedComponents(connectedComponents, representatives, colors);
            
            printHeader();
            for (int i = 0; i < connectedComponents.length; i++) {

                Set<Integer> vertexSet = connectedComponents[i].vertexSet();
                
                int nodes = vertexSet.size();
                int edges = connectedComponents[i].edgeSet().size();

                //populate samples with labels (actual strings if label map is )
                String samples = getLabel(representatives[i], labels) + "(C)"; 
                int sampleCount = 0;
                for (int vertex : vertexSet) {
                    if (representatives[i] == vertex) {
                        continue;
                    }
                    samples = samples + ", " + getLabel(vertex, labels);
                    if (sampleCount++ > SAMPLE_SIZE) {
                        break;
                    }
                 }
                
                //double gfd = (edges > 0) ? edges/(double)nodes : 0.0 ;
                double loggfd = (edges > 0) ? Math.log(edges) / (double) Math.log(nodes) : 0.0;

                DecimalFormat df2 = new DecimalFormat( "#.####" );
                System.out.println(nodes + "\t" + edges+ "\t" + df2.format(loggfd)
                        +  "\t" + getLabel(representatives[i], labels)  + "\t"  +
                        samples);
                
            }
        }
    }

    private int[] findRepresentativeNodes(DirectedSubgraph<Integer, DefaultEdge>[] connectedComponents) {
        int[] centroids = new int[connectedComponents.length];

        int idx = 0;
        for (DirectedSubgraph<Integer, DefaultEdge> connectedComponent : connectedComponents) {
            int maxdegree = 0;
            int centroid = -1;

            if (connectedComponent == null) {
                System.out.println("Error : NULL connected component");
            }

            Set<Integer> vertexSet = connectedComponent.vertexSet();
            if (vertexSet == null || vertexSet.isEmpty()) {
                System.out.println("Error : Empty Vertex set");
            }

            for (int vertex : vertexSet) {
                int degree = connectedComponent.inDegreeOf(vertex) + connectedComponent.outDegreeOf(vertex);

                if (maxdegree <= degree) {
                    if (maxdegree == degree) {
                        centroid = (centroid > vertex) ? vertex : centroid;
                    } else {
                        centroid = vertex;
                    }
                    maxdegree = degree;
                }
            }
            centroids[idx++] = centroid;
        }

        return centroids;
    }

    public static void main(String[] args) throws IOException {
        String graphFile = (args.length > 0) ? args[0] : "/Users/avishekanand/research/data/de-yr-graphs/de-2003.gz";
        String mapfile = (args.length > 1) ? args[1] : null;

        log.log(Level.INFO, "Loading the web graph from : " + graphFile);
        long time = System.currentTimeMillis();
        GraphLoaderToJGraphT loader = new GraphLoaderToJGraphT(graphFile);
        DirectedGraph webGraph = loader.getWebGraph();
        log.log(Level.INFO, "Loaded the web graph in " + (System.currentTimeMillis()- time)/1000 + " seconds");
        
        
        DcoreAnalysis dcoreAnalysis = new DcoreAnalysis();
        
        TIntObjectHashMap<String> mappings = null;
        if (mapfile != null) {
            mappings = loader.createIdToLabelMappings(mapfile);
        }
        
        ExamineStructuralGraphProperties g = new ExamineStructuralGraphProperties(webGraph);
        dcoreAnalysis.findCoreWiseSizeDistribution(g, mappings);
    }

    private void printHeader() {
        System.out.println("nodes\tedges\tGFD\tColor\tSamples");
    }

    private String getLabel(int representative, TIntObjectHashMap<String> labels) {
        return (labels != null) ? labels.get(representative) : "" + representative;
    }
}
