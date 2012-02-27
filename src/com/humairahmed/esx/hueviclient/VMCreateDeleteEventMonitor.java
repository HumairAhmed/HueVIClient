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

import java.util.ArrayList;

import com.vmware.vim25.ArrayOfEvent;
import com.vmware.vim25.Event;
import com.vmware.vim25.EventFilterSpec;
import com.vmware.vim25.EventFilterSpecByEntity;
import com.vmware.vim25.EventFilterSpecRecursionOption;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.ObjectUpdate;
import com.vmware.vim25.ObjectUpdateKind;
import com.vmware.vim25.PropertyChange;
import com.vmware.vim25.PropertyChangeOp;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertyFilterUpdate;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RequestCanceled;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.UpdateSet;
import com.vmware.vim25.VmEvent;
import com.vmware.vim25.mo.EventHistoryCollector;
import com.vmware.vim25.mo.EventManager;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.PropertyCollector;
import com.vmware.vim25.mo.PropertyFilter;
import com.vmware.vim25.mo.ServiceInstance;


public class VMCreateDeleteEventMonitor implements Runnable
{

  private Folder _rootFolder;

  // EventManager and EventHistoryCollector References
  private EventManager _eventManager;

  private EventHistoryCollector _eventHistoryCollector;

  private static PropertyFilter propFilter;

  private static PropertyCollector propColl;

  public static Boolean shouldRun;
  
  private PropertyFilterSpec spec;
  
  private ArrayList<VMCreateDeleteEventListener> listener = new ArrayList<VMCreateDeleteEventListener>();

  /**
   * Initialize the necessary Managed Object References needed here
   */
  public void initialize(ServiceInstance si) throws Exception
  {
	
      _eventManager = si.getEventManager();
      _rootFolder = si.getRootFolder();
      createEventHistoryCollector();
      spec = createEventFilterSpec();
      propColl = si.getPropertyCollector();
      propFilter = propColl.createFilter(spec, true);
  }

  public PropertyFilterSpec getFilterSpec(){
	  return spec;
  }
  private void createEventHistoryCollector() throws Exception
  {
      // Create an Entity Event Filter Spec to
      // specify the MoRef of the VM to be get events filtered for
      EventFilterSpecByEntity entitySpec = new EventFilterSpecByEntity();
      entitySpec.setEntity(_rootFolder.getMOR());
      entitySpec.setRecursion(EventFilterSpecRecursionOption.children);

      // set the entity spec in the EventFilter
      EventFilterSpec eventFilter = new EventFilterSpec();
      eventFilter.setEntity(entitySpec);

      // we are only interested in getting events for the VM.
      // Add as many events you want to track relating to vm.
      // Refer to API Data Object vmEvent and see the extends class list for
      // elaborate list of vmEvents
      eventFilter.setType(new String[] { "VmCreatedEvent",
              "VmRemovedEvent",  "VmRenamedEvent" });

      // create the EventHistoryCollector to monitor events for a VM
      // and get the ManagedObjectReference of the EventHistoryCollector
      // returned
      _eventHistoryCollector = _eventManager
              .createCollectorForEvents(eventFilter);
  }

  private PropertyFilterSpec createEventFilterSpec()
  {
      // Set up a PropertySpec to use the latestPage attribute
      // of the EventHistoryCollector

      PropertySpec propSpec = new PropertySpec();
      propSpec.setAll(new Boolean(false));
      propSpec.setPathSet(new String[] { "latestPage" });
      propSpec.setType(_eventHistoryCollector.getMOR().getType());

      // PropertySpecs are wrapped in a PropertySpec array
      PropertySpec[] propSpecAry = new PropertySpec[] { propSpec };

      // Set up an ObjectSpec with the above PropertySpec for the
      // EventHistoryCollector we just created
      // as the Root or Starting Object to get Attributes for.
      ObjectSpec objSpec = new ObjectSpec();
      objSpec.setObj(_eventHistoryCollector.getMOR());
      objSpec.setSkip(new Boolean(false));

      // Get Event objects in "latestPage" from "EventHistoryCollector"
      // and no "traversl" further, so, no SelectionSpec is specified
      objSpec.setSelectSet(new SelectionSpec[] {});

      // ObjectSpecs are wrapped in an ObjectSpec array
      ObjectSpec[] objSpecAry = new ObjectSpec[] { objSpec };

      PropertyFilterSpec spec = new PropertyFilterSpec();
      spec.setPropSet(propSpecAry);
      spec.setObjectSet(objSpecAry);
      return spec;
  }

  void handleUpdate(UpdateSet update)
  {
      ObjectUpdate[] vmUpdates;
      PropertyFilterUpdate[] pfus = update.getFilterSet();
      for (int pfui = 0; pfui < pfus.length; pfui++)
      {
          System.out.println("Virtual Machine updates:");
          vmUpdates = pfus[pfui].getObjectSet();
          for (ObjectUpdate vmi : vmUpdates)
          {
              System.out.println("Handling object update");
              handleObjectUpdate(vmi);
          }
      }
      if(pfus.length>0)
	      for(VMCreateDeleteEventListener l: listener){
			  l.handleEvent(pfus[0].getObjectSet()[0]);
		  }
  }

  protected void handleObjectUpdate(ObjectUpdate oUpdate)
  {
	  
      PropertyChange[] pc = oUpdate.getChangeSet();
      System.out.println("Update kind = " + oUpdate.getKind());
      if (oUpdate.getKind() == ObjectUpdateKind.enter )
      {
          System.out.println(" New Data:");
          handleChanges(pc);
      } else if (oUpdate.getKind() == ObjectUpdateKind.leave)
      {
          System.out.println(" Removed Data:");
          handleChanges(pc);
      } else if (oUpdate.getKind() == ObjectUpdateKind.modify)
      {
          System.out.println(" Changed Data:");
          handleChanges(pc);
      }

  }

  void handleChanges(PropertyChange[] changes)
  {
	  
      for (int pci = 0; pci < changes.length; ++pci)
      {
          String name = changes[pci].getName();
          Object value = changes[pci].getVal();
          PropertyChangeOp op = changes[pci].getOp();
          if (value != null && op!= PropertyChangeOp.remove)
          {
              System.out.println("===============");
              System.out.println("\nEvent Details follows:");
              if (value instanceof ArrayOfEvent)
              {
                  ArrayOfEvent aoe = (ArrayOfEvent) value;
                  Event[] evts = aoe.getEvent();
                  for (int evtID = 0; evtID < evts.length; ++evtID)
                  {
                      Event anEvent = evts[evtID];
                      System.out.println("\n----------" + "\n Event ID: "
                              + anEvent.getKey() + "\n Event: "
                              + anEvent.getClass().getName()
                              + "\n FullFormattedMessage: "
                              + anEvent.getFullFormattedMessage()
                              + "\n VM Reference: "
                            
                              + "\n----------\n");
                  }
              } else if (value instanceof VmEvent)
              {
                  VmEvent anEvent = (VmEvent) value;
                  System.out.println("\n----------" + "\n Event ID: "
                          + anEvent.getKey() + "\n Event: "
                          + anEvent.getClass().getName()
                          + "\n FullFormattedMessage: "
                          + anEvent.getFullFormattedMessage()
                          + "\n VM Reference: "
                          + anEvent.getVm().getVm().get_value()
                          + "\n----------\n");
              }
              System.out.println("===============");
          }
      }
  }

  public void addListener(VMCreateDeleteEventListener l){
	  listener.add(l);
  }
  
  
  public void run()
  {
      String version = "";
      try
      {
          do
          {
              try
              {
                  System.out
                          .println("Waiting for new Updates. \n");
                  UpdateSet update = propColl.waitForUpdates(version);
                  if (update != null && update.getFilterSet() != null)
                  {

                      version = update.getVersion();
                      System.out.println(" Current Version: " + version);
                      
                      this.handleUpdate(update);

                  } else
                  {
                      System.out.println("No update is present!");
                  }
              } catch (Exception e)
              {        	
              	propColl.cancelWaitForUpdates();
                System.out
                        .println("Exiting the program, performing required cleaning");
                propFilter.destroyPropertyFilter();
              }
          } while (true);
      } catch (Exception e)
      {
          if (e instanceof RequestCanceled)
          {
              System.out.println("OK");
          } else
          {
              System.out.println("Caught Exception : " + " Name : "
                      + e.getClass().getName() + " Message : "
                      + e.getMessage() + " Trace : ");
          }
      }
  }
}
