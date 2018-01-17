import java.util.List;
import java.util.Map;

public class AlgUtils {
    public static double eps = 1e-6;
    public static boolean compare( List<Map<String, Double>> Y , List<Map<String, Double>> MY){
        int n = Y.size();
        int mn = MY.size();
        //System.out.println(n+" "+mn);
        if(mn != n){
            return false;
        }
        int nins = Y.get(0).keySet().size();

        double [][] valY = new double [n][nins];
        double [][] valMY = new double [n][nins];

        for( int i = 0; i <n ; i++){
            int j = 0;
            for(String s : Y.get(i).keySet()){
                valY[i][j] = Y.get(i).get(s).doubleValue();
                j++;
            }
        }

        for( int i = 0; i <n ; i++){
            int j = 0;
            for(String s : MY.get(i).keySet()){
                valMY[i][j] = MY.get(i).get(s).doubleValue();
                j++;
            }
        }


        for(int i = 0; i< n ; i ++){
            boolean flag = true;

            loop:
            for(int k = 0; k< n; k++){
                boolean check = true;

                insloop:
                for(int j = 0; j< nins ; j++){
                    check = check && ( Math.abs(valY[i][j] - valMY[k][j] ) <= AlgUtils.eps);
                    if(!check){
                       break insloop;
                    }
                }

                if(check){
                    flag = true;
                    break loop;
                }else{
                    flag = false;
                }
            }

            if(!flag){
                return false;
            }
        }


        return true;
    }
}
