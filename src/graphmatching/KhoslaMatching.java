package graphmatching;

import com.sun.tools.javac.util.Pair;
import input.io.GraphLoaderToJGraphT;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author avishekanand
 */
public class KhoslaMatching {

    private static final Logger log = Logger.getLogger(GraphLoaderToJGraphT.class.getName());

    private final UndirectedGraph<String, DefaultEdge> inputGraph;

    private HashSet<String> rightSet;

    private HashSet<String> leftSet;

    private Set<DefaultEdge> matchedEdges;

    public boolean LEFT_PRIMARY = true;

    private HashMap<String, Integer> labels;

    //private 
    public KhoslaMatching(UndirectedGraph<String, DefaultEdge> graph, HashSet<String> left, HashSet<String> right) {
        this.inputGraph = graph;
        leftSet = left;
        rightSet = right;
        matchedEdges = new HashSet<>();
    }

    public Set<DefaultEdge> run(int loopLimit) {

        ///Track Discarded edges
        HashSet<DefaultEdge> discarded = new HashSet<>();
        //initialize labels 
        labels = new HashMap<>();

        for (String bucket : rightSet) {
            labels.put(bucket, 0);
        }

        //initialize mappings
        HashMap<String, String> matching = new HashMap<>();

        for (String currentNode : leftSet) {
            
            if (currentNode.equals("module")) {
                System.out.println("");
            }
            if (matching.containsKey(currentNode)) {
                continue;
            }

            //Set<String> choices = neighborsOfNode(currentNode);
            Set<String> choices = undirectedNeighborsOfNode(currentNode);

            //determine the choice with the lowest label value
            Pair<String, String> topChoices = getBestCandidates(choices, labels);

            String bestLocationChoice = topChoices.fst;

            //String nextBestLocationChoice = (topChoices.snd != null) ? topChoices.snd : EMPTY_CHOICE;
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
                //choices = neighborsOfNode(currentNode);
                choices = undirectedNeighborsOfNode(currentNode);
                topChoices = getBestCandidates(choices, labels);

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

        }

        //construct matching
        constructMatching(matching);

//        return matchedEdges;
        return discarded;
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

    /**
     * Assumes undirected edge semantics
     *
     * @param currentNode
     * @return
     */
    private HashSet<String> neighborsOfNode(String currentNode) {

        HashSet<String> choices = new HashSet<>();

        //Construct a set of neighbors
        for (DefaultEdge edge : inputGraph.edgesOf(currentNode)) {
            if (LEFT_PRIMARY) {
                choices.add(inputGraph.getEdgeTarget(edge));
            } else {
                choices.add(inputGraph.getEdgeSource(edge));
            }

        }
        return choices;
    }

    private HashSet<String> undirectedNeighborsOfNode(String currentNode) {

        HashSet<String> choices = new HashSet<>();

        //Construct a set of neighbors
        for (DefaultEdge edge : inputGraph.edgesOf(currentNode)) {
            String nbr = inputGraph.getEdgeTarget(edge);
            nbr = nbr.equals(currentNode) ? inputGraph.getEdgeSource(edge) : nbr;
            choices.add(nbr);
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

    public UndirectedGraph<String, DefaultEdge> getInputGraph() {
        return inputGraph;
    }

    public HashSet<String> getRightSet() {
        return rightSet;
    }

    public HashSet<String> getLeftSet() {
        return leftSet;
    }

    public void checkAugmentingPath() {
        Set<DefaultEdge> matching = getMatching();

        HashMap<String, String> fwdMatching = new HashMap<>();
        HashMap<String, String> invMatching = new HashMap<>();

        //initialized matching
        HashSet<String> boundVertices = new HashSet<>();
        for (DefaultEdge edge : matching) {
            String edgeSource = inputGraph.getEdgeSource(edge);
            String edgeTarget = inputGraph.getEdgeTarget(edge);

            boundVertices.add(edgeSource);
            boundVertices.add(edgeTarget);

            fwdMatching.put(edgeSource, edgeTarget);
            invMatching.put(edgeTarget, edgeSource);
        }

        HashSet<String> nodesWithfreeVertices = new HashSet<>();

        //find bound vertices which are connected with free vertices
        for (String candidate : boundVertices) {
            HashSet<String> neighbors = undirectedNeighborsOfNode(candidate);
            neighbors.removeAll(boundVertices);

            if (!neighbors.isEmpty()) {
                nodesWithfreeVertices.add(candidate);
            }
        }

        int cnt = 0;
        for (String nodeWithFreeVertex : nodesWithfreeVertices) {
//            ArrayList<String> path = doBFS(nodeWithFreeVertex, fwdMatching, 
//                                        invMatching, boundVertices, nodesWithfreeVertices);

            String partner = (fwdMatching.containsKey(nodeWithFreeVertex))
                ? fwdMatching.get(nodeWithFreeVertex)
                : invMatching.get(nodeWithFreeVertex);

            LinkedList<String> dfsPath = new LinkedList<>();
            dfsPath.add(nodeWithFreeVertex);
            
            if (nodeWithFreeVertex.equals("2")) {
                System.out.println("");
            }

            boolean hasAugPath = doDFS(partner, dfsPath, boundVertices, nodesWithfreeVertices, 
                                                        fwdMatching, invMatching, true);
            if (hasAugPath) {
                printAugmentingPath(dfsPath, boundVertices, fwdMatching, invMatching);
                cnt++;
            }
        }
        System.out.println("Has " + cnt + " augmenting paths..");
    }

    private boolean doDFS(String current, LinkedList<String> dfsPath,
        HashSet<String> boundVertices,
        HashSet<String> nodesWithfreeVertices,
        HashMap<String, String> fwdMatching,
        HashMap<String, String> invMatching,
        boolean outgoing) {
        dfsPath.add(current);

        //base case
        if (nodesWithfreeVertices.contains(current) && outgoing) {
            return true;
        }

        // in case of an incoming bound node choose the matched counterpart
        if (!outgoing) {
            String cand = fwdMatching.containsKey(current) ? fwdMatching.get(current)
                : invMatching.get(current);
            
            //flip switch
            outgoing = outgoing ? false : true;
            return doDFS(cand, dfsPath, boundVertices, nodesWithfreeVertices, fwdMatching, invMatching,
                true);
        } else {
            HashSet<String> candidates = undirectedNeighborsOfNode(current);
            candidates.removeAll(dfsPath);
            //candidates.retainAll(boundVertices);

            //flip switch
            outgoing = outgoing ? false : true;

            for (String candidate : candidates) {
                boolean hasAugPath = doDFS(candidate, dfsPath, boundVertices, nodesWithfreeVertices,
                    fwdMatching, invMatching, outgoing);
                if (hasAugPath) {
                    return true;
                } else {
                    dfsPath.remove(candidate);
                }
            }
        }
        return false;
    }

    private void printAugmentingPath(LinkedList<String> dfsPath, HashSet<String> boundVertices, HashMap<String, String> fwdMatching, HashMap<String, String> invMatching) {
        
        if ((dfsPath.size() + 2) > 6) {
            return ;
        }
        
        System.out.println("Augmenting Path of path length : " + (dfsPath.size() + 2));
        String freeVertex = "";
        
        System.out.println("\t Free Vertex --> ");
        for (String node : dfsPath) {
            String printnode = node;
            if (labels.containsKey(node)) {
                printnode = node + " (" + labels.get(node) + ") ";
            }
            
            if (fwdMatching.containsKey(node)) {
                printnode = printnode + " <==> " + fwdMatching.get(node);
            } else if (invMatching.containsKey(node)) {
                printnode = printnode + " <==> " + invMatching.get(node);
            }
            System.out.println("\t" + printnode);
            freeVertex = node;
        }

        HashSet<String> freecandidates = undirectedNeighborsOfNode(freeVertex);
        freecandidates.removeAll(boundVertices);

        for (String freecandidate : freecandidates) {
            freeVertex = freecandidate;
            if (labels.containsKey(freecandidate)) {
                freeVertex = freeVertex + "FV ( " + labels.get(freecandidate) + ")";
            }
        }

        System.out.print(freeVertex);
        System.out.println("");
    }
}
