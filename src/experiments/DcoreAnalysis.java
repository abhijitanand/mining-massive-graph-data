package experiments;

import gnu.trove.TIntObjectHashMap;
import input.io.GraphLoaderToJGraphT;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Set;
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
        
        for (int k = 6; k < 2000; k++) {
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

                String samples = labels.get(representatives[i]) + "(C)";
                int sampleCount = 0;
                for (int vertex : vertexSet) {
                    if (representatives[i] == vertex) {
                        continue;
                    }
                    samples = samples + ", " + labels.get(vertex);
                    if (sampleCount++ > SAMPLE_SIZE) {
                        break;
                    }
                }
                
                
                //double gfd = (edges > 0) ? edges/(double)nodes : 0.0 ;
                double loggfd = (edges > 0) ? Math.log(edges) / (double) Math.log(nodes) : 0.0;

                DecimalFormat df2 = new DecimalFormat( "#.####" );
                if (labels != null) {
                    System.out.println(nodes + "\t" + edges+ "\t" + df2.format(loggfd)
                        +  "\t" + colors.get(representatives[i])  + "\t"  +
                        samples);
                } else {
                    System.out.println(nodes + "\t" + edges+ "\t" + df2.format(loggfd) + "\t"  +
                        labels.get(representatives[i]) +  "\t" + colors.get(representatives[i]));
                }
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

        GraphLoaderToJGraphT loader = new GraphLoaderToJGraphT(graphFile);
        DirectedGraph webGraph = loader.getWebGraph();

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

}
