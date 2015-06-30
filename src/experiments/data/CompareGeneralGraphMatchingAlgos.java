package experiments.data;

import Diameter.DiameterBFS;
import graphmatching.generalgraphs.AllNodeLabelling;
import input.io.GraphLoaderToJGraphT;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.EdmondsBlossomShrinking;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author avishekanand
 */
public class CompareGeneralGraphMatchingAlgos {

    private static final Logger log = Logger.getLogger(GraphLoaderToJGraphT.class.getName());

    public static void main(String[] args) throws IOException {

//        String filename = (args.length > 0) ? args[0]: "/Users/avishekanand/research/data/delicious/deli-wiki.tsv";
//        String filename = (args.length > 0) ? args[0] : "/Users/avishekanand/research/data/delicious/sample.tsv";
        String filename = (args.length > 0) ? args[0]: "/Users/avishekanand/research/data/delicious/amazon-graph.txt";
//        String filename = (args.length > 0) ? args[0]: "/Users/avishekanand/research/data/delicious/handgraph.txt";
        
        int headerSpan = (args.length > 1) ? Integer.parseInt(args[1]) : 4;
        int sourceOffset = (args.length > 1) ? Integer.parseInt(args[2]) : 0;
        int targetOffset = (args.length > 1) ? Integer.parseInt(args[3]) : 1;

        GraphLoaderToJGraphT graphConstructor = new GraphLoaderToJGraphT();
        BufferedReader br = getReaderFromFile(filename);
        long time = System.currentTimeMillis();

        time = System.currentTimeMillis();
        UndirectedGraph<String, DefaultEdge> generalGraph
            = graphConstructor.constructUndirectedUnweightedGeneralGraph(br, sourceOffset, targetOffset, headerSpan);

        log.log(Level.INFO, "Edges : " + generalGraph.edgeSet().size() + " Vertices : "
            + generalGraph.vertexSet().size());
        log.log(Level.INFO, "General graph constructed in "
            + (System.currentTimeMillis() - time) / 1000 + " seconds  ");

        //experiments with general graphs
        compareWithVaryingLoopLimitsGeneralGraphs(generalGraph);

        time = System.currentTimeMillis();
        EdmondsBlossomShrinking ed = new EdmondsBlossomShrinking(generalGraph);
        Set<DefaultEdge> edmonds = ed.getMatching();
        System.out.println("Edmonds Blossom Matching done in  " + (System.currentTimeMillis() - time) / 1000
            + " seconds matching size : " + edmonds.size());
    }

    private static void compareWithVaryingLoopLimitsGeneralGraphs(UndirectedGraph<String, DefaultEdge> generalGraph) {
        Set<DefaultEdge> khosla = null;
        
        long time = System.currentTimeMillis();
        DiameterBFS<String, DefaultEdge> diameterBFS = new DiameterBFS(generalGraph);
        
        int diameterLB = diameterBFS.findDiameterUpperBound();
        log.log(Level.INFO, "Diameter Estimation done in " + (System.currentTimeMillis() - time)/1000 + " seconds");
        
        int[] loopLimits = {1, 5, 10, 100, 1000, 10000, 100000, diameterLB};
//        int[] loopLimits = {diam};
        for (int loopLimit : loopLimits) {
            if (loopLimit > diameterLB) {
                continue;
            }
            AllNodeLabelling lsa = new AllNodeLabelling(generalGraph);
            //KhoslaMatchingGeneralGraphsBallBinImplementation lsa = new KhoslaMatchingGeneralGraphsBallBinImplementation(generalGraph);    
            //run lsa with run bounds
            time = System.currentTimeMillis();
            lsa.run(loopLimit);
            khosla = lsa.getMatching();
            System.out.println("Khosla Matching on General Graphs done in  " + (System.currentTimeMillis() - time) / 1000
                + " seconds matching size : " + khosla.size() + " , Loop Limit : " + loopLimit);

            //lsa.checkAugmentingPath();
        }
        log.log(Level.INFO, "Khosla Matchings Done..");
    }

    private static BufferedReader getReaderFromFile(String filename) throws IOException {
        BufferedReader br;
        if (filename.endsWith(".gz")) {
            br = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(filename))));
        } else {
            br = new BufferedReader(new FileReader(filename));
        }

        return br;
    }

}
