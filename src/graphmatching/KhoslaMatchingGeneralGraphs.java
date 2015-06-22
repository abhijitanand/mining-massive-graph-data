package graphmatching;

import com.sun.tools.javac.util.Pair;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author avishekanand
 */
public class KhoslaMatchingGeneralGraphs {
    private final UndirectedGraph<String, DefaultEdge> inputGraph;
    private final Set<DefaultEdge> matchedEdges;
    
    public KhoslaMatchingGeneralGraphs(UndirectedGraph<String, DefaultEdge> graph) {
        this.inputGraph = graph;
        
        matchedEdges = new HashSet<>();
        
    }
    
     public Set<DefaultEdge> run(int loopLimit) {
         //initialize matching
        HashMap<String, String> matching = new HashMap<>();
         
        ///Track Discarded edges
        HashSet<DefaultEdge> discarded = new HashSet<>(); 
         
        //initialize bins
        HashMap<String, Integer> labels = new HashMap<>();
         for (String vertex : inputGraph.vertexSet()) {
             labels.put(vertex, 0);
         }
         
        //initialize used bins
        HashSet<String> usedBins = new HashSet<>();
        
        //initialize used balls
        HashSet<String> usedBalls = new HashSet<>();
        
         //process all vertices
         for (String currentNode : inputGraph.vertexSet()) {
             //Skip vertex if its used as a bin
             if (usedBins.contains(currentNode)) {
                 continue;
             }
             
            // get valid choices i.e. all choices which have not been used as balls 
            // in other words have been in matchings as balls
             //Set<String> choices = neighborsOfNode(currentNode);
            Set<String> choices = undirectedNeighborsOfNode(currentNode,usedBalls);

            //determine the choice with the lowest label value
            Pair<String, String> topChoices = getBestCandidates(choices, labels);

            String bestLocationChoice = topChoices.fst;
            String nextBestLocationChoice = topChoices.snd;
            
            //If the best choice > n-1, then we discard the item
            if (bestLocationChoice == null || labels.get(bestLocationChoice) >= loopLimit) {
                DefaultEdge edge = inputGraph.getEdge(currentNode, bestLocationChoice);
                discarded.add(edge);
                continue;
            }
            
            //iterate until the bestchoice is unoccupied
            while (isOccupied(bestLocationChoice, matching)) {
                //Update matching and evict already matched item
                currentNode = updateMatching(currentNode, bestLocationChoice, matching);
                
                //update labels 
                updateLabels(bestLocationChoice, nextBestLocationChoice, labels, loopLimit);

                //re-evaluate the choices for the evicted item
                topChoices = getBestCandidates(undirectedNeighborsOfNode(currentNode, usedBalls), labels);

                bestLocationChoice = topChoices.fst;
                nextBestLocationChoice = topChoices.snd;

                //If the best choice > n-1, then we discard the item
                if (bestLocationChoice == null || labels.get(bestLocationChoice) >= loopLimit) {
                    discarded.add(inputGraph.getEdge(currentNode, bestLocationChoice));
                    break;
                }
            }

            //If the best choice > n-1, then we discard the item
            if (bestLocationChoice == null || labels.get(bestLocationChoice) >= loopLimit) {
                continue;
            }

            //finally insert the current node into the empty best choice
            updateMatching(currentNode, bestLocationChoice, matching);
            updateLabels(bestLocationChoice, nextBestLocationChoice, labels, loopLimit);
            
            usedBalls.add(currentNode);
            usedBins.add(bestLocationChoice);
            labels.remove(currentNode);
         }
         
          //construct matching
        constructMatching(matching);

         
         return matchedEdges;
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
            ? looplimit - 1 : labels.get(nextBestLocationChoice);

        labels.put(bestLocationChoice, nextBestLabel + 1);
    }
    
    private HashSet<String> undirectedNeighborsOfNode(String currentNode, HashSet<String> usedBalls) {

        HashSet<String> choices = new HashSet<>();

        //Construct a set of neighbors
        for (DefaultEdge edge : inputGraph.edgesOf(currentNode)) {
            String nbr = inputGraph.getEdgeTarget(edge);
            nbr = nbr.equals(currentNode) ? inputGraph.getEdgeSource(edge) : nbr;
            
            if (usedBalls.contains(nbr)) {
                continue;
            }
            choices.add(nbr);
        }
        return choices;
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
                nextBestlabel = bestlabel;
                nextBestChoice = bestChoice;
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

    private void constructMatching(HashMap<String, String> matching) {
        for (String source : matching.keySet()) {

            String target = matching.get(source);
            if (inputGraph.containsEdge(source, target)) {
                DefaultEdge edge = inputGraph.getEdge(source, target);
                matchedEdges.add(edge);
            }
        }
    }
    
    public Set<DefaultEdge> getMatching() {
        return matchedEdges;
    }
}
