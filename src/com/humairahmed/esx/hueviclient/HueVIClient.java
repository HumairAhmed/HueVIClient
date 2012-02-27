/* Author:   Humair Ahmed
 * Version:  1.0
 * Date:     03/15/2010
 * Website:  http://www.HumairAhmed.com
 * 
 * Open source software being distributed under GPL license. For more information see here:
 * http://www.gnu.org/copyleft/gpl.html. 
 * 
 * Can edit and redistribute code as long as above reference of authorship is kept within the code.
 * 
 */



package com.humairahmed.esx.hueviclient;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

import java.net.MalformedURLException;
import java.net.URL;

import com.vmware.vim25.FileFault;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.ToolsUnavailable;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;



public class HueVIClient extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 8597898401783011167L;
	public static final String[] actionItems = {"Power On", "Power Off", "Suspend", "Reset", "Shut Down Guest", "Restart Guest"};
	
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
	private JMenuItem quitItem;
	
	private Container container;
	private FlowLayout layout;
	private BorderLayout outerLayout;
	
	private int vmCount;           //Virtual Machine Count on ESX Server
	//One instance per ESX Server/VirtualCenter Server - starting point for all managed objects
	private ServiceInstance si;    
	  
	private ManagedEntity[] mes;  //holds Virtual Machine objects 
		
	
	
	public HueVIClient()
	{
		super("ESX HueVIClient"); //Frame title
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
			Disconnect(); //disconnect from ESX server
		}
	    else if(e.getSource() == getInfoButton) //get info about the virtual machine
		{	
			VirtualMachine vm = null;
			String info = null;
			
			if(mes!=null || mes.length !=0) //if at least one virtual machine exists
			{
				//get selected virtual machine
				vm = (VirtualMachine) mes[vmComboBox.getSelectedIndex()];
			
				//get virtual machine info
				info = "Virtual Machine: " + vm.getConfig().getGuestFullName() +
				       "\n\n" + "# of CPUs Allocated: " + vm.getConfig().hardware.getNumCPU()
				       + "\n\n" + "Memory Allocation: " + vm.getConfig().hardware.memoryMB + " Megabytes";
				
				resultTextArea.setText(info);
			}
		}
		else if(e.getSource() == executeButton) //execute selected command on virtual machine
		{
			VirtualMachine vm = null;
			String action = null;
			
			if(mes!=null || mes.length !=0) //if at least one virtual machine exists
			{
				//get selected virtual machine and selected action
				vm = (VirtualMachine) mes[vmComboBox.getSelectedIndex()];
				action = actionComboBox.getSelectedItem().toString();
			
				//get virtual machine name/Guest OS
				String vm_name = vm.getConfig().getGuestFullName();
			
			//take appropriate action based on selection command to execute
			if(action.equalsIgnoreCase("Power On"))
			{	 
				try 
				{
					vm.powerOnVM_Task(null);
					resultTextArea.setText(vm_name + " VM powered on.");
				} 
				catch (VmConfigFault e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (TaskInProgress e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (FileFault e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InvalidState e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InsufficientResourcesFault e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (RuntimeFault e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (RemoteException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else if(action.equalsIgnoreCase("Power Off"))
			{
				try 
				{
					vm.powerOffVM_Task();
					resultTextArea.setText(vm_name + " VM powered off.");
				} 
				catch (TaskInProgress e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (InvalidState e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (RuntimeFault e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (RemoteException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else if(action.equalsIgnoreCase("Suspend"))
			{
				try 
				{
					vm.suspendVM_Task();
					resultTextArea.setText(vm_name + " VM suspended.");
				} 
				catch (TaskInProgress e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (InvalidState e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (RuntimeFault e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (RemoteException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else if(action.equalsIgnoreCase("Reset"))
			{
				try 
				{
					vm.resetVM_Task();
					resultTextArea.setText(vm_name + " VM reset.");
				} 
				catch (TaskInProgress e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (InvalidState e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (RuntimeFault e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (RemoteException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else if(action.equalsIgnoreCase("Shut Down Guest"))
			{
				try
				{
					vm.shutdownGuest();
					resultTextArea.setText(vm_name + " VM guest shut down.");
				} 
				catch (TaskInProgress e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (InvalidState e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (ToolsUnavailable e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (RuntimeFault e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (RemoteException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else if(action.equalsIgnoreCase("Restart Guest"))
			{
				try 
				{
					vm.reload();
					resultTextArea.setText(vm_name + " VM guest restarted.");
				} 
				catch (RuntimeFault e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (RemoteException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			}
		}
		else if(e.getSource() == quitItem) //if 'Quit' menu item is selected
		{
			resultTextArea.setText("Bye");
			System.exit(0); //terminate the program
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
		
		quitItem = new JMenuItem("Quit");
        quitItem.setMnemonic('Q');
        quitItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        quitItem.addActionListener(this);
		
        menubar.add(fileMenu); 
        fileMenu.add(quitItem);
        
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
		
		getInfoButton = new JButton("Get Info");
		getInfoButton.addActionListener(this);
		getInfoButton.setBackground(Color.darkGray);
		getInfoButton.setForeground(Color.white);
		
		executeButton = new JButton("Execute");
		executeButton.addActionListener(this);
		executeButton.setBackground(Color.darkGray);
		executeButton.setForeground(Color.white);
		
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
		

		setPreferredSize(new Dimension(450,370));
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
    	
    	
    	//try connecting to ESX server using supplied authentication data
		try
		{
			this.si = new ServiceInstance(new URL("https://" + ip + "/sdk"), username, password, true);
			Folder rootFolder = si.getRootFolder(); //needed to get all virtual machines
		
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
					VirtualMachineConfigInfo vminfo = vm.getConfig();
					
					vmComboBox.addItem(vminfo.getGuestFullName());
				}
				
				//add all actions to the actions drop down box
				for(String item : actionItems)
				{
					actionComboBox.addItem(item);
				}
				
			}
			if (si != null)
			{
				resultTextArea.setText("Connected to ESX server.");
			}
			else
			{
				resultTextArea.setText("Could not connect to ESX server.");
			}
		
		}
		catch(MalformedURLException ex)
		{
			
		}
		catch(RemoteException e)
		{
			
		}
    }
	 
    //Disconnect from ESX server
    public void Disconnect()
    {
    	si.getServerConnection().logout();
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
