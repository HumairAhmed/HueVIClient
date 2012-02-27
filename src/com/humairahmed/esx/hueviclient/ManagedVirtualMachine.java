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

import java.rmi.RemoteException;

import com.vmware.vim25.InvalidState;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.ToolsUnavailable;

import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.VirtualMachine;



public class ManagedVirtualMachine 
{
	private VirtualMachine vm;
	
	
	public ManagedVirtualMachine(VirtualMachine virtualMachine) throws Exception
	{
		if(virtualMachine != null)
			this.vm = virtualMachine;
		else throw new Exception("Null Virtual Machine. Unable to manage it.");
	}
	
	public String getName()
	{
		return vm.getConfig().getGuestFullName();
	}


	public int getMemoryMB()
	{
	    return vm.getConfig().getHardware().getMemoryMB();
	}

	
	public int getMemoryGB()
	{
	    return vm.getConfig().getHardware().getMemoryMB()/1024;
	}

	
	public Integer getGuestMemoryUsage()
	{
	    return vm.getSummary().getQuickStats().getGuestMemoryUsage();
	}

	
	public Integer getFreeMemoryMB()
	{
	    return getMemoryMB()-getGuestMemoryUsage();
	}

	
	public long getCPUUsage() throws Exception
	{
		if(VirtualMachinePowerState.poweredOn.equals(vm.getRuntime().getPowerState()))
			return vm.getSummary().getQuickStats().getOverallCpuUsage().longValue()/vm.getConfig().getCpuAllocation().getLimit();
		throw new Exception("Invalid Virtual Machine power state. Unable to manage it.");
	}
	
	
	public int getNumCPU() throws Exception
	{
		return vm.getConfig().hardware.getNumCPU();
	}
	

	//Restarts the guest OS on the virtual machine
	public void restartGuestOnVM() throws RuntimeFault, RemoteException
	{
		vm.reload();
	}
	
	
	
	//Shuts down the guest OS on the virtual machine
	public void shutDownGuestOnVM() throws TaskInProgress, InvalidState, ToolsUnavailable, RuntimeFault, RemoteException
	{
		vm.shutdownGuest();
	}
	
	
	
	public VirtualMachine getVM()
	{
		return vm;
	}
}

