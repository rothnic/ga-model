
import com.phoenix_int.aserver.*;
import com.phoenix_int.aserver.types.*;

/**
 * This is an example of how to implement a driver component on the
 * Analysis Server. This component performs a basic Parametric Study
 * analysis
 *
 * @author Haisma, 10/99 - borrowed heavily from Monte Carlo
 */
public class ParmStudy implements IPHXDriver
{
   private PHXReference designVar = new PHXReference();
   private PHXReferenceArray responses = new PHXReferenceArray();
   private int iteration = 0;
   private long numSteps = 0;
   private double stepSize = 0;
   private double fromValue = 0;
   private double toValue = 0;
   private boolean collectResults = false;
   private PHXDoubleArray results = new PHXDoubleArray();

   public ParmStudy()
      throws Exception
   {
      designVar.createRefProp( "from", "double" );
      designVar.createRefProp( "to", "double" );
      designVar.createRefProp( "numSteps", "long" );

      results.resize(new int[] {0,0} );

      responses.setAutoGrow( true );
   }

   public PHXReference getDesignVar() { return designVar; }
   public void setResponses( PHXReferenceArray v ) { responses = v; }
   public PHXReferenceArray getResponses() { return responses; }
   public int getIteration() { return iteration; }
   public boolean getCollectResults() { return collectResults; }
   public void setCollectResults(boolean cr) { collectResults = cr; }
   public PHXDoubleArray getResults() { return results; }

   /**
    * this function is called before we begin iterating with the driver
    */
   public void initializeIterations() throws Exception
   {
      // reset the iteration count
      iteration = 0;

      // allocate arrays to store values in
      fromValue = designVar.getRefPropValueDouble( "from" );
      toValue = designVar.getRefPropValueDouble( "to" );
	   numSteps = designVar.getRefPropValueLong( "numSteps" );

	  if( numSteps < 2 )
	     numSteps = 2;
      stepSize = (toValue - fromValue)/(numSteps - 1);
      
      if ( collectResults ) 
      {
      	results.resize(new int[] {responses.getLength(), (int)numSteps} );
      }
      else
      {
      	results.resize(new int[] {responses.getLength(), 0} );
      }
	}

   /**
    * this function is called at the beginning of each iteration. The
    * component should set values for the case it wants the client
    * to run.
    */
   public void startIteration() throws Exception
   {
	  designVar.setValue( fromValue + iteration*stepSize );
   }

   /**
    * this function is called after the client application (ModelCenter)
    * has had a chance to run the case. At this point, this component
    * will have updated values for all of it's input variables
    */
   public boolean endIteration()
   {
     double[] vals = responses.getValues();
     if ( collectResults )
     {
        for ( int i = 0 ; i < responses.getLength() ; i ++ )
        {
           results.setValue(new int[] {i,iteration}, vals[i]);
        }
     }
     iteration++;
     if ( iteration >= numSteps )
        return false;
     else
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
