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

    //Allocation Mapping
    HashMap<Integer, Integer> allocation;

    private final UndirectedGraph<String, DefaultEdge> inputGraph;

    private final String EMPTY_CHOICE = "-1";

    private static HashSet<String> rightSet = new HashSet<>();
    
    private static HashSet<String> leftSet = new HashSet<>();
    
    private static Set<DefaultEdge> matchedEdges = new HashSet<DefaultEdge>();
        
    public LSAJgraphT(UndirectedGraph<String, DefaultEdge> graph) {
        allocation = new HashMap<>();

        this.inputGraph = graph;
    }

    public static void main(String[] args) throws IOException {

        String graphFile = (args.length > 0) ? args[0] : "/Users/avishekanand/research/data/delicious/deli-wiki.tsv";

        GraphLoaderToJGraphT graphConstructor = new GraphLoaderToJGraphT();
        BufferedReader br = new BufferedReader(new FileReader(graphFile));

        long time = System.currentTimeMillis();
        UndirectedGraph<String, DefaultEdge> bipartiteGraph = graphConstructor.constructBipartiteUndirectedUnweightedGraph(br, 2, 4, leftSet, rightSet);
        log.log(Level.INFO, "Bipartite graph constructed in " + (System.currentTimeMillis() - time) / 1000 + " seconds  ");

        
        LSAJgraphT lsa = new LSAJgraphT(bipartiteGraph);

        //run lsa with run bounds
        time = System.currentTimeMillis();
        lsa.run(10);
        System.out.println("Khosla Matching done in  " + (System.currentTimeMillis() - time) / 1000 + " seconds matching size : " + lsa.getMatching().size());
        
        time = System.currentTimeMillis();
        HopcroftKarpBipartiteMatching<String, DefaultEdge> alg
                = new HopcroftKarpBipartiteMatching<String, DefaultEdge>(bipartiteGraph, leftSet, rightSet);
        Set<DefaultEdge> match = alg.getMatching();
        System.out.println("Hopcroft Matching done in  " + (System.currentTimeMillis() - time) / 1000 + " seconds matching size : " + match.size());

        
    }

    private void run(int support) {

        //initialize labels 
        HashMap<String, Integer> labels = new HashMap<>();

        //initialize mappings
        HashMap<String, String> matching = new HashMap<>();

        for (String currentNode : leftSet) {
            Set<String> choices = neighborsOfNode(currentNode);

            //determine the choice with the lowest label value
            Pair<String, String> topChoices = getBestCandidates(choices, labels);

            String bestChoice = topChoices.fst;
            String nextBestChoice = (topChoices.snd != null) ? topChoices.snd : EMPTY_CHOICE;

            //iterate until the bestchoice is unoccupied
            while (isOccupied(bestChoice, matching)) {
                //Update matching and evict already matched item
                currentNode = updateMatching(currentNode, bestChoice, matching);

                //update labels 
                updateLabels(bestChoice, nextBestChoice, labels);

                //re-evaluate the choices for the evicted item
                choices = neighborsOfNode(currentNode);
                topChoices = getBestCandidates(choices, labels);

                bestChoice = topChoices.fst;
                nextBestChoice = topChoices.snd;
            }
            //finally insert the current node into the empty best choice
            currentNode = updateMatching(currentNode, bestChoice, matching);
            updateLabels(bestChoice, nextBestChoice, labels);
        }

        //construct matching
        constructMatching(matching);
    }
    
    public Set<DefaultEdge> getMatching(){
        return matchedEdges;
    }

    private Pair<String, String> getBestCandidates(Set<String> choices, HashMap<String, Integer> labels) {
        if (choices.size() == 1) {
            return new Pair(choices.toArray()[0], null);
        }

        int bestlabel = Integer.MAX_VALUE;
        int nextBestlabel = Integer.MAX_VALUE;

        String bestChoice = "F";
        String nextBestChoice = "S";

        //System.out.print("Finding best choice from : ");
        for (String choice : choices) {
            int label;
            if (!labels.containsKey(choice)) {
                labels.put(choice, 0);
            }
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

    private String updateMatching(String currentNode, String location, HashMap<String, String> matching) {
        String evictedItem;
        if (!matching.containsKey(location)) {

        }
        evictedItem = matching.get(location);

        matching.put(location, currentNode);

        return evictedItem;
    }

    private void updateLabels(String bestChoice, String nextBestChoice, HashMap<String, Integer> labels) {
        int nextBestLabel = (nextBestChoice.equals(EMPTY_CHOICE)) ? Integer.MAX_VALUE : labels.get(nextBestChoice);

        labels.put(bestChoice, nextBestLabel + 1);
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

}
