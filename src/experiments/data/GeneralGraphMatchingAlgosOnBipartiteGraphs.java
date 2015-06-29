package experiments.data;

import Diameter.DiameterBFS;
import graphmatching.KhoslaMatchingBipartiteGraph;
import graphmatching.generalgraphs.AllNodeLabelling;
import input.io.GraphLoaderToJGraphT;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.HopcroftKarpBipartiteMatching;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author avishekanand
 */
public class GeneralGraphMatchingAlgosOnBipartiteGraphs {

    private static final Logger log = Logger.getLogger(GraphLoaderToJGraphT.class.getName());

    public static void main(String[] args) throws IOException {
        String filename = (args.length > 0) ? args[0]: "/Users/avishekanand/research/data/delicious/deli-wiki.tsv";
//        String filename = (args.length > 0) ? args[0] : "/Users/avishekanand/research/data/delicious/yahoo/webscope-bipartite-g1/ydata-ysm-advertiser-phrase-graph-v1_0.txt.gz";
//        String filename = (args.length > 0) ? args[0] : "/Users/avishekanand/research/data/delicious/yahoo/webscope-bipartite-g1/sample-cycle.txt";
//        String filename = (args.length > 0) ? args[0]: "/Users/avishekanand/research/data/delicious/sample.tsv";
// 1, 0, 1 for yahoo data
        int headerSpan = (args.length > 1) ? Integer.parseInt(args[1]) : 0;
        int sourceOffset = (args.length > 1) ? Integer.parseInt(args[2]) : 2;
        int targetOffset = (args.length > 1) ? Integer.parseInt(args[3]) : 4;

        GraphLoaderToJGraphT graphConstructor = new GraphLoaderToJGraphT();

        BufferedReader br = getReaderFromFile(filename);

        long time = System.currentTimeMillis();
        
        
        HashSet<String> left = new HashSet<>();
        HashSet<String> right = new HashSet<>();

        UndirectedGraph<String, DefaultEdge> bipartiteGraph
            = graphConstructor.constructBipartiteUndirectedUnweightedGraph(getReaderFromFile(filename), sourceOffset, targetOffset, left, right, headerSpan);

        log.log(Level.INFO, "Bipartite graph constructed in "
            + (System.currentTimeMillis() - time) / 1000 + " seconds  ");
        log.log(Level.INFO, "Edges : " + bipartiteGraph.edgeSet().size() + " Vertices : "
            + bipartiteGraph.vertexSet().size());

        //compare algos in connected components
        //compareOnConnectedComponents(bipartiteGraph);
        compareWithVaryingLoopLimits(bipartiteGraph, left, right);


        time = System.currentTimeMillis();
//        UndirectedGraph<String, DefaultEdge> generalGraph
//            = graphConstructor.constructUndirectedUnweightedGeneralGraph(br, sourceOffset, targetOffset,headerSpan);
//
//        log.log(Level.INFO, "Edges : " + generalGraph.edgeSet().size() + " Vertices : "
//            + generalGraph.vertexSet().size());
//        log.log(Level.INFO, "General graph constructed in "
//            + (System.currentTimeMillis() - time) / 1000 + " seconds  ");

        //experiments with general graphs
        compareWithVaryingLoopLimitsGeneralGraphs(bipartiteGraph);

    }
   
    private static void compareWithVaryingLoopLimits(UndirectedGraph<String, DefaultEdge> bipartiteGraph, HashSet<String> left, HashSet<String> right) {

        long time = System.currentTimeMillis();
        Set<DefaultEdge> khosla = null;
        
        DiameterBFS<String, DefaultEdge> diameterBFS = new DiameterBFS(bipartiteGraph);
        
        int diameterLB = diameterBFS.findDiameterUpperBound();
        log.log(Level.INFO, "Diameter Estimation done in " + (System.currentTimeMillis() - time)/1000 + " seconds");
        
        
        int[] loopLimits = {1, 5, 10, 100, 1000, 10000, 100000, diameterLB};
//        int[] loopLimits = {diam};
        for (int loopLimit : loopLimits) {
            if (loopLimit > diameterLB) {
                continue;
            }
            KhoslaMatchingBipartiteGraph lsa = null;

            if (left.size() > right.size()) {
                lsa = new KhoslaMatchingBipartiteGraph(bipartiteGraph, left, right);
            } else {
                lsa = new KhoslaMatchingBipartiteGraph(bipartiteGraph, right, left);
                lsa.LEFT_PRIMARY = false;
            }

            //run lsa with run bounds
            time = System.currentTimeMillis();
            lsa.isBipartite();
            lsa.run(loopLimit);
            khosla = lsa.getMatching();
            System.out.println("Khosla-Bipartite Matching done in  " + (System.currentTimeMillis() - time) / 1000
                + " seconds matching size : " + khosla.size() + " , Loop Limit : " + loopLimit);

            //lsa.checkAugmentingPath();
        }

//        time = System.currentTimeMillis();
//        HopcroftKarpBipartiteMatching<String, DefaultEdge> alg
//            = new HopcroftKarpBipartiteMatching<String, DefaultEdge>(bipartiteGraph, left, right);
//        Set<DefaultEdge> hopcroft = alg.getMatching();
//        System.out.println("Hopcroft Matching done in  " + (System.currentTimeMillis() - time) / 1000
//            + " seconds matching size : " + hopcroft.size());
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
