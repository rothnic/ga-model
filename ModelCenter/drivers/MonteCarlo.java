
import com.phoenix_int.aserver.*;
import com.phoenix_int.aserver.types.*;
import java.util.*;

/**
 * This is an example of how to implement a driver component on the
 * Analysis Server. This component performs a basic Monte Carlo 
 * analysis
 *
 * @author Woyak, 9/99
 */
public class MonteCarlo implements IPHXDriver
{
   private PHXReferenceArray designVars = new PHXReferenceArray();
   private PHXReferenceArray outputs = new PHXReferenceArray();
   private double[][] values = new double[0][0];
   private int trial = 0;
   private int numTrials = 50;

   public MonteCarlo()
   {
      IPHXRefPropInfo prop;

      // ----- design variable setup
      designVars.setAutoGrow( true );
      
      prop = designVars.createRefProp( "distributionType", "string" );
      prop.enumValuesFromString( "Normal,Uniform,Triangular,Exponential,Weibull" );
      prop.setTitle( "Distribution Type" );

      prop = designVars.createRefProp( "parm1", "double" );
      prop.setTitle( "Param #1" );
      prop.setDescription( "distribution parameter one" );

      prop = designVars.createRefProp( "parm2", "double" );
      prop.setTitle( "Param #2" );
      prop.setDescription( "distribution parameter two" );

      prop = designVars.createRefProp( "parm3", "double" );
      prop.setTitle( "Param #3" );
      prop.setDescription( "distribution parameter three" );

      // ----- output setup
      outputs.setAutoGrow( true );

      prop = outputs.createRefProp( "avg", "double" );
      prop.setInput( false );
      prop.setDescription( "Average" );
      prop.setTitle( "average" );

      prop = outputs.createRefProp( "std", "double" );
      prop.setInput( false );
      prop.setTitle( "Standard Deviation" );
      prop.setDescription( "standard deviation" );
   }

   public PHXReferenceArray getDesignVars() { return designVars; }
   public void setOutputs( PHXReferenceArray v ) { outputs = v; }
   public PHXReferenceArray getOutputs() { return outputs; }
   public void setNumTrials( int v ) { numTrials = v; }
   public int getNumTrials() { return numTrials; }
   public int getTrial() { return trial; }

   /**
    * this function is called before we begin iterating with the driver
    */
   public void initializeIterations()
   {
      // reset the run number
      trial = 0;

      // allocate the array to store values in
      values = new double[numTrials][outputs.getLength()];
   }

   /**
    * this function is called at the beginning of each iteration. The
    * component should set values for the case it wants the client
    * to run.
    */
   public void startIteration() throws Exception
   {
      // increment the trial number
      trial++;

      for ( int i = 0; i < designVars.getLength(); i++ )
      {
         String type = designVars.getRefPropValueString( "distributionType", i );
         double parm1 = designVars.getRefPropValueDouble( "parm1", i );
         double parm2 = designVars.getRefPropValueDouble( "parm2", i );
         double parm3 = designVars.getRefPropValueDouble( "parm3", i );

         double value;
         if ( type.equals( "Normal" ) )
            value = Distribution.getNormal( parm1, parm2 );
         else if ( type.equals( "Uniform" ) )
            value = Distribution.getUniform( parm1, parm2 );
         else if ( type.equals( "Triangular" ) )
            value = Distribution.getTriangular( parm1, parm2, parm3 );
         else if ( type.equals( "Exponential" ) )
            value = Distribution.getExponential( parm1 );
         else if ( type.equals( "Weibull" ) )
            value = Distribution.getWeibull( parm1, parm2 );
         else
         {
            String msg = "unsupported distribution type: " + type;
            throw new IllegalArgumentException( msg );
         }

         designVars.getValues()[i] = value;
      }
   }

   /**
    * this function is called after the client application (ModelCenter)
    * has had a chance to run the case. At this point, this component
    * will have updated values for all of it's input variables
    */
   public boolean endIteration() throws PHXTypeMismatchException
   {
      for ( int i = 0; i < outputs.getLength(); i++ )
      {
         values[trial-1][i] = outputs.getValues()[i];
      }

      computeStatistics();

      if ( trial == numTrials )
         return false;
      else
         return true;
   }

   void computeStatistics() throws PHXTypeMismatchException
   {
      for ( int var = 0; var < outputs.getLength(); var++ )
      {
         double sum = 0;
         double avg;
         double std;

         // compute the average value
         for ( int j = 0; j < trial; j++ )
         {
            sum += values[j][var];
         }
         avg = sum/trial;

         // compute the standard deviation
         sum = 0;
         for ( int j = 0; j < trial; j++ )
         {
            sum += Math.pow(values[j][var]-avg, 2);
         }

         if ( trial > 1 )
            std = Math.sqrt( (1.0/(trial-1))*sum );
         else
            std = 0;

         // load the values into the reference variables so that
         // ModelCenter can get them
         outputs.setRefPropValue( "avg", var, avg );
         outputs.setRefPropValue( "std", var, std );
      }
   }

   public void end()
   {
   }

   public static String getAuthor() { return "Phoenix Integration"; }
   public static String getVersion() { return "DEMO"; }
   public static String getDescription() { return "example driver component"; }
   public static String getHelpURL() { return "www.phoenix-int.com"; }
}

class Distribution
{
   public static double getNormal( double avg, double std )
   {
      Random r = new Random();
      double u1 = r.nextDouble();
      double u2 = r.nextDouble();
      double z = Math.sqrt( -2*Math.log(u1) )*Math.cos(2*3.1415926*u2);
      return avg + z*std;
   }

   public static double getUniform( double low, double high )
   {
      Random r = new Random();
      return low + (high-low)*r.nextDouble();
   }

   public static double getTriangular( double low, double peak, double high )
   {
      Random r = new Random();
      double d = (peak-low)/(high-low);
      double u = r.nextDouble();

      if ( u < d )
         return low + Math.sqrt(u*(peak-low)*(high-low));
      else
         return high - Math.sqrt((1-u)*(high-peak)*(high-low));
   }

   public static double getExponential( double mean )
   {
      Random r = new Random();
      return -mean*Math.log(1-r.nextDouble());
   }

   public static double getWeibull( double theta, double beta )
   {
      Random r = new Random();
      return theta*Math.pow(-Math.log(1-r.nextDouble()),1/beta);
   }
}
