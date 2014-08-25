/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communityMethods;

import java.util.Collection;
import configurations.Configuration;

/**
 *
 * @author joan
 */
public class Circles extends AbstractCommunityMethod
{

    public Circles(Configuration config)
    {
        super(config);
    }

    
   

	@Override
	protected double calculateMetric(Collection<Long> community)
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    
}
