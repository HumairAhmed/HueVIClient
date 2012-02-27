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

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.vmware.vim25.Description;
import com.vmware.vim25.FileFault;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.NotSupported;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineFileInfo;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualSCSISharing;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class ESX 
{
	private int vmCount;     //Virtual Machine Count on ESX Server
	private int hostCount;   //Host Server count
	private ArrayList<VirtualMachine> vms;
	private ArrayList<HostSystem> hosts;


	//One instance per ESX Server/VirtualCenter Server - starting point for all managed objects
	private ServiceInstance si;    

	private ManagedEntity[] mes;  //holds Virtual Machine objects 
	private ManagedEntity[] mesHost; //holds System


	public ESX() 
	{
		vms = new ArrayList<VirtualMachine>();
		hosts = new ArrayList<HostSystem>();
	}


	//Connects to an ESXi Server - returns boolean 'true' if successful otherwise returns 'false'
	public boolean connect(String ip, String username, String password) throws RemoteException, MalformedURLException
	{    	
		//try connecting to ESX server using supplied authentication data
		this.si = new ServiceInstance(new URL("https://" + ip + "/sdk"), username, password, true);
		Folder rootFolder = si.getRootFolder(); //needed to get all virtual machines and hosts

		//get all virtual machine and host objects
		mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		mesHost = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");			

		if (si != null)
		{
			return true;
		}
		
		return false;
    }
	
	
	
	public ServiceInstance getServiceInstance()
	{
		return si;
	}



	//Disconnects from the ESXi server
	public void disconnect()
	{
		si.getServerConnection().logout();
	}


	//method returns an ArrayList of Virtual machines
	public ArrayList<VirtualMachine> getVirtualMachines() throws InvalidProperty, RuntimeFault, RemoteException
	{
		vms.clear();
		mes = new InventoryNavigator(si.getRootFolder()).searchManagedEntities("VirtualMachine");

		if(mes!= null && mes.length !=0)
		{
			this.vmCount = mes.length; //get and store virtual machine count
				
			//add all virtual machine names to the Virtual Machine drop down box
			for(int i = 0; i < vmCount; i++)
			{
				VirtualMachine vm = (VirtualMachine) mes[i]; 
					
				vms.add(vm); //add VM object to ArrayList
			}
			
		}
		return vms;
	}
	
	
	
	//method returns an ArrayList of Hosts
	public ArrayList<HostSystem> getHosts() throws InvalidProperty, RuntimeFault, RemoteException
	{
		hosts.clear();
		
		mesHost = new InventoryNavigator(si.getRootFolder()).searchManagedEntities("HostSystem");
		
		if(mesHost!= null && mesHost.length !=0)
		{
			this.hostCount = mesHost.length; //get and store virtual machine count
				
			//add all host names to the hosts array list
			for(int i = 0; i < hostCount; i++)
			{
				HostSystem hs = (HostSystem) mesHost[i]; 
					
				hosts.add(hs); //add host object to ArrayList
			}
			
		}
		return hosts;
	}


	//method not supported with ESX - will work only with vCenter - uncomment below lines if using vCenter
	public boolean cloneVM(ManagedVirtualMachine mvm, String clonedVMName) throws InvalidProperty, RuntimeFault, RemoteException 
	{
		VirtualMachine vm = mvm.getVM();
		String cloneName = clonedVMName;

		if(vm == null)
		{
			return false;
		}

		VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
		cloneSpec.setLocation(new VirtualMachineRelocateSpec());
		cloneSpec.setPowerOn(false);
		cloneSpec.setTemplate(false);

		Task task = vm.cloneVM_Task((Folder) vm.getParent(), cloneName, cloneSpec);

		//uncomment this code if running with vCenter
		//	    String status = task.waitForMe();

		//	    if(status != Task.SUCCESS)
		//	    {
		//	    	return false;
		//	    }

		//	    vmCount++;  
		return true;
	}	




	//Powers on the virtual machine
	public boolean createVM(String vmName, String os, int memory, int storage, int numberOfProcessors, String datastore, String vmNetwork, String vmNic) throws Exception 
	{		
		Folder rootFolder  = si.getRootFolder();
		String dcname = "ha-datacenter";

		String virtualMachineName = vmName;
		long memorySizeMB = memory;
		int cupCount = numberOfProcessors;
		String guestOsId = os; 
		long diskSizeKB = storage;
		String diskMode = "persistent";
		String datastoreName = datastore;
		String netName = vmNetwork;
		String nicName = vmNic;

		Datacenter dc = (Datacenter) new InventoryNavigator(
				rootFolder).searchManagedEntity("Datacenter", dcname);

		ResourcePool rp = (ResourcePool) new InventoryNavigator(
				dc).searchManagedEntities("ResourcePool")[0];

		Folder vmFolder = dc.getVmFolder();

		// create vm config spec
		VirtualMachineConfigSpec vmSpec = new VirtualMachineConfigSpec();
		vmSpec.setName(virtualMachineName);
		vmSpec.setAnnotation("VirtualMachine Annotation");
		vmSpec.setMemoryMB(memorySizeMB);
		vmSpec.setNumCPUs(cupCount);
		vmSpec.setGuestId(guestOsId);


		// create virtual devices
		int cKey = 1000;

		VirtualDeviceConfigSpec scsiSpec = createScsiSpec(cKey);
		VirtualDeviceConfigSpec diskSpec = createDiskSpec(
				datastoreName, cKey, diskSizeKB, diskMode);
		VirtualDeviceConfigSpec nicSpec = createNicSpec(netName, nicName);
		vmSpec.setDeviceChange(new VirtualDeviceConfigSpec[]{scsiSpec, diskSpec, nicSpec});

		// create file info for the vmx file
		VirtualMachineFileInfo vmfi = new VirtualMachineFileInfo();
		vmfi.setVmPathName("["+ datastoreName +"]");
		vmSpec.setFiles(vmfi);

		// call the createVM_Task method on the vm folder
		Task task = vmFolder.createVM_Task(vmSpec, rp, null);
		String result = task.waitForMe();

		if(result != Task.SUCCESS)
		{
			return false;
		}

		vms.removeAll(vms);

		//get all virtual machine objects
		mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");

		//if at least one virtual machine exists
		if(mes!= null && mes.length !=0)
		{
			this.vmCount = mes.length; //get and store virtual machine count

			//add all virtual machine names to the Virtual Machine drop down box
			for(int i = 0; i < vmCount; i++)
			{
				VirtualMachine vm = (VirtualMachine) mes[i]; 

				vms.add(vm); //add VM object to ArrayList
			}	
		}

		vmCount++;
		return true;
	}
	
	
	//overloaded method for specifying # of VMs to create
	public boolean createVM(String vmName, String os, int memory, int storage, int numberOfProcessors, String datastore, String vmNetwork, String vmNic,int n) throws Exception 
	{		
		Folder rootFolder  = si.getRootFolder();
		String dcname = "ha-datacenter";

		long memorySizeMB = memory;
		int cupCount = numberOfProcessors;
		String guestOsId = os; 
		long diskSizeKB = storage;
		String diskMode = "persistent";
		String datastoreName = datastore;
		String netName = vmNetwork;
		String nicName = vmNic;

		for(int j=1;j<=n;j++) {

			String virtualMachineName = vmName + j;

			Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", dcname);

			ResourcePool rp = (ResourcePool) new InventoryNavigator(dc).searchManagedEntities("ResourcePool")[0];

			Folder vmFolder = dc.getVmFolder();

			// create vm config spec
			VirtualMachineConfigSpec vmSpec = new VirtualMachineConfigSpec();
			vmSpec.setName(virtualMachineName);
			vmSpec.setAnnotation("VirtualMachine Annotation" + j);
			vmSpec.setMemoryMB(memorySizeMB);
			vmSpec.setNumCPUs(cupCount);
			vmSpec.setGuestId(guestOsId);


			// create virtual devices
			int cKey = 1000;

			VirtualDeviceConfigSpec scsiSpec = createScsiSpec(cKey);
			VirtualDeviceConfigSpec diskSpec = createDiskSpec(
					datastoreName, cKey, diskSizeKB, diskMode);
			VirtualDeviceConfigSpec nicSpec = createNicSpec(netName, nicName);
			vmSpec.setDeviceChange(new VirtualDeviceConfigSpec[]{scsiSpec, diskSpec, nicSpec});

			// create file info for the vmx file
			VirtualMachineFileInfo vmfi = new VirtualMachineFileInfo();
			vmfi.setVmPathName("["+ datastoreName +"]");
			vmSpec.setFiles(vmfi);

			// call the createVM_Task method on the vm folder
			Task task = vmFolder.createVM_Task(vmSpec, rp, null);
			String result = task.waitForMe();

			if(result != Task.SUCCESS)
			{
				return false;
			}
		}//end of for
		vms.removeAll(vms);

		//get all virtual machine objects
		mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");

		//if at least one virtual machine exists
		if(mes!= null && mes.length !=0)
		{
			this.vmCount = mes.length; //get and store virtual machine count

			//add all virtual machine names to the Virtual Machine drop down box
			for(int i = 0; i < vmCount; i++)
			{
				VirtualMachine vm = (VirtualMachine) mes[i]; 

				vms.add(vm); //add VM object to ArrayList
			}	
		}

		vmCount++;

		return true;
	}
	
	
	static VirtualDeviceConfigSpec createScsiSpec(int cKey)
	{
		VirtualDeviceConfigSpec scsiSpec = new VirtualDeviceConfigSpec();

		scsiSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
		VirtualLsiLogicController scsiCtrl = new VirtualLsiLogicController();

		scsiCtrl.setKey(cKey);
		scsiCtrl.setBusNumber(0);
		scsiCtrl.setSharedBus(VirtualSCSISharing.noSharing);
		scsiSpec.setDevice(scsiCtrl);

		return scsiSpec;
	}
	
	

	static VirtualDeviceConfigSpec createDiskSpec(String dsName,
			int cKey, long diskSizeKB, String diskMode)
	{
		VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();
		diskSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
		diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.create);

		VirtualDisk vd = new VirtualDisk();
		vd.setCapacityInKB(diskSizeKB);
		diskSpec.setDevice(vd);
		vd.setKey(0);
		vd.setUnitNumber(0);
		vd.setControllerKey(cKey);
		VirtualDiskFlatVer2BackingInfo diskfileBacking = new VirtualDiskFlatVer2BackingInfo();
		String fileName = "["+ dsName +"]";
		diskfileBacking.setFileName(fileName);
		diskfileBacking.setDiskMode(diskMode);
		diskfileBacking.setThinProvisioned(true);
		vd.setBacking(diskfileBacking);

		return diskSpec;
	}


	static VirtualDeviceConfigSpec createNicSpec(String netName, String nicName) throws Exception
	{
		VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
		nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);

		VirtualEthernetCard nic = new VirtualPCNet32();
		VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();

		nicBacking.setDeviceName(netName);
		Description info = new Description();
		info.setLabel(nicName);
		info.setSummary(netName);
		nic.setDeviceInfo(info);

		// type: “generated”, “manual”, “assigned” by VC
		nic.setAddressType("generated");
		nic.setBacking(nicBacking);
		nic.setKey(0);
		nicSpec.setDevice(nic);

		return nicSpec;
	}


	//Deletes the VM from the ESXi server
	public void destroyVM(ManagedVirtualMachine mvm) throws Exception 
	{	
		mvm.getVM().destroy_Task();
		vmCount--;
	}


	//Powers on the virtual machine
	public void powerOnVM(ManagedVirtualMachine mvm) throws VmConfigFault, TaskInProgress, FileFault, InvalidState, InsufficientResourcesFault, RuntimeFault, RemoteException 
	{
		mvm.getVM().powerOnVM_Task(null);
	}


	//Powers off the virtual machine
	public void powerOffVM(ManagedVirtualMachine mvm) throws TaskInProgress, InvalidState, RuntimeFault, RemoteException
	{
		mvm.getVM().powerOffVM_Task();
	}


	//Restarts the virtual machine
	public void resetVM(ManagedVirtualMachine mvm) throws TaskInProgress, InvalidState, RuntimeFault, RemoteException
	{
		mvm.getVM().resetVM_Task();

	}

	
	//Suspends the virtual machine
	public void suspendVM(ManagedVirtualMachine mvm) throws TaskInProgress, InvalidState, RuntimeFault, RemoteException
	{
		mvm.getVM().suspendVM_Task();
	}
	
	
	public void shutdownServer() throws InvalidState, NotSupported, RuntimeFault, RemoteException
	{
		getHosts().get(0).shutdownHost_Task(true);
	}
	
	public void restartServer() throws InvalidState, NotSupported, RuntimeFault, RemoteException
	{
		getHosts().get(0).rebootHost(true);
	}
	
	public String getIpAddress() throws InvalidState, RuntimeFault, RemoteException 
	{
		return si.getAccountManager().getServerConnection().getUrl().getHost();
	}	
	
	public String getNetwork() throws InvalidState, RuntimeFault, RemoteException 
	{
		Network[] networks = getHosts().get(0).getNetworks();
		return networks[0].getName();
	}
	
	
	public String getServerModel() throws InvalidState, RuntimeFault, RemoteException 
	{
		return getHosts().get(0).queryHostConnectionInfo().getHost().getHardware().model;
	}	
	
	
	public String getCPUModel() throws InvalidState, RuntimeFault, RemoteException 
	{
		return getHosts().get(0).queryHostConnectionInfo().getHost().getHardware().getCpuModel();
	}
	
	
	public int getNumNics() throws InvalidState, RuntimeFault, RemoteException 
	{
		return getHosts().get(0).queryHostConnectionInfo().getHost().getHardware().getNumNics();
	}
	
	
	public int getNumCPUs() throws InvalidState, RuntimeFault, RemoteException 
	{
		return getHosts().get(0).queryHostConnectionInfo().getHost().getHardware().getNumCpuPkgs();
	}
	
	
	public int getNumCPUCores() throws InvalidState, RuntimeFault, RemoteException 
	{
		return getHosts().get(0).queryHostConnectionInfo().getHost().getHardware().getNumCpuCores();
	}
	
	
	public long getMemory() throws InvalidState, RuntimeFault, RemoteException 
	{
		return (long) Math.ceil(getHosts().get(0).queryHostConnectionInfo().getHost().getHardware().getMemorySize()/1048576.0/1024.0);
	}
	
	
	public double getUptime() throws InvalidState, RuntimeFault, RemoteException 
	{
		double upTime =  getHosts().get(0).retrieveHardwareUptime()/3600.00;
		
		DecimalFormat twoDec = new DecimalFormat("#.##");
	    
		return Double.valueOf(twoDec.format(upTime));
	}
	
	
//	public void exportOvf(String url, String userName, String pwd, String vAppOrVmName, String hostip, String entityType, String targetDir) throws Exception{
//		OvfExport.startExport(url, userName, pwd, vAppOrVmName, hostip, entityType, targetDir);
//	}

	
//	public void importOvf(String url, String userName, String pwd, String vAppOrVmName, String hostip, String entityType, String targetDir) throws Exception{
//		OvfImport.startImport(url, userName, pwd, vAppOrVmName, hostip, entityType, targetDir);
//	}
}
