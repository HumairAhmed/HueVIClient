package com.humairahmed.esx.hueviclient;

/* Lead Developer:   Humair Ahmed 
 * 
 * Contributors:
 *                   Lisa Huang - Added event listener functionality to update application when changes are
 *                                made either through the Java client or on the ESXi server itself
 *                              - Moved prior ESX code to MangedVirtualMachine to better organize/implement code
 *                                and some additions
 *                              - Helped isolate and fix minor bugs
 *                            
 * Program:  HueVIClient
 * Version:  2.0
 * Date:     10/27/2011
 * Website:  http://www.HumairAhmed.com
 * 
 * Notes: OVF Import/Export functionality currently taken out to address some bugs - will be added in future release
 * 
 * 
 * License:
 * 
 * Open source software being distributed under GPL license. For more information see here:
 * http://www.gnu.org/copyleft/gpl.html. 
 * 
 * Can edit and redistribute code as long as above reference of authorship is kept within the code.
 * 
 */

import com.vmware.vim25.mo.HttpNfcLease;



public class LeaseProgressUpdater extends Thread
{
	private HttpNfcLease httpNfcLease = null;
	private int progressPercent = 0;
	private int updateInterval;

	public LeaseProgressUpdater(HttpNfcLease httpNfcLease, int updateInterval) 
	{
		this.httpNfcLease = httpNfcLease;
		this.updateInterval = updateInterval;
	}

	public void run() 
	{
		while (true) 
		{
			try 
			{
				httpNfcLease.httpNfcLeaseProgress(progressPercent);
				Thread.sleep(updateInterval);
			}
			catch(InterruptedException ie)
			{
				break;
			}
			catch(Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	public void setPercent(int percent)
	{
		this.progressPercent = percent;
	}
}