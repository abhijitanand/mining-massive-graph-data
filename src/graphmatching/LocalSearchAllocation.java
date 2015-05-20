package graphmatching;

import com.sun.tools.javac.util.Pair;
import input.BipartiteGraph;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author avishekanand
 */
public class LocalSearchAllocation {
    //Allocation Mapping
    HashMap<Integer,Integer> allocation;
    
    private final BipartiteGraph inputGraph;
    
    private final int EMPTY_CHOICE = -1;
    
    public LocalSearchAllocation(BipartiteGraph graph) {
        allocation = new HashMap<>();
        
        this.inputGraph = graph;
    }
    
    public static void main(String[] args) throws IOException {
        BipartiteGraph bipartiteGraph = new BipartiteGraph("/Users/avishekanand/research/proposals/graphlog/data/bip-sample-15.dat");
        
        bipartiteGraph.printGraph();
        
        LocalSearchAllocation lsa = new LocalSearchAllocation(bipartiteGraph);
        
        //run lsa with run bounds
        lsa.run(10);
    }

    private void run(int support) {
        //input nodes
        HashMap<Integer, ArrayList<Integer>> input = inputGraph.getHashBasedAdjacencyList();
        
        //initialize labels 
        HashMap<Integer, Integer> labels = new HashMap<>();
       
        //initialize mappings
        HashMap<Integer, Integer> matching = new HashMap<>();
        
        for (int currentNode : input.keySet()) {
            ArrayList<Integer> choices = input.get(currentNode);
            
            //determine the choice with the lowest label value
            Pair<Integer, Integer> topChoices = getBestCandidates(choices,labels);
            
            int bestChoice = topChoices.fst; 
            int nextBestChoice = (topChoices.snd != null) ? topChoices.snd : EMPTY_CHOICE;
            
            //iterate until the bestchoice is unoccupied
            while(!isOccupied(bestChoice, matching)){
                //Update matching and evict already matched item
                currentNode = updateMatching(currentNode, bestChoice, matching);
                
                //update labels 
                updateLabels(bestChoice, nextBestChoice, labels);
                
                //re-evaluate the choices for the evicted item
                choices = input.get(currentNode);
                topChoices = getBestCandidates(choices,labels);
            
                bestChoice = topChoices.fst; 
                nextBestChoice = topChoices.snd;
            }
            //finally insert the current node into the empty best choice
            currentNode = updateMatching(currentNode, bestChoice, matching);
            updateLabels(bestChoice, nextBestChoice, labels);
        }
        
    }

    private Pair<Integer, Integer> getBestCandidates(ArrayList<Integer> choices, HashMap<Integer, Integer> labels) {
        if (choices.size() == 1) {
            return new Pair(choices.get(0), null);
        }
        
        int bestlabel = Integer.MAX_VALUE;
        int nextBestlabel = Integer.MAX_VALUE;
        
        int bestChoice = -1;
        int nextBestChoice = -1;
        
        System.out.print("Finding best choice from : ");
        for (int choice : choices) {
            int label;
            if (!labels.containsKey(choice)) {
                labels.put(choice, 0);
            }
            label = labels.get(choice);
            
            System.out.print(choice + "(" + label + ")");
            if (label < bestlabel) {
                bestlabel = label;
                bestChoice = choice;
            } else if(label < nextBestlabel){
                nextBestlabel = label;
                nextBestChoice = choice;
            }
        }
        System.out.println("Chose : " + bestChoice + + nextBestChoice);
            return new Pair(bestChoice,nextBestChoice);
    }

    private boolean isOccupied(int bestChoice, HashMap<Integer, Integer> matching) {
        return matching.containsKey(bestChoice) ? true : false;
    }

    private int updateMatching(int currentNode, int location, HashMap<Integer, Integer> matching) {
        int evictedItem;
        if (!matching.containsKey(location)) {
            
        }
        evictedItem = matching.get(location);
        
        matching.put(location, currentNode);
        
        return evictedItem;
    }

    private void updateLabels(int bestChoice, int nextBestChoice, HashMap<Integer, Integer> labels) {
        int nextBestLabel = (nextBestChoice == EMPTY_CHOICE) ? Integer.MAX_VALUE : labels.get(nextBestChoice);
        
        labels.put(bestChoice, nextBestLabel+1);
    }
}
