
package input.io;

import java.net.*;
import java.util.List;

import org.jgrapht.*;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.*;


public final class HelloJGraphT
{
    

    private HelloJGraphT()
    {
    } // ensure non-instantiability.

   
    /**
     * The starting point for the demo.
     *
     * @param args ignored.
     */
    public static void main(String [] args) throws MalformedURLException
    {
        UndirectedGraph<String, DefaultEdge> stringGraph = createStringGraph();

        // note undirected edges are printed as: {<v1>,<v2>}
        System.out.println(stringGraph.toString());

        // create a graph based on URL objects
        DirectedGraph<URL, DefaultEdge> hrefGraph = createHrefGraph();

        // note directed edges are printed as: (<v1>,<v2>)
        System.out.println(hrefGraph.toString());
        
        URL amazon = new URL("http://www.amazon.com");
        URL ebay = new URL("http://www.ebay.com");
        
        BellmanFordShortestPath<URL, DefaultEdge> bsp = new BellmanFordShortestPath<>(hrefGraph, amazon);
        
        double cost = bsp.getCost(ebay);
        List<DefaultEdge> pathEdgeList = bsp.getPathEdgeList(ebay);
        
        System.out.println("BFSP Cost : " + cost + " , path : " + pathEdgeList);
        
        //KShortestPaths<URL, DefaultEdge> ksp = new KShortestPathshrefGraph, amazon);
                
                
    }

    /**
     * Creates a toy directed graph based on URL objects that represents link
     * structure.
     *
     * @return a graph based on URL objects.
     */
    private static DirectedGraph<URL, DefaultEdge> createHrefGraph()
    {
        DirectedGraph<URL, DefaultEdge> g =
            new DefaultDirectedGraph<URL, DefaultEdge>(DefaultEdge.class);

        try {
            URL amazon = new URL("http://www.amazon.com");
            URL yahoo = new URL("http://www.yahoo.com");
            URL ebay = new URL("http://www.ebay.com");
            URL google = new URL("http://www.google.com");

            // add the vertices
            g.addVertex(amazon);
            g.addVertex(yahoo);
            g.addVertex(ebay);
            g.addVertex(google);

            // add edges to create linking structure
            g.addEdge(amazon, yahoo);
            g.addEdge(yahoo, ebay);
            g.addEdge(yahoo, google);
            g.addEdge(google, ebay);
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return g;
    }

    /**
     * Craete a toy graph based on String objects.
     *
     * @return a graph based on String objects.
     */
    private static UndirectedGraph<String, DefaultEdge> createStringGraph()
    {
        UndirectedGraph<String, DefaultEdge> g =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

        String v1 = "v1";
        String v2 = "v2";
        String v3 = "v3";
        String v4 = "v4";

        // add the vertices
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);

        // add edges to create a circuit
        g.addEdge(v1, v2);
        g.addEdge(v2, v3);
        g.addEdge(v3, v4);
        g.addEdge(v4, v1);

        return g;
    }
}

// End HelloJGraphT.java