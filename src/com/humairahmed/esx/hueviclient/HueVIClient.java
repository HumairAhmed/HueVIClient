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
import java.rmi.RemoteException;
import java.util.ArrayList;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.JOptionPane;
import com.humairahmed.esx.hueviclient.ESX;
import com.humairahmed.esx.hueviclient.ManagedVirtualMachine;
import com.vmware.vim25.FileFault;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.NotSupported;
import com.vmware.vim25.ObjectUpdate;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.ToolsUnavailable;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.VirtualMachine;




public class HueVIClient extends JFrame implements ActionListener, VMCreateDeleteEventListener
{
	private static final long serialVersionUID = 8597898401783011167L;
	public static final String[] actionItems = {"Power On VM", "Power Off VM", "Suspend VM", "Reset VM", "Shut Down Guest", 
		"Restart Guest", "Create Windows XP (32-bit) VM", "Create Kubuntu 10.10 Ser (64-bit) VM", 
		"Clone VM", "Delete VM","Export ovf","Import ovf"};
		
	private JLabel vmLabel;
	private JLabel actionLabel;

	private JComboBox vmComboBox;
	private JComboBox actionComboBox;

	private JButton executeButton;
	private JButton getInfoButton;
	private JButton connectButton;
	private JButton disconnectButton;
		
	private JTextField ipTextField, usernameTextField;
		
	private JTextArea resultTextArea;
		
	private JPasswordField passwordPasswordField;

	private JPanel innerPanel;
	private JPanel buttonPanel;
	private JPanel innerButtonPanel;
	private JPanel connectPanel;
		
	private JMenuBar menubar;
	private JMenu fileMenu;
	private JMenu infoMenu;
	private JMenu aboutMenu;
	
	private JMenuItem quitItem;
	private JMenuItem shutdownServerItem;
	private JMenuItem restartServerItem;
	private JMenuItem networkItem;
	private JMenuItem developersItem;
	private JMenuItem serverItem;
	private JMenuItem diagnosticItem;
	
		
	private Container container;
	private FlowLayout layout;
	private BorderLayout outerLayout;
		   
		  
	//private ManagedEntity[] mes;  //holds Virtual Machine objects 
	private ArrayList<VirtualMachine> vms; //holds Virtual Machine objects 
	private ESX esx_instance;
			
		
		
	public HueVIClient()
	{
		super("ESX API Test Program"); //Frame title
		esx_instance = new ESX();
		SetupGUI(); //Create GUI
	}
		
		
	//catches all events by user and takes appropriate action
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == connectButton) 
		{
			Connect(); //connect to ESX server using supplied authentication data
		}
		else if(e.getSource() == disconnectButton)
		{
			disconnect(); //disconnect from ESX server
		}
		else if(e.getSource() == getInfoButton) //get info about the virtual machine
		{	
			VirtualMachine vm = null;
			ManagedVirtualMachine mvm = null;
			
			
			String info = null;
				
			try {
				if(!esx_instance.getVirtualMachines().isEmpty()) //if at least one virtual machine exists
				{
					//get selected virtual machine
					vm = vms.get(vmComboBox.getSelectedIndex());
					
					try {
						mvm = new ManagedVirtualMachine(vm);
					} catch (Exception e1) {
					
						e1.printStackTrace();
					}
					
					//get virtual machine info
					try 
					{
						info = "Virtual Machine: " + mvm.getName() +
							    "\n\n" + "# of CPUs Allocated: " + mvm.getNumCPU()
							    + "\n\n" + "Memory Allocation: " + mvm.getMemoryMB() + " Megabytes";
						System.out.println("info : " + info);
					} 
					catch (Exception e1) 
					{
						
						e1.printStackTrace();
					}
						
					resultTextArea.setText(info);
				}
			} catch (InvalidProperty e1) {
				
				e1.printStackTrace();
			} catch (RuntimeFault e1) {
				
				e1.printStackTrace();
			} catch (RemoteException e1) {
				
				e1.printStackTrace();
			}
		}
	    else if(e.getSource() == shutdownServerItem) //if 'Shutdown Server' menu item is selected
	    {
	    	try {
				esx_instance.shutdownServer();
			} catch (InvalidState e1) {
				
				e1.printStackTrace();
			} catch (NotSupported e1) {
				
				e1.printStackTrace();
			} catch (RuntimeFault e1) {
			
				e1.printStackTrace();
			} catch (RemoteException e1) {
			
				e1.printStackTrace();
			}
		    resultTextArea.setText("Server has been shut down.");
	    }
		else if(e.getSource() == quitItem) //if 'Quit' menu item is selected
		{
			//esx_instance.disconnect();
			resultTextArea.setText("Bye");
			System.exit(0); //terminate the program
		}
		else if(e.getSource() == developersItem) //if 'Developers' menu item is selected
		{
			String msg = "Developers: \n\nHumair Ahmed\nYi Huang\nAshaya Meshram";
		    JOptionPane optionPane = new JOptionPane();
		    optionPane.setMessage(msg);
		    optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
		    JDialog dialog = optionPane.createDialog(null, "Developers");
		    dialog.setVisible(true);
		}
		else if(e.getSource() == networkItem) //if 'ESXi Network Info' menu item is selected
		{
			String msg = null;
			try {
				msg = "Network: " + esx_instance.getNetwork() +
				"\nIP Address: " + esx_instance.getIpAddress();
								
			} catch (InvalidState e1) {
			
				e1.printStackTrace();
			} catch (RuntimeFault e1) {
			
				e1.printStackTrace();
			} catch (RemoteException e1) {
				
				e1.printStackTrace();
			}
		    JOptionPane optionPane = new JOptionPane();
		    optionPane.setMessage(msg);
		    optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
		    JDialog dialog = optionPane.createDialog(null, "Network Info");
		    dialog.setVisible(true);
		}
		else if(e.getSource() == diagnosticItem) //if 'Diagnostics' menu item is selected
		{
			String msg = null;
			try {
				msg = "Server Uptime: " + esx_instance.getUptime() + " hours";
								
			} catch (InvalidState e1) {
				
				e1.printStackTrace();
			} catch (RuntimeFault e1) {
				
				e1.printStackTrace();
			} catch (RemoteException e1) {
				
				e1.printStackTrace();
			}
		    JOptionPane optionPane = new JOptionPane();
		    optionPane.setMessage(msg);
		    optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
		    JDialog dialog = optionPane.createDialog(null, "Diagnostics");
		    dialog.setVisible(true);
		}
		else if(e.getSource() == serverItem) //if 'Server Info' menu item is selected
		{
			String msg = null;
			try {				
				msg = "Server Model: " + esx_instance.getServerModel() + "\n\n"
				+ "CPU Model: " + esx_instance.getCPUModel() +"\n"
				+ "# of CPUs: " + esx_instance.getNumCPUs() +"\n"
				+ "# of CPU cores: " + esx_instance.getNumCPUCores() +"\n\n"
				+ "Memory: " + esx_instance.getMemory() +" GB\n\n"
			    + "# of NICS: " + esx_instance.getNumNics();
				
			} catch (InvalidState e1) {
			
				e1.printStackTrace();
			} catch (RuntimeFault e1) {
				
				e1.printStackTrace();
			} catch (RemoteException e1) {
				
				e1.printStackTrace();
			}
		    JOptionPane optionPane = new JOptionPane();
		    optionPane.setMessage(msg);
		    optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
		    JDialog dialog = optionPane.createDialog(null, "Server Info");
		    dialog.setVisible(true);
		}
		else if(e.getSource() == executeButton) //execute selected command on virtual machine
		{
			VirtualMachine vm = null;
			ManagedVirtualMachine mvm = null;

			String action = null;
				
			try {
				if(!esx_instance.getVirtualMachines().isEmpty()) //if at least one virtual machine exists
				{
					//get selected virtual machine and selected action
					vm = vms.get(vmComboBox.getSelectedIndex());
					try {
						mvm = new ManagedVirtualMachine(vm);
					} catch (Exception e2) {
						
						e2.printStackTrace();
					}
					action = actionComboBox.getSelectedItem().toString();
					
					//get virtual machine name/Guest OS
					String vm_name = mvm.getName();
					
					//take appropriate action based on selection command to execute
					if(action.equalsIgnoreCase("Power On"))
					{	 
						try {
							esx_instance.powerOnVM(mvm);
						} catch (VmConfigFault e1) {
							
							e1.printStackTrace();
						} catch (TaskInProgress e1) {
							
							e1.printStackTrace();
						} catch (FileFault e1) {
							
							e1.printStackTrace();
						} catch (InvalidState e1) {
							
							e1.printStackTrace();
						} catch (InsufficientResourcesFault e1) {
							
							e1.printStackTrace();
						} catch (RuntimeFault e1) {
							
							e1.printStackTrace();
						} catch (RemoteException e1) {
							
							e1.printStackTrace();
						}
						resultTextArea.setText(vm_name + " VM powered on.");
					}
					else if(action.equalsIgnoreCase("Power Off"))
					{
						try {
							esx_instance.powerOffVM(mvm);
						} catch (TaskInProgress e1) {
							
							e1.printStackTrace();
						} catch (InvalidState e1) {
						
							e1.printStackTrace();
						} catch (RuntimeFault e1) {
							
							e1.printStackTrace();
						} catch (RemoteException e1) {
							
							e1.printStackTrace();
						}
						resultTextArea.setText(vm_name + " VM powered off.");
					}
					else if(action.equalsIgnoreCase("Suspend"))
					{
						try {
							esx_instance.suspendVM(mvm);
						} catch (TaskInProgress e1) {
							
							e1.printStackTrace();
						} catch (InvalidState e1) {
						
							e1.printStackTrace();
						} catch (RuntimeFault e1) {
							
							e1.printStackTrace();
						} catch (RemoteException e1) {
							
							e1.printStackTrace();
						}
						resultTextArea.setText(vm_name + " VM suspended.");
					}
					else if(action.equalsIgnoreCase("Reset"))
					{
						try {
							esx_instance.resetVM(mvm);
						} catch (TaskInProgress e1) {
							
							e1.printStackTrace();
						} catch (InvalidState e1) {
							
							e1.printStackTrace();
						} catch (RuntimeFault e1) {
						
							e1.printStackTrace();
						} catch (RemoteException e1) {
							
							e1.printStackTrace();
						}
						resultTextArea.setText(vm_name + " VM reset.");
					} 

					else if(action.equalsIgnoreCase("Shut Down Guest"))
					{
						try {
							mvm.shutDownGuestOnVM();
						} catch (TaskInProgress e1) {
							
							e1.printStackTrace();
						} catch (InvalidState e1) {
							
							e1.printStackTrace();
						} catch (ToolsUnavailable e1) {
						
							e1.printStackTrace();
						} catch (RuntimeFault e1) {
							
							e1.printStackTrace();
						} catch (RemoteException e1) {
							
							e1.printStackTrace();
						}
						resultTextArea.setText(vm_name + " VM guest shut down.");
					}
					else if(action.equalsIgnoreCase("Restart Guest"))
					{
						try {
							mvm.restartGuestOnVM();
						} catch (RuntimeFault e1) {
							
							e1.printStackTrace();
						} catch (RemoteException e1) {
							
							e1.printStackTrace();
						}
						resultTextArea.setText(vm_name + " VM guest restarted.");
					}
					else if( (action.equalsIgnoreCase("Create Windows XP (32-bit) VM")) || (action.equalsIgnoreCase("Create Kubuntu 10.10 Ser (64-bit) VM") ))
					{
						boolean result = false;
						String os = "";
					
						String osName =  action.equalsIgnoreCase("Create Windows XP (32-bit) VM") ? "Windows XP (32-bit)": "Kubuntu 10.10 Ser (64-bit)";
						
						if(osName.equalsIgnoreCase("Windows XP (32-bit)"))
						{
							os = "winXPProGuest";
						}
						else if(osName.equalsIgnoreCase("Kubuntu 10.10 Ser (64-bit)"))
						{
							os = "ubuntu64Guest";
						}
						String vmName = JOptionPane.showInputDialog("Enter the Virtual Machine Name");
						
						
						try 
						{
							//result = esx_instance.createVM(vmName, os, 500, 1000, 1, "datastore1", "VM Network", "vmnic0",1);
							result = esx_instance.createVM(vmName, os, 500, 1000, 1, "datastore1", "VM Network", "vmnic0");
							
						} catch (RuntimeFault e1) {
							
							e1.printStackTrace();
						} catch (RemoteException e1) {
							
							e1.printStackTrace();
						} catch (Exception e1) {
							
							e1.printStackTrace();
						}
						if(result)
						{
							resultTextArea.setText("VM guest " + os + " created!");
						}
						else
						{
							resultTextArea.setText("VM guest " + os + " could not be created!");
						}
					}
					else if(action.equalsIgnoreCase("Clone VM"))
					{
						try 
						{
						    String vmClone = JOptionPane.showInputDialog("Enter the Virtual Machine Name");

						    if(esx_instance.cloneVM(mvm, vmClone))
						    {
								resultTextArea.setText(mvm.getName() + " cloned as " + vmClone);
						    }
						    else
						    {
						    	resultTextArea.setText("Cloning failed! Errors encountered while trying to clone " + mvm.getName() + ".");
						    }
						} 
						catch (RuntimeFault e1) 
						{
							e1.printStackTrace();
						} 
						catch (RemoteException e1) 
						{
							e1.printStackTrace();
						}
					}
					else if(action.equalsIgnoreCase("Delete VM"))
					{
						String toRemovedVM = null;
						try 
						{
							toRemovedVM = mvm.getName();
							esx_instance.destroyVM(mvm);
							vmComboBox.removeItemAt(vmComboBox.getSelectedIndex());
										
						} catch (RuntimeFault e1) {
							
							e1.printStackTrace();
						} catch (RemoteException e1) {
							
							e1.printStackTrace();
						} catch (Exception e1) {
							
							e1.printStackTrace();
						}
						resultTextArea.setText("VM guest " + toRemovedVM + " deleted.");
					}
					else if(action.equalsIgnoreCase("Export ovf")) 
					{
						try 
						{
							VirtualMachine vmName = vms.get(vmComboBox.getSelectedIndex());
							
							String exportPath = JOptionPane.showInputDialog("Enter the absolute directory path to export the OVF file to: ");
							
//							esx_instance.exportOvf("https://" + ipTextField.getText() + "/sdk", usernameTextField.getText(), 
//									passwordPasswordField.getText(), vmName.getName(), ipTextField.getText(), "VirtualMachine", exportPath);
						} 
						catch (Exception e1) 
						{
							e1.printStackTrace();
						}
					}
					else if(action.equalsIgnoreCase("Import ovf")) 
					{
						try 
						{
							String importPath = JOptionPane.showInputDialog("Enter the absolute path to the OVF file to import: ");
							
//							esx_instance.importOvf("https://" + ipTextField.getText() + "/sdk", usernameTextField.getText(), 
//							passwordPasswordField.getText(), "vmFromOvf", ipTextField.getText(), "VirtualMachine", importPath);
						} 
						catch (Exception e1) 
						{
							e1.printStackTrace();
						}
					}
					
					}
			} catch (InvalidProperty e1) {
		
				e1.printStackTrace();
			} catch (RuntimeFault e1) {
				
				e1.printStackTrace();
			} catch (HeadlessException e1) {
			
				e1.printStackTrace();
			} catch (RemoteException e1) {
				
				e1.printStackTrace();
			}

			}
		}
		
		
		
		//setup GUI
		public void SetupGUI()
		{	
			layout = new FlowLayout();
			layout.setAlignment(FlowLayout.LEFT); //orientation is 'left' for the outside container
			
			outerLayout = new BorderLayout();
			container = getContentPane();
			container.setLayout(outerLayout);
			
			innerPanel = new JPanel();
			innerPanel.setLayout(layout);
			
			menubar = new JMenuBar();
			fileMenu = new JMenu("File");
			fileMenu.setMnemonic('F');
			infoMenu = new JMenu("ESXi Info");
			infoMenu.setMnemonic('E');
			aboutMenu = new JMenu("About");
			aboutMenu.setMnemonic('A');
			
			
			quitItem = new JMenuItem("Quit");
	        quitItem.setMnemonic('Q');
	        quitItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
	        quitItem.addActionListener(this);
	        
	        shutdownServerItem = new JMenuItem("Shutdown ESXi Server");
	        shutdownServerItem.setMnemonic('S');
	        shutdownServerItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
	        shutdownServerItem.addActionListener(this);
	        
	        restartServerItem = new JMenuItem("Restart ESXi Server");
	        restartServerItem.setMnemonic('R');
	        restartServerItem.setAccelerator(KeyStroke.getKeyStroke("control R"));
	        restartServerItem.addActionListener(this);
	        
			diagnosticItem = new JMenuItem("Diagnostics");
			diagnosticItem.setMnemonic('D');
			diagnosticItem.setAccelerator(KeyStroke.getKeyStroke("control D"));
			diagnosticItem.addActionListener(this);
	        
			networkItem = new JMenuItem("ESXi Network Info");
	        networkItem.setMnemonic('N');
	        networkItem.setAccelerator(KeyStroke.getKeyStroke("control P"));
	        networkItem.addActionListener(this);
	        
			serverItem = new JMenuItem("Server Info");
	        serverItem.setMnemonic('V');
	        serverItem.setAccelerator(KeyStroke.getKeyStroke("control V"));
	        serverItem.addActionListener(this);
	        
			developersItem = new JMenuItem("Developers");
			developersItem.setMnemonic('D');
			developersItem.setAccelerator(KeyStroke.getKeyStroke("control D"));
			developersItem.addActionListener(this);

			
	        menubar.add(fileMenu); 
	        fileMenu.add(restartServerItem);
	        fileMenu.add(shutdownServerItem);
	        fileMenu.add(quitItem);
	        
	        menubar.add(infoMenu);
	        infoMenu.add(networkItem);
	        infoMenu.add(serverItem);
	        infoMenu.add(diagnosticItem);
	        aboutMenu.add(developersItem);
	        
	        menubar.add(aboutMenu);
	        
			container.add(menubar, BorderLayout.PAGE_START);
			
			connectPanel = new JPanel();
			connectPanel.setLayout(new GridLayout(5,3));
				
			ipTextField = new JTextField("");
			usernameTextField = new JTextField("");
			passwordPasswordField = new JPasswordField("");
			
			connectButton = new JButton("Connect");
			disconnectButton = new JButton("Disconnect");
					
			connectButton.addActionListener(this);
			disconnectButton.addActionListener(this);		
			
			connectPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			connectPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			connectPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			connectPanel.add(new JLabel("IP Address:"));
			connectPanel.add(new JLabel("Username:"));
			connectPanel.add(new JLabel("Password:"));
			connectPanel.add(ipTextField);
			connectPanel.add(usernameTextField);
			connectPanel.add(passwordPasswordField);
			connectPanel.add(connectButton);
			connectPanel.add(disconnectButton);
			connectPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			connectPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			connectPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			
			
			container.add(connectPanel);
			
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(7,1)); //create a 5 x 1 grid for inside panel controls
			
			innerButtonPanel = new JPanel();
			innerButtonPanel.setLayout(new GridLayout(1,2));
			
			vmLabel = new JLabel("Virtual Machine: ");
			actionLabel = new JLabel("Action: ");
			
			vmComboBox = new JComboBox();
			actionComboBox = new JComboBox();
			
			getInfoButton = new JButton("Get VM Info");
			getInfoButton.addActionListener(this);
			//getInfoButton.setBackground(Color.darkGray);
			//getInfoButton.setForeground(Color.white);
			
			executeButton = new JButton("Execute");
			executeButton.addActionListener(this);
			//executeButton.setBackground(Color.darkGray);
			//executeButton.setForeground(Color.white);
			
			buttonPanel.add(vmLabel);
			buttonPanel.add(vmComboBox);
			
			buttonPanel.add(new JLabel(""));
			
			buttonPanel.add(actionLabel);
			buttonPanel.add(actionComboBox);
			
			buttonPanel.add(new JLabel(""));
				
			
			innerButtonPanel.add(getInfoButton);
			innerButtonPanel.add(executeButton);
			
			buttonPanel.add(innerButtonPanel);
			
			buttonPanel.setBorder(BorderFactory.createLineBorder(Color.blue, 2));
			buttonPanel.setVisible(true);
			buttonPanel.setBackground(Color.white);
			innerPanel.add(buttonPanel); //add the inside panel to the outer container
			
			resultTextArea = new JTextArea("", 11, 12); //output result to 5 x 10 JTextArea box
			resultTextArea.setBorder(BorderFactory.createLineBorder(Color.red, 2));
			resultTextArea.setLineWrap(true);
			resultTextArea.setWrapStyleWord(true);
			resultTextArea.setEditable(false);
			
			innerPanel.add(resultTextArea); //text area that will output total price is added to outside container 
			
			container.add(innerPanel, BorderLayout.PAGE_END);
			

			setPreferredSize(new Dimension(550,370));
			pack(); //adjust size for best fit
		    setResizable(false);
			setLocationRelativeTo(null); //show application in center of screen
			setVisible(true);
		}
		
		
	    public void Connect()
	    {
	    	//remove any data before connecting and adding more data to the combo boxes
	    	vmComboBox.removeAllItems();
	    	actionComboBox.removeAllItems();
	    	
	    	String ip = "";
	    	String username = "";
	    	String password = "";
	    	
	    	//get all needed data needed to connect to ESX server
	    	ip = ipTextField.getText();
	    	username = usernameTextField.getText();
	    	password = passwordPasswordField.getText();
	    	
	    	boolean success = false;
	    	
			try {
				success = esx_instance.connect(ip, username, password);
			} catch (RemoteException e) {
				
				e.printStackTrace();
			} catch (MalformedURLException e) {
				
				e.printStackTrace();
			}
	      	  	
	    	if(success)
	    	{
				resultTextArea.setText("Connected to ESX server.");  	
				
		    	try {
					vms = esx_instance.getVirtualMachines();
				} catch (InvalidProperty e) {
					
					e.printStackTrace();
				} catch (RuntimeFault e) {
					
					e.printStackTrace();
				} catch (RemoteException e) {
					
					e.printStackTrace();
				}
				//add all virtual machine names to the Virtual Machine drop down box
		    	refreshComboBox();
				
				//add all actions to the actions drop down box
				for(String item : actionItems)
				{
					actionComboBox.addItem(item);
				}
				setupEventMonitor();
	    	}
	    	else
	    	{
				resultTextArea.setText("Could not connect to ESX server.");    		
	    	}
	    }

	    
	    private void refreshComboBox()
	    {
	    	synchronized(this)
	    	{
	    	try {
				vms = esx_instance.getVirtualMachines();
			} catch (InvalidProperty e) {
				
				e.printStackTrace();
			} catch (RuntimeFault e) {
				
				e.printStackTrace();
			} catch (RemoteException e) {
				
				e.printStackTrace();
			}
	    	
	    	vmComboBox.removeAllItems();	    	
	    	for(int i = 0; i < vms.size(); i++)
			{
				VirtualMachine vm = vms.get(i); 
				VirtualMachineConfigInfo vminfo = vm.getConfig();
				if(vminfo !=null)
					vmComboBox.addItem(vminfo.getName());
			}
	    	}
	    }
	    
	    
		 
	    @Override
		public void handleEvent(ObjectUpdate oUpdate) 
	    {
			refreshComboBox();
			
		}	
	    
	    
	    private void setupEventMonitor()
	    {
	    	
	          System.out.println("info---" + esx_instance.getServiceInstance().getAboutInfo().getFullName());

	          VMCreateDeleteEventMonitor eventMonitor = new VMCreateDeleteEventMonitor();
	          eventMonitor.addListener(this);
	          try {
				eventMonitor.initialize(esx_instance.getServiceInstance());
				
		          
		          Thread watchUpdates = new Thread(eventMonitor);
		          eventMonitor.shouldRun = true;
		          watchUpdates.start();
			} catch (Exception e) {
			
				e.printStackTrace();
				eventMonitor.shouldRun = false;
		        
			}   
	    }
	    
	    //Disconnect from ESX server
	    public void disconnect()
	    {
	    	esx_instance.disconnect();
	    	//remove all items from combo boxes
	    	vmComboBox.removeAllItems();
	    	actionComboBox.removeAllItems();
	    	resultTextArea.setText("Disconnected from ESX server.");
	    }
	    
	    

	    
	    
		public static void main(String[] args) 
		{
			HueVIClient app = new HueVIClient();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //clean exit of app when user exits
		}		
}
