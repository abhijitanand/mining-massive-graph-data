package experiments;

import com.sun.tools.javac.util.Pair;
import graphmatching.KhoslaMatchingBipartiteGraph;
import input.io.GraphLoaderToJGraphT;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.HopcroftKarpBipartiteMatching;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UndirectedSubgraph;

/**
 *
 * @author avishekanand
 */
public class CompareBipartiteGraphMatchingAlgos {

    private static final Logger log = Logger.getLogger(GraphLoaderToJGraphT.class.getName());

    public static void main(String[] args) throws IOException {

        String filename = (args.length > 0) ? args[0]: "/Users/avishekanand/research/data/delicious/deli-wiki.tsv";

//        String filename = (args.length > 0) ? args[0] : "/Users/avishekanand/research/data/delicious/sample.tsv";
        
        GraphLoaderToJGraphT graphConstructor = new GraphLoaderToJGraphT();
        
        BufferedReader br;
        if (filename.endsWith(".gz")) {
            br = new BufferedReader(new InputStreamReader (
                                         new GZIPInputStream(new FileInputStream(filename))));
        }else{
            br = new BufferedReader(new FileReader(filename));
        }
        
        long time = System.currentTimeMillis();

        HashSet<String> left = new HashSet<>();
        HashSet<String> right = new HashSet<>();

        UndirectedGraph<String, DefaultEdge> bipartiteGraph
            = graphConstructor.constructBipartiteUndirectedUnweightedGraph(br, 2, 4, left, right,0);

        int treeComponents = identifyTreeStructures(bipartiteGraph, left, right);

        log.log(Level.INFO, "Number of Tree components :  " + treeComponents + " Left set: "
            + left.size() + " right set : " + right.size());
        log.log(Level.INFO, "Bipartite graph constructed in "
            + (System.currentTimeMillis() - time) / 1000 + " seconds  ");

        //compare algos in connected components
        //compareOnConnectedComponents(bipartiteGraph);
        compareWithVaryingLoopLimits(bipartiteGraph, left, right);
    }

    private static int identifyTreeStructures(UndirectedGraph<String, DefaultEdge> bipartiteGraph,
        HashSet<String> left, HashSet<String> right) {
        int count = 0;

        for (String source : left) {
            boolean isTree = true;
            for (DefaultEdge edge : bipartiteGraph.edgesOf(source)) {
                String target = bipartiteGraph.getEdgeTarget(edge);
                //System.out.println(source + ", " + target + " : " + bipartiteGraph.edgesOf(target).size() + "," + bipartiteGraph.degreeOf(target));
                if (bipartiteGraph.degreeOf(target) > 1) {
                    isTree = false;
                    break;
                }
            }
            count = (isTree == true) ? count + 1 : count;
        }

        return count;
    }

    private static void compareOnConnectedComponents(UndirectedGraph<String, DefaultEdge> bipartiteGraph) {
        //find connected Components
        ConnectivityInspector<String, DefaultEdge> ci = new ConnectivityInspector<>(bipartiteGraph);
        List<Set<String>> connectedSets = ci.connectedSets();

        for (Set<String> vertexSet : connectedSets) {
            UndirectedSubgraph<String, DefaultEdge> connectedComponent = new UndirectedSubgraph(bipartiteGraph, vertexSet, null);

            Pair<HashSet<String>, HashSet<String>> partitions = getLeftAndRightSets(connectedComponent);
            System.out.println("Connected Component Size : " + connectedComponent.vertexSet().size()
                + ", edges : " + connectedComponent.edgeSet().size());

            // Hopcroft matching
            long time = System.currentTimeMillis();
            HopcroftKarpBipartiteMatching<String, DefaultEdge> alg
                = new HopcroftKarpBipartiteMatching<String, DefaultEdge>(bipartiteGraph, partitions.fst, partitions.snd);
            Set<DefaultEdge> hopcroft = alg.getMatching();
            System.out.println("Hopcroft Matching done in  " + (System.currentTimeMillis() - time) / 1000
                + " seconds matching size : " + hopcroft.size());

            // Khosla Matching
            KhoslaMatchingBipartiteGraph lsa = null;
            HashSet<String> left = partitions.fst;
            HashSet<String> right = partitions.snd;
            int looplimit = 0;

            if (left.size() > right.size()) {
                lsa = new KhoslaMatchingBipartiteGraph(connectedComponent, left, right);
                looplimit = right.size();
            } else {
                lsa = new KhoslaMatchingBipartiteGraph(bipartiteGraph, right, left);
                lsa.LEFT_PRIMARY = false;
                looplimit = left.size();
            }

            time = System.currentTimeMillis();
            lsa.run(looplimit);
            Set<DefaultEdge> khosla = lsa.getMatching();
            System.out.println("Khosla Matching done in  " + (System.currentTimeMillis() - time) / 1000
                + " seconds matching size : " + khosla.size() + " , Loop Limit : " + looplimit);

            if (khosla.size() != hopcroft.size()) {
                //   lsa.checkAugmentingPath();
                System.out.println("Size Mismatch..." + (-khosla.size() + hopcroft.size()));
            }
        }
    }

    private static Pair<HashSet<String>, HashSet<String>> getLeftAndRightSets(UndirectedSubgraph connectedComponent) {
        HashSet<String> leftSet = new HashSet<>();
        HashSet<String> rightSet = new HashSet<>();

        Set<DefaultEdge> edgeSet = connectedComponent.edgeSet();
        for (DefaultEdge edge : edgeSet) {
            leftSet.add((String) connectedComponent.getEdgeSource(edge));
            rightSet.add((String) connectedComponent.getEdgeTarget(edge));
        }

        return new Pair(leftSet, rightSet);
    }

    private static void compareWithVaryingLoopLimits(UndirectedGraph<String, DefaultEdge> bipartiteGraph, HashSet<String> left, HashSet<String> right) {

        long time;
        
        Set<DefaultEdge> khosla = null;
        int[] loopLimits = {1, 2, 4, 5, 8, 10, 50, 100, 1000, 10000, 100000, right.size()};
        //int[] loopLimits = {right.size()};
        for (int loopLimit : loopLimits) {
            KhoslaMatchingBipartiteGraph lsa = null;

            if (left.size() > right.size()) {
                lsa = new KhoslaMatchingBipartiteGraph(bipartiteGraph, left, right);
            } else {
                lsa = new KhoslaMatchingBipartiteGraph(bipartiteGraph, right, left);
                lsa.LEFT_PRIMARY = false;
            }

            //run lsa with run bounds
            time = System.currentTimeMillis();
            lsa.run(loopLimit);
            khosla = lsa.getMatching();
            System.out.println("Khosla Matching done in  " + (System.currentTimeMillis() - time) / 1000
                + " seconds matching size : " + khosla.size() + " , Loop Limit : " + loopLimit);

            //lsa.checkAugmentingPath();
        }

        time = System.currentTimeMillis();
        HopcroftKarpBipartiteMatching<String, DefaultEdge> alg
            = new HopcroftKarpBipartiteMatching<String, DefaultEdge>(bipartiteGraph, left, right);
        Set<DefaultEdge> hopcroft = alg.getMatching();
        System.out.println("Hopcroft Matching done in  " + (System.currentTimeMillis() - time) / 1000
            + " seconds matching size : " + hopcroft.size());
    }

}
