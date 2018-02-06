import ilog.concert.IloException;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.*;
import java.util.ArrayList;
import java.util.Arrays;
import com.csvreader.*;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class main {
    public static double eps = 10e-8;
    public static void main (String [] args){
        String type = args[0];
        String inputfile = args[1];
        String inputname = args[2];
        String outputfolder = args[3];
        boolean consider_matrix = Boolean.parseBoolean(args[4]);
        int nstat = Integer.parseInt(args[5]);







        //boolean allbinaries =  (Boolean) parameters.get(n);
        //boolean consider_matrix = nonlinear;


        /*
        int nrun = 100;
        int nstat = 4;
        int nupperruns = 10;
        int nvarsvariation = 4;
        int statsnumber = 2;
        */

        try {
            main.upperRuns(nstat, inputfile, type,inputname, outputfolder, consider_matrix);
        }catch (IOException e){
            System.out.println("ERROR something is wrong with the Input/Output");
            e.printStackTrace();
        }

    }

    private static ArrayList<Object> readFromFile( String type, String inputfile)throws FileNotFoundException{
        if( type.equalsIgnoreCase("2dkp")){
            return read2DKP(inputfile);
        }else if (type.equalsIgnoreCase("ap")){
            return readAP(inputfile);
        }
       return readStandardInputFile( inputfile);
    }

    private static ArrayList<Object> read(String inputfile) throws FileNotFoundException{
        ArrayList<Object> parameters = new ArrayList<>();
        Scanner fileIn = new Scanner(new File(inputfile)).useDelimiter("\\s* \\s*");;
        do {
            ArrayList<Integer> singleinput = new ArrayList<>();
            while (fileIn.hasNext()) {
                singleinput.add( fileIn.nextInt());
            }
            parameters.add(singleinput);
        }while (fileIn.hasNextLine());
        fileIn.close();

        return parameters;
    }
    private static ArrayList<Object> read2DKP(String inputfile) throws FileNotFoundException{
        ArrayList<Object> parameters = read(inputfile);
        int n = parameters.size();
        int binaryvariables = ((ArrayList<Integer>) parameters.get(0)).get(0);
        int [] b = new int [2];
        b[0] = ((ArrayList<Integer>) parameters.get(1)).get(0);
        b[1] = ((ArrayList<Integer>) parameters.get(2)).get(0);
        ArrayList<Integer> obj1 = (ArrayList<Integer>) parameters.get(3);
        ArrayList<Integer> obj2 = (ArrayList<Integer>) parameters.get(4);
        ArrayList<Integer> con1 = (ArrayList<Integer>) parameters.get(5);
        ArrayList<Integer> con2 = (ArrayList<Integer>) parameters.get(6);
        int [][] c = new int [2][n];
        int [][] A = new int [2][n];
        for(int i = 0; i <n ; i++){
            c[0][i] = obj1.get(i);
            c[1][i] = obj2.get(i);
            A[0][i] = con1.get(i);
            A[1][i] = con2.get(i);
        }

        parameters.add(n);
        parameters.add(c);
        parameters.add(A);
        parameters.add(b);
        parameters.add(-1); // allbinaries


        return parameters;
    }

    private static ArrayList<Object> readAP(String inputfile)throws FileNotFoundException{
        ArrayList<Object> parameters = new ArrayList<>();
        return parameters;
    }

    private static ArrayList<Object> readStandardInputFile(String inputfile)throws FileNotFoundException{
        ArrayList<Object> parameters = new ArrayList<>();
        return parameters;
    }

    public static void upperRuns(int nstat, String type,String inputfile,
                                 String inputname,
                                 String outputfolder,
                                 boolean consider_matrix) throws IOException{
        //double[][] stats = new double[statsnumber*nstat][nupperruns*nvarsvariation];
        String output = outputfolder + File.separator+inputfile+"_"+"Stats.csv";

        //for(int h = 1; h < nvarsvariation+1; h++) {
            //for(int k = 0; k < nupperruns; k++) {

                //double[][] res = new double[nstat][nrun];


                String outputkh = outputfolder + File.separator+"SingleRun_"+inputname+"_Results.csv";
                CsvWriter outputWriterkh = new CsvWriter(new FileWriter(outputkh, false), ',');
                outputWriterkh.write("FPA_time");
                outputWriterkh.write("MAV_time");
                outputWriterkh.write("Check");
                outputWriterkh.write("FPA_total_points");
                outputWriterkh.write("MAV_total_points");
                outputWriterkh.write("FPA_total_nodes");
                outputWriterkh.write("MAV_total_nodes");
                outputWriterkh.endRecord();
                    //System.out.println("UpperRun: "+k+"  Variables: "+(h*10)+"  Iter:" + i);
                    long[] ret = main.run(nstat,consider_matrix, type, inputfile);
                    for (int j = 0; j < nstat; j++) {
                        outputWriterkh.write(ret[j]+"");
                    }
                    outputWriterkh.endRecord();
                outputWriterkh.close();
    }

    public static long[] run( int nstat, boolean consider_matrix, String type, String inputfile){
        long [] ret = new long [nstat];
        //ArrayList<Object> param = main.RANDOMINSIDEABOX(allbinaries, nsize);

        ArrayList<Object> param = new ArrayList<>();
        try{
            param = readFromFile(type, inputfile);
        }catch( FileNotFoundException f){
            f.printStackTrace();
            return ret;
        }

        int n = (Integer) param.get(0);
        double [][] objectives =  (double[][]) param.get(1);
        double [][] matrixA = (double[][]) param.get(2);
        double [] b = (double[]) param.get(3);
        //double [][][] matrixO = (consider_matrix ? (double[][][]) param.get(5) : new double[n][n][n]);


        boolean [] binary = new boolean[n];
        int binaries = (Integer) param.get(4);
        boolean allbinaries = false;

        if( binaries < 0){
            for( int i = 0; i < n; i++) binary[i] = true;
            allbinaries = true;
        }else{
            for( int i = 0; i< binaries ; i++) binary[i] = true;
        }


        try{
            CleverFrontierAlg alg = new CleverFrontierAlg(norm.Random_Weights);
            long init = System.currentTimeMillis();
            List<Object> res = alg.solveLinear(objectives, matrixA, b, binary, consider_matrix);
            List<Map<String,Double>> Y = (List<Map<String,Double>>)res.get(0);
            long totnodes =(int) res.get(1);
            long end = System.currentTimeMillis();

            /*LexicographicAlgorithm lalg = new LexicographicAlgorithm();
            long linit = System.currentTimeMillis();
            List<Object> lres = lalg.solve(objectives,matrixO, matrixA, b, binary, consider_matrix);
            List<Map<String,Double>> lY = (List<Map<String,Double>>) lres.get(0);
            long ltotnodes =(int) lres.get(1);
            long lend = System.currentTimeMillis();*/


            boolean printbool = true;
            CleverFrontierAlg.printFrontierCsv(Y,printbool);
           // LexicographicAlgorithm.printFrontierCsv(lY,printbool);

            long start = 0, finish = 0;
            if(allbinaries) {
                MavrotasAlgorithm malg = new MavrotasAlgorithm();
                start = System.currentTimeMillis();
                List<Object> resM = malg.solveLinear(objectives,
                        matrixA, b, binary, consider_matrix);
                List<Map<String, Double>> MY = ( List<Map<String, Double>>) resM.get(0);
                long totnodesM = (int) resM.get(1);
                finish = System.currentTimeMillis();
                MavrotasAlgorithm.printFrontierCsv(MY,printbool);
                if(nstat>=3) {
                    ret[2] = (AlgUtils.compare(Y, MY) ? 1 : 0);
                }
                if( nstat >= 4){
                    ret[3] = Y.size();
                }
                if( nstat >= 5){
                    ret[4] = MY.size();
                }
                if( nstat >= 6){
                    ret[5] =  totnodes;
                }
                if( nstat >= 7){
                    ret[6] =  totnodesM;
                }

            }else {

                if (nstat >= 4) {
                    ret[3] = Y.size();
                }
                if (nstat >= 6) {
                    ret[5] = totnodes;
                }
            }


            ret[0] = end - init;
            ret[1] = finish - start;
            //System.out.println("Total time Partitioner(s): "+(end-init));
            //System.out.println("Total time Mavrotas   (s): "+(finish-start));
        }catch(IloException e){
            e.printStackTrace();
        }
        return ret;
    }

    public static ArrayList<Object> RANDOMINSIDEABOX(boolean allbinaries, int nsize){
        int m = 2;
        int n = nsize;//nsize;// (int) Math.round(Math.random()*200);
        int p = 5*n;//(int) Math.round(Math.random()*200);
        int ord = 100;
        //System.out.println(m+" "+n+" "+p);


        double [] [] matrix = new double [p+2*n] [n];
        double [] b = new double [p+2*n];
        double [][] o = new double [m][n];
        double [][][] matrixO = main.generateRandomPositiveSemidefiniteMatrix(m,n,2*n,ord);
        boolean [] binary = new boolean [n];

        for(int i = 0; i<p; i++){
            for(int j = 0; j<n; j++)
                matrix[i][j] = Math.round(Math.random()*21-1);
        }
        for(int i = 0; i<p; i++){
            b[i] =Math.round(10 - Math.random()*5);
        }

        double val =Math.round(1*L1NormBounder(matrix, b));

        for(int i = p; i<(p+2*n); i++){
            for(int j = 0; j<n; j++)
                if(i==p+j) {
                    matrix[i][j] = -1;
                }else if( i == p+n+j){
                    matrix[i][j] = 1;
                }
        }
        for(int i = p; i<(p+2*n); i++){
            if(i<p+n) {
                b[i] = -val;
            }else{
                b[i] = -val;
            }
        }
        //////////////////////////////////////





        for(int i = 0; i<m; i++){
            for(int j = 0; j<n; j++)
                o[i][j] = Math.round(Math.random()*ord);//-50;
        }



        if(allbinaries){
           for(int i = 0 ; i < n ; i++){
               binary[i] = true;
           }
        }else {
            int count = 0;
            loop:
            while (true) {
                for (int i = 0; i < n; i++) {
                    if (Math.random() > 0.5d && !binary[i]) {
                        binary[i] = true;
                        count++;
                    }
                    if (count >= n / 2) {
                        break loop;
                    }

                }
            }
        }

        ArrayList<Object> ret = new ArrayList<>();
        ret.add(o);
        ret.add(matrix);
        ret.add(b);
        ret.add(binary);
        ret.add(matrixO);


        return ret;
    }


    public static double L1NormBounder( double[][] a, double [] b){
        double ret = 10e-200;
        for(int i = 0; i<a.length;i++){
            if(Math.abs(b[i]) > eps) {
                double maxb = b[i];
                for (int j = 0; j < a[0].length; j++) {
                    if (Math.abs(a[i][j]) > eps) {
                        double maxa = a[i][j];
                        double val = Math.abs(maxb/maxa);
                        if( val > ret){
                            ret = val;
                        }
                    }
                }
            }
        }
        return ret;
    }
    public static void printMatrix(String name, double[][] a){
        System.out.println("Matrix Printer-- Matrix Name: "+name);
        for(int i = 0; i<a.length;i++){
            for(int j = 0; j<a[0].length; j++){
                System.out.print(a[i][j]+" ");
            }
            System.out.println();
        }
    }
    public static void printMatrix(String name, double[] a){
        System.out.println("Matrix Printer-- Matrix Name: "+name);
        for(int i = 0; i<a.length;i++){
            System.out.println(a[i]+", ");
        }
    }

    public static ArrayList<Object> BOX(){
        int m = 2;
        int n = 3;// (int) Math.round(Math.random()*200);
        int p = 2*n;//(int) Math.round(Math.random()*200);
        System.out.println(m+" "+n+" "+p);


        double [] [] matrix = new double [p] [n];
        double [] b = new double [p];
        double [][] o = new double [m][n];

        for(int i = 0; i<p; i++){
            for(int j = 0; j<n; j++)
                if(i==j) {
                    matrix[i][j] = -1;
                }else if( i == n+j){
                    matrix[i][j] = 1;
                }
        }
        for(int i = 0; i<p; i++){
            if(i<n) {
                b[i] = -1;
            }else{
                b[i] = 2;
            }
        }
        for(int i = 0; i<m; i++){
            for(int j = 0; j<n; j++) {
                o[0][j] = 1;
                o[1][0] = -1;
                o[1][1] = 0;
                o[1][2] = 0;
            }
        }

        ArrayList<Object> ret = new ArrayList<>();
        ret.add(o);
        ret.add(matrix);
        ret.add(b);
        return ret;
    }


    public static double [][][] generateRandomPositiveSemidefiniteMatrix(int a, int b, int c, int ord){
        double[][][] ret = new double[a][b][c];
        double[][][] piv = new double[a][b][b];


        for(int i = 0; i< a ; i++){
            for(int h = 0; h < b; h++){
                for(int k = 0; k < c; k++){
                    ret[i][h][k] = Math.round(Math.random()*ord)-ord/2;
                }
            }
        }

        for(int i = 0; i< a ; i++){
            for(int h = 0; h < b; h++){
                for(int k = 0; k < b; k++){
                    piv[i][h][k] = main.vectorialProd(ret[i][h], ret[i][k]);
                }
            }
        }


        return piv;
    }

    public static double vectorialProd(double [] a, double [] b){
        int n = a.length;
        if( n != b.length){
            return Double.NaN;
        }

        double ret = 0;

        for(int i = 0; i< n ; i ++){
            ret += a[i]*b[i];
        }
        return ret;
    }
}
