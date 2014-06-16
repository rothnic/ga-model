
import com.phoenix_int.aserver.*;
import com.phoenix_int.aserver.types.*;

/**
 * This is an example of how to implement a driver component on the
 * Analysis Server. This component uses a simple stepping algorithm
 * to perform a simple optimization case.
 *
 * @author Woyak, 8/99
 */
public class Minimizer implements IPHXDriver
{
   private PHXReference valueToSet = new PHXReference();
   private PHXReference valueToMinimize = new PHXReference();

   private double       initialStepSize = 1;
   private double       tolerance = 0.01;
   private int          maxIterations = 50;
   private int          iterations;

   private double       lastY;
   private double       step;

   public Minimizer()
   {
   }

   // inputs
   public void setValueToMinimize( PHXReference v ) { valueToMinimize = v; }
   public PHXReference getValueToMinimize() { return valueToMinimize; }
   public void setInitialStepSize( double v ) { initialStepSize = v; }
   public double getInitialStepSize() { return initialStepSize; }
   public void setMaxIterations( int v ) { maxIterations = v; }
   public int getMaxIterations() { return maxIterations; }
   public void setTolerance( double v ) { tolerance = v; }
   public double getTolerance() { return tolerance; }

   // outputs
   public PHXReference getValueToSet() { return valueToSet; }
   public int getIterations() { return iterations; }
   public double getStep() { return step; }

   /**
    * this function is called before we begin iterating with the driver
    */
   public void initializeIterations()
   {
      // reset the iteration count
      iterations = 0;

      // reset the step size
      step = initialStepSize;

      // and record the starting value for Y
      lastY = valueToMinimize.getValue();
   }

   /**
    * this function is called at the beginning of each iteration. The
    * component should set values for the case it wants the client
    * to run.
    */
   public void startIteration() throws Exception
   {
      // increment the iteration cound
      iterations++;

      // verify that we haven't exceeded the maximum number of iterations
      if ( iterations > maxIterations )
      {
         throw new Exception( "Max iterations exceeded" );
      }

      // set the value for the case we want to run - the current value
      // plus some step increment
      double X = valueToSet.getValue() + step;
      valueToSet.setValue( X );
   }

   /**
    * this function is called after the client application (ModelCenter)
    * has had a chance to run the case. At this point, this component
    * will have updated values for all of it's input variables
    */
   public boolean endIteration()
   {
      double Y = valueToMinimize.getValue();

      // if the difference between the last value and the current value
      // is less than the tolerance, we've converged
      if ( Math.abs( lastY-Y ) < tolerance )
      {
         // we're done
         return false;
      }

      // if the new value is greater than the previous value, start stepping
      // in the previous direction
      if ( Y > lastY )
      {
         step *= -0.5;
      }

      // record the current value for comparison at the next iteration
      lastY = Y;

      // try another iteration
      return true;
   }

   public void end()
   {
   }

   public static String getAuthor() { return "Phoenix Integration"; }
   public static String getVersion() { return "DEMO"; }
   public static String getDescription() { return "example driver component"; }
   public static String getHelpURL() { return "www.phoenix-int.com"; }
}
