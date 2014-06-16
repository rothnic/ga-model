
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
public class LatinHypercubeSampling implements IPHXDriver
{
   private PHXReferenceArray designVars = new PHXReferenceArray();
   private PHXReferenceArray outputs = new PHXReferenceArray();
   private double[][] values = new double[0][0];
   private double[][] samples = new double[0][0];
   private int trial = 0;
   private int numTrials = 50;
   private boolean reinitialize = true;
   private Random r = new Random();
   private int oldDimension = 0;
   private int oldNumTrials = 0;

   public LatinHypercubeSampling()
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
   public void setReinitializeSample( boolean v ) { reinitialize = v; }
   public boolean getReinitializeSample() { return reinitialize; }
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

      //
      // create the uniform Latin Hypercube Samples only if the size of the
      // input array has changed
      //
      int dimension = designVars.getLength();

      // only recompute the samples if the number of variables has changed
      if (dimension != oldDimension || numTrials!=oldNumTrials || reinitialize)
      {
	  oldDimension = dimension;
        oldNumTrials = numTrials;

	  // allocate memory for samples
	  samples = new double[numTrials][dimension];

	  // fill in the matrix with uniform stratified samples for each dimension
	  int[] perm = new int[numTrials];
	  for ( int j = 0; j < dimension; j++)
	      {
		  // reset the permutation vector
		  for ( int i = 0; i < numTrials; i++)
		      {
			  perm[i] = 0;
		      }

		  for ( int i = 0; i < numTrials; i++)
		      {
			  // randomly generate the next position
			  // (not the most elegant way, but efficiency is not really an issue here)
			  int rank = r.nextInt(numTrials-i)+1;
			  int pos;
			  for ( pos = 0 ; rank > 0; pos++)
			      {
				  if (perm[pos] == 0)
				      rank--;
			      }
			  perm[--pos] = 1;

			  // store the sample for the current stratum in position pos
			  samples[pos][j] = (i + r.nextDouble())/numTrials;
		      }
	      }
      } // end if (initialize)
   }

   /**
    * this function is called at the beginning of each iteration. The
    * component should set values for the case it wants the client
    * to run.
    */
   public void startIteration() throws Exception
   {
      for ( int i = 0; i < designVars.getLength(); i++ )
      {
         String type = designVars.getRefPropValueString( "distributionType", i );
         double parm1 = designVars.getRefPropValueDouble( "parm1", i );
         double parm2 = designVars.getRefPropValueDouble( "parm2", i );
         double parm3 = designVars.getRefPropValueDouble( "parm3", i );

         double value;
         if ( type.equals( "Normal" ) )
            value = LHSDistribution.getNormal( parm1, parm2, samples[trial][i] );
         else if ( type.equals( "Uniform" ) )
            value = LHSDistribution.getUniform( parm1, parm2, samples[trial][i] );
         else if ( type.equals( "Triangular" ) )
            value = LHSDistribution.getTriangular( parm1, parm2, parm3, samples[trial][i] );
         else if ( type.equals( "Exponential" ) )
            value = LHSDistribution.getExponential( parm1, samples[trial][i] );
         else if ( type.equals( "Weibull" ) )
            value = LHSDistribution.getWeibull( parm1, parm2, samples[trial][i] );
         else
         {
            String msg = "unsupported distribution type: " + type;
            throw new IllegalArgumentException( msg );
         }

         designVars.getValues()[i] = value;
      }

      // increment the trial number
      trial++;   }

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

   public static String getAuthor() { return "Phoenix Integration -- modified by Chris Paredis"; }
   public static String getVersion() { return "DEMO"; }
   public static String getDescription() { return "Performs Stratified Lating Hypercube Sampling"; }
   public static String getHelpURL() { return "www.srl.gatech.edu/Eductation/ME8813"; }
}

class LHSDistribution
{
   public static double getNormal( double avg, double std, double uniformSample )
   {
      double z = -Math.sqrt(2)*erfcinv(2*uniformSample);
      return avg + z*std;
   }

   public static double getUniform( double low, double high, double uniformSample )
   {
      return low + (high-low)*uniformSample;
   }

   public static double getTriangular( double low, double peak, double high, double uniformSample )
   {
      double d = (peak-low)/(high-low);

      if ( uniformSample < d )
         return low + Math.sqrt(uniformSample*(peak-low)*(high-low));
      else
         return high - Math.sqrt((1-uniformSample)*(high-peak)*(high-low));
   }

   public static double getExponential( double mean, double uniformSample )
   {
      return -mean*Math.log(1-uniformSample);
   }

   public static double getWeibull( double theta, double beta, double uniformSample )
   {
      return theta*Math.pow(-Math.log(1-uniformSample),1/beta);
   }

   private static double erfcinv( double y )
   {
      // implements the inverse complementary error function
      // code adapted from Matlab

      double x = 0; // the output

      // Coefficients in rational approximations
      double a[] = {1.370600482778535e-02, -3.051415712357203e-01,
                     1.524304069216834e+00, -3.057303267970988e+00,
                     2.710410832036097e+00, -8.862269264526915e-01};
      double b[] = {-5.319931523264068e-02,  6.311946752267222e-01,
                     -2.432796560310728e+00,  4.175081992982483e+00,
                     -3.320170388221430e+00};
      double c[] = {5.504751339936943e-03,  2.279687217114118e-01,
                     1.697592457770869e+00,  1.802933168781950e+00,
                     -3.093354679843504e+00, -2.077595676404383e+00};
      double d[] = {7.784695709041462e-03,  3.224671290700398e-01,
                     2.445134137142996e+00,  3.754408661907416e+00};

      // Define break-points.
      double ylow  = 0.0485;
      double yhigh = 1.9515;

      // Rational approximation for central region
      if (ylow <= y && y <= yhigh)
      {
         double q = y-1;
         double r = q*q;
         x = (((((a[0]*r+a[1])*r+a[2])*r+a[3])*r+a[4])*r+a[5])*q/
             (((((b[0]*r+b[1])*r+b[2])*r+b[3])*r+b[4])*r+1);
      }

      // Rational approximation for lower region
      else if (0.0 < y && y < ylow)
      {
         double q  = Math.sqrt(-2*Math.log(y/2));
         x = (((((c[0]*q+c[1])*q+c[2])*q+c[3])*q+c[4])*q+c[5])/
              ((((d[0]*q+d[1])*q+d[2])*q+d[3])*q+1);
      }

      // Rational approximation for upper region
      else if (yhigh < y & y < 2)
      {
         double q  = Math.sqrt(-2*Math.log(1-y/2));
         x = -(((((c[0]*q+c[1])*q+c[2])*q+c[3])*q+c[4])*q+c[5])/
               ((((d[0]*q+d[1])*q+d[2])*q+d[3])*q+1);
      }
      else
	  {
	      // should throw some error here... fix later.
	      x = 0;
	  }
      return x;
   }
}
