import ilog.concert.*;
import ilog.cplex.*;
import java.util.Map;
import java.util.TreeMap;

public class ProblemLexicographic {

    private long ID;
    private long level;
    private static Map<String, IloAddable> constraints;
    private static Map<String, IloNumExpr> objectives;
    private IloAddable localObjective;
    private Map<String, IloAddable> localConstraints;
    private static Map<String, IloNumVar> vars;
    private static IloCplex cplex;
    private static IloModel model;
    private Map<String, Double> relsol;
    private Map<String, Double> pareto;
    private static Map<String, Double> val;
    public static double eps = 10e-8;
    private static boolean consider_matrix = false;


    public ProblemLexicographic(long ID,double [][] objectives,double[][][] matrixO, double [][] matrixA, double[] b, boolean[] binary, boolean consider_matrix) throws IloException {
        this.basicFilling(objectives, matrixO, matrixA, b, binary);

        this.ID = ID;
        this.consider_matrix = consider_matrix;
    }

    public ProblemLexicographic(long ID, ProblemLexicographic p,Map<String, IloAddable> newconstraints) throws IloException {
        this.ID = ID;
        this.level = p.level + 1;
        Map<String , IloAddable> empty = new TreeMap<>();
        for(String s : p.localConstraints.keySet()){
            empty.put(s, p.localConstraints.get(s));
        }
        for(String conname: newconstraints.keySet()) {
            IloAddable con = newconstraints.get(conname);
            empty.put(conname, con);
        }
        this.localConstraints = empty;

    }

    public ProblemLexicographic(long ID, ProblemLexicographic p,String conname, IloAddable con) throws IloException {
        this.ID = ID;
        this.level = p.level + 1;
        Map<String , IloAddable> empty = new TreeMap<>();
        for(String s : p.localConstraints.keySet()){
            empty.put(s, p.localConstraints.get(s));
        }

        empty.put(conname, con);

        this.localConstraints = empty;

    }

    private void basicFilling(double [][] objectives,double[][][] matrixO, double [][] matrixA, double[] b, boolean[] binary) throws IloException{
        this.cplex = new IloCplex();
        this.vars  = new TreeMap<>();
        this.createVariable(objectives[0].length, binary);
        this.constraints = new TreeMap<>();
        this.createConstraints(matrixA, b);
        this.objectives = new TreeMap<>();
        this.val = new TreeMap<>();
        this.createObjectives(objectives, matrixO);
        this.model = this.cplex.getModel();
        this.localConstraints = new TreeMap<>();
        this.level = 0;





    }

    private void createVariable(int n, boolean[] binary) throws IloException{
        for(int i = 0; i < n; i++){
            String s = "x"+(i+1);
            if(binary[i]){
                this.vars.put(s, this.cplex.boolVar());
            }else {
                this.vars.put(s, this.cplex.intVar(Integer.MIN_VALUE, Integer.MAX_VALUE));
            }
        }
    }

    private void createConstraints(double [][] matrixA, double[] b) throws IloException{
        for(int i = 0; i < matrixA.length; i++){
            IloLinearNumExpr expr = this.cplex.linearNumExpr();

            int j = 0;
            for(String s : this.vars.keySet()){
                expr.addTerm(matrixA[i][j], this.vars.get(s));
                j++;
            }
            IloAddable con = this.cplex.addGe(expr, b[i]);
            String s = "con"+i;
            this.constraints.put(s, con);
        }
    }

    private void createObjectives(double [][] objectives, double[][][] matrixO) throws IloException{
        for(int i = 0; i < objectives.length; i++){
            double minpos = 10e200;
            // double maxneg = -1e200;
            IloNumExpr expr = this.cplex.numExpr();
            //System.out.println();
            int j = 0;
            for(String s : this.vars.keySet()){
                IloNumExpr ex0 = this.cplex.prod(objectives[i][j], this.vars.get(s));
                expr = this.cplex.sum(expr,ex0);
                //System.out.print(objectives[i][j]+" ");
                if(Math.abs(objectives[i][j]) > ProblemLexicographic.eps && minpos > Math.abs(objectives[i][j])){
                    minpos = Math.abs(objectives[i][j]);
                }
                j++;
            }

            if(this.consider_matrix) {
                IloNumExpr qexpr = this.cplex.numExpr();
                j = 0;
                for (String s : this.vars.keySet()) {
                    int k = 0;
                    for (String r : this.vars.keySet()) {
                        IloNumExpr ex1 = this.cplex.prod(this.vars.get(r), this.vars.get(s));
                        IloNumExpr ex0 = this.cplex.sum(matrixO[i][j][k], ex1);

                        qexpr = this.cplex.sum(qexpr, ex0);

                        k++;
                    }
                    j++;
                }

                IloNumExpr[] vec = new IloNumExpr[2];
                vec[0] = expr;
                vec[1] = qexpr;

                IloNumExpr expression = this.cplex.sum(vec);
                minpos = 1;

                String s = "objective"+i;
                //System.out.println(expression);
                this.objectives.put(s, expression);
                this.val.put(s, minpos);
            }else{
                IloNumExpr expression = expr;
                minpos = 1;

                String s = "objective"+i;
                //System.out.println(expression);
                this.objectives.put(s, expression);
                this.val.put(s, minpos);
            }


        }
    }

    private void setObjective(int i) throws IloException{
            this.model.remove(this.localObjective);
            this.localObjective = this.cplex.addMinimize(this.objectives.get("objective"+i));
    }


    private void solverSettings() throws IloException{
        this.cplex.setOut(null);
        this.cplex.setParam(IloCplex.IntParam.AdvInd, 0);
    }

    public boolean solve() throws IloException{
        for(IloAddable con : this.localConstraints.values()){
            this.model.add(con);
        }


        int n = this.objectives.keySet().size();
        IloAddable [] constraintsPool = new IloAddable[n];
        this.solverSettings();
        for( int i = 0; i< n ; i++) {
            this.setObjective(i);
            boolean solvable = this.cplex.solve();
            if (!solvable) {
                return false;
            }

            if(i < n-1) {
                double d = this.cplex.getObjValue();
                IloLinearNumExpr expr = this.cplex.linearNumExpr();
                constraintsPool[i] = this.cplex.addLe(expr, d);
            }
        }
        this.relsol = new TreeMap<>();
        this.pareto = new TreeMap<>();

        for(String y : this.vars.keySet()){
            this.relsol.put(y, this.cplex.getValue(this.vars.get(y)));
        }
        for(String y : this.objectives.keySet()){
            this.pareto.put(y, this.cplex.getValue(this.objectives.get(y)));
        }
        this.model.remove(constraintsPool);
        return true;
    }

    public void refresh() throws IloException{
        this.model.remove(this.localObjective);
        for(IloAddable con : this.localConstraints.values()){
            this.model.remove(con);
        }
    }

    public Map<String, IloAddable> branchOn() throws IloException{
        Map<String, IloAddable> ret = new TreeMap<>();
            double relval = this.pareto.get("objective"+0);
            double val = this.val.get("objective"+0);
            //System.out.println("val:"+val);
            IloAddable con = this.cplex.addGe(this.objectives.get("objective"+0), relval + val);

            ret.put("objective"+0 + this.ID, con);
            this.model.remove(con);

        return ret;
    }

    public Map<String, Double> getRelsol() {
        return relsol;
    }

    public Map<String, Double> getPareto() {
        return pareto;
    }


//todo: devi togliere prima o poi la funzione obiettivo
}
