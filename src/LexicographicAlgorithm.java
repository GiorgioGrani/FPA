import ilog.concert.IloException;
import ilog.concert.IloAddable;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import java.util.concurrent.*;


public class LexicographicAlgorithm {
    private long timemax = Long.MAX_VALUE;

    public LexicographicAlgorithm(){

    }

    public LexicographicAlgorithm(long timemax){
        this.timemax = timemax;
    }

    public void setTimemax(long timemax){
        this.timemax = timemax;
    }

    public List<Object> solve(double [][] objectives, double[][][] matrixO,
                              double [][] matrixA, double[] b, boolean[] binary, boolean consider_matrix) throws IloException{
        return this.solveWithoutTimeStop(objectives,matrixO, matrixA, b, binary, consider_matrix);
    }

    public List<Object> solveWithoutTimeStop(double [][] objectives,double[][][] matrixO,
                                             double [][] matrixA, double[] b, boolean [] binary, boolean consider_matrix) throws IloException{

        //initializzation
        int ID = 0;
        ProblemLexicographic root = new ProblemLexicographic(0, objectives, matrixO, matrixA, b,binary, consider_matrix);
        List<ProblemLexicographic> D = new ArrayList<>();
        D.add(root);
        List<Map<String, Double>> Y = new ArrayList<>();

        //alg
        while( D.size() > 0 ){
            ProblemLexicographic p = D.get(D.size()-1); // todo this is only depthfirst
            boolean isSolvable = p.solve();
            if( isSolvable){
                Map<String, IloAddable> pool = p.branchOn();
                Y.add(p.getPareto());
                D.remove(p);
                for(String s: pool.keySet()){
                    ID++;
                    ProblemLexicographic pk = new ProblemLexicographic(ID,p,s,pool.get(s));
                    D.add(pk);
                }

            }else{

                D.remove(p);
            }
            p.refresh();

        }

        List<Object> ret = new ArrayList<>();
        ret.add(0,Y);
        ret.add(1,(ID));

        return ret;
    }

    public static void printFrontier(List<Map<String, Double>> Y){
        int i = 1;
        System.out.println("---Optimal frontier FPA---");
        for(Map<String, Double> y : Y ){
            System.out.print("Point: "+i+"    (");
            for(String s : y.keySet()){
                System.out.print(y.get(s)+" ");
            }
            System.out.print(")\n");
            i++;
        }
    }

    public static void printFrontierCsv(List<Map<String, Double>> Y, boolean printbool){
        if(!printbool) return;
        int i = 1;
        System.out.println("---Optimal frontier Lexicographic---");
        for(Map<String, Double> y : Y ){
            for(String s : y.keySet()){
                System.out.print(y.get(s)+", ");
            }
            System.out.print("\n");
            i++;
        }
    }

}

