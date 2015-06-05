package graphmatching;

import com.sun.tools.javac.util.Pair;
import input.io.GraphLoaderToJGraphT;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.HopcroftKarpBipartiteMatching;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author avishekanand
 */
public class LSAJgraphT {

    private static final Logger log = Logger.getLogger(GraphLoaderToJGraphT.class.getName());

    private final UndirectedGraph<String, DefaultEdge> inputGraph;

    private HashSet<String> rightSet;

    private HashSet<String> leftSet;

    private Set<DefaultEdge> matchedEdges;

    public LSAJgraphT(UndirectedGraph<String, DefaultEdge> graph, HashSet<String> left, HashSet<String> right) {
        this.inputGraph = graph;
        leftSet = left;
        rightSet = right;
        matchedEdges = new HashSet<>();
    }

    public static void main(String[] args) throws IOException {

//        String graphFile = (args.length > 0) ? args[0] 
//                        : "/Users/avishekanand/research/data/delicious/deli-wiki-head-5k.tsv";

        
        String graphFile = (args.length > 0) ? args[0] 
                        : "/Users/avishekanand/research/data/delicious/sample.tsv";
        
        GraphLoaderToJGraphT graphConstructor = new GraphLoaderToJGraphT();
        BufferedReader br = new BufferedReader(new FileReader(graphFile));

        long time = System.currentTimeMillis();

        HashSet<String> left = new HashSet<>();
        HashSet<String> right = new HashSet<>();

        UndirectedGraph<String, DefaultEdge> bipartiteGraph
            = graphConstructor.constructBipartiteUndirectedUnweightedGraph(br, 2, 4, left, right);

        int treeComponents = identifyTreeStructures(bipartiteGraph, left, right);

        log.log(Level.INFO, "Number of Tree components :  " + treeComponents + " Left set: "
                                                    + left.size() + " right set : " + right.size());
        log.log(Level.INFO, "Bipartite graph constructed in "
            + (System.currentTimeMillis() - time) / 1000 + " seconds  ");

        Set<DefaultEdge> khosla = null;
        int[] loopLimits = {1, 2, 4, 5, 8, 10, 50, 100, 1000, 10000, 100000, 1000000};
        for (int loopLimit : loopLimits) {
            LSAJgraphT lsa = new LSAJgraphT(bipartiteGraph, left, right);
            //run lsa with run bounds
            time = System.currentTimeMillis();
            lsa.run(loopLimit);
            
            khosla = lsa.getMatching();
            System.out.println("Khosla Matching done in  " + (System.currentTimeMillis() - time) / 1000
                + " seconds matching size : " + khosla.size() + " , Loop Limit : " + loopLimit);

        }

        time = System.currentTimeMillis();
        HopcroftKarpBipartiteMatching<String, DefaultEdge> alg
            = new HopcroftKarpBipartiteMatching<String, DefaultEdge>(bipartiteGraph, left, right);
        Set<DefaultEdge> hopcroft = alg.getMatching();
        System.out.println("Hopcroft Matching done in  " + (System.currentTimeMillis() - time) / 1000 
                                                            + " seconds matching size : " + hopcroft.size());

        //hopcroft.removeAll(khosla);
        
        int cnt = 0;
        for (DefaultEdge edge : hopcroft) {
            if (!khosla.contains(edge)) {
                cnt++;
                //System.out.println("\t" + bipartiteGraph.getEdgeSource(edge) + " , " + bipartiteGraph.getEdgeTarget(edge));
            }
        }
        
        System.out.println("Hopcroft - Khosla : " + cnt);
    }

    private Set<DefaultEdge> run(int loopLimit) {

        //initialize labels 
        HashMap<String, Integer> labels = new HashMap<>();

        for (String bucket : rightSet) {
            labels.put(bucket, 0);
        }

        //initialize mappings
        HashMap<String, String> matching = new HashMap<>();

        for (String currentNode : leftSet) {
            Set<String> choices = neighborsOfNode(currentNode);

            //determine the choice with the lowest label value
            Pair<String, String> topChoices = getBestCandidates(choices, labels);

            String bestLocationChoice = topChoices.fst;
            //String nextBestLocationChoice = (topChoices.snd != null) ? topChoices.snd : EMPTY_CHOICE;
            String nextBestLocationChoice = topChoices.snd;

            //If the best choice > n-1, then we discard the item
            if (bestLocationChoice == null || labels.get(bestLocationChoice) > loopLimit) {
                continue;
            }

            //iterate until the bestchoice is unoccupied
            while (isOccupied(bestLocationChoice, matching)) {
                //Update matching and evict already matched item
                currentNode = updateMatching(currentNode, bestLocationChoice, matching);

                //update labels 
                updateLabels(bestLocationChoice, nextBestLocationChoice, labels, loopLimit);

                //re-evaluate the choices for the evicted item
                choices = neighborsOfNode(currentNode);
                topChoices = getBestCandidates(choices, labels);

                bestLocationChoice = topChoices.fst;
                nextBestLocationChoice = topChoices.snd;

                //If the best choice > n-1, then we discard the item
                if (bestLocationChoice == null || labels.get(bestLocationChoice) > loopLimit) {
                    break;
                }
            }

            //If the best choice > n-1, then we discard the item
            if (bestLocationChoice == null || labels.get(bestLocationChoice) > loopLimit) {
                continue;
            }

            //finally insert the current node into the empty best choice
            updateMatching(currentNode, bestLocationChoice, matching);
            updateLabels(bestLocationChoice, nextBestLocationChoice, labels, loopLimit);
        }

        //construct matching
        constructMatching(matching);

        return matchedEdges;
    }

    public Set<DefaultEdge> getMatching() {
        return matchedEdges;
    }

    private Pair<String, String> getBestCandidates(Set<String> choices, HashMap<String, Integer> labels) {
        if (choices.size() == 1) {
            return new Pair(choices.toArray()[0], null);
        }

        int bestlabel = Integer.MAX_VALUE;
        int nextBestlabel = Integer.MAX_VALUE;

        String bestChoice = null;
        String nextBestChoice = null;

        //System.out.print("Finding best choice from : ");
        for (String choice : choices) {
            int label;
            label = labels.get(choice);

            //System.out.print(choice + "(" + label + ")");
            if (label < bestlabel) {
                bestlabel = label;
                bestChoice = choice;
            } else if (label < nextBestlabel) {
                nextBestlabel = label;
                nextBestChoice = choice;
            }
        }
        
        //System.out.println("Best Choices : " + bestChoice + ", " + nextBestChoice);
        return new Pair(bestChoice, nextBestChoice);
    }

    private boolean isOccupied(String bestChoice, HashMap<String, String> matching) {
        return matching.containsKey(bestChoice) ? true : false;
    }

    private String updateMatching(String currentNode, String bestMatchedNeighbor, HashMap<String, String> matching) {
        String evictedItem = matching.get(bestMatchedNeighbor);
        matching.put(bestMatchedNeighbor, currentNode);

        return evictedItem;
    }

    private void updateLabels(String bestLocationChoice, String nextBestLocationChoice,
                                         HashMap<String, Integer> labels, int looplimit) {
       int nextBestLabel = (nextBestLocationChoice == null)
            ? looplimit : labels.get(nextBestLocationChoice);

        labels.put(bestLocationChoice, nextBestLabel + 1);
    }

    private Set<String> neighborsOfNode(String currentNode) {

        HashSet<String> choices = new HashSet<>();

        //Construct a set of neighbors
        for (DefaultEdge edge : inputGraph.edgesOf(currentNode)) {
            choices.add(inputGraph.getEdgeTarget(edge));
        }
        return choices;
    }

    private void constructMatching(HashMap<String, String> matching) {
        for (String source : matching.keySet()) {

            String target = matching.get(source);
            if (inputGraph.containsEdge(source, target)) {
                DefaultEdge edge = inputGraph.getEdge(source, target);
                matchedEdges.add(edge);
            }
        }
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

    public UndirectedGraph<String, DefaultEdge> getInputGraph() {
        return inputGraph;
    }

    public HashSet<String> getRightSet() {
        return rightSet;
    }

    public HashSet<String> getLeftSet() {
        return leftSet;
    }

}
