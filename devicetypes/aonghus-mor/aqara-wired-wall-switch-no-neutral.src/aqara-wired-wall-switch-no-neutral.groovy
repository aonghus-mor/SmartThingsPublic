/**
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Modified from DTH by a4refillpad
 *
 *  01.10.2017 first release
 *  01.11.2018 Adapted the code to work with QBKG03LM
 *  21.04.2019 handling cluster 0006 to update the app device state when the buttons are pressed manually
 *             used code parts from: https://github.com/dschich/Smartthings/blob/master/devicetypes/dschich/Aqara-Switch-QBKG12LM.src/Aqara-Switch-QBKG12LM.groovy  
 *  20.06.2019 - 12.08.2020 modified by @aonghus-mor 
 *  12.08.2020 modified by @aonghus-mor to recognise QBKG21LM & QBKG22LM (but not yet QBKG25LM).
 *  13.10.2020 New version by @aonghus-mor for new smartthigs app.
*/
 
import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata 
{
    definition (name: "Aqara Wired Wall Switch No Neutral", namespace: "aonghus-mor", author: "aonghus-mor", 
    			 mnmn: "LUMI", vid: "generic-switch", ocfDeviceType: "oic.d.switch")
    {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        //capability "Momentary"
        capability "Button"
        capability "Temperature Measurement"
        capability "Health Check"
        
        //command "on3"
        //command "off3"
        //command "on2"
        //command "off2"
        //command "on1"
        //command "off1"
        //command "buttonpush"
        //command "doButtonPush"
        //command "leftButtonPush"
        //command "rightButtonPush"
        command "childOn"
        command "childOff"
        
        
        //attribute "switch","ENUM", ["on","off", "turningOn", "turningOff", "held", "released", "pushed"]
        //attribute "switch2", "string"
        attribute "lastCheckin", "string"
        attribute "lastPressType", "enum", ["soft","hard","both","held","released","refresh"]
        //attribute "momentary", "ENUM", ["Pressed", "Standby"]
        attribute "button", "ENUM", ["Pressed", "Held", "Standby"]
        //attribute "tempOffset", "number"
        
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0001,0002,0003,0004,0005,0006,0010,000A", outClusters: "0019,000A", 
        		manufacturer: "LUMI", model: "lumi.ctrl_neutral2", deviceJoinName: "Aqara Switch QBKG03LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.ctrl_neutral1", deviceJoinName: "Aqara Switch QBKG04LM"
    	fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.b2lacn02", deviceJoinName: "Aqara Switch QBKG22LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.b1lacn02", deviceJoinName: "Aqara Switch QBKG21LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.l3acn3", deviceJoinName: "Aqara Switch QBKG25LM"       
    }
	
    preferences 
    {
    	input name: "unwiredSwitch", type: "enum", options: ['None', 'First', 'Second'], title: "Identify the unwired switch", 
        							defaultValue: 'None', displayDuringSetup: true
        input name: "tempOffset", type: "decimal", title:"Temperature Offset", 
        							description:"Adjust temperature by this many degrees", range:"*..*", defaultValue: 0                          
        input name: "infoLogging", type: "bool", title: "Display info log messages?", defaultValue: true
		input name: "debugLogging", type: "bool", title: "Display debug log messages?"
    }
}


// Parse incoming device messages to generate events
def parse(String description)
{
   	displayDebugLog( "Parsing '${description}'" )
    //Map testmap = zigbee.parseDescriptionAsMap(description)
    //displayDebugLog("As Map: " + testmap)
    def dat = new Date()
    def newcheck = dat.time
    state.lastCheckTime = state.lastCheckTime == null ? 0 : state.lastCheckTime
    def diffcheck = newcheck - state.lastCheckTime
    displayDebugLog(newcheck + " " + state.lastCheckTime + " " + diffcheck)
    state.lastCheckTime = newcheck
    /*
    if ( diffcheck > 2000 ) // if the state has not been resolved after 2 seconds, clear the flags
    {
    	if ( state.flag == null && ( state.sw1 != null || state.sw2 != null || state.sw3 != null ) )
        	state.flag = 'soft'
        else
        	clearFlags()
    }
    */
    displayDebugLog( "(parse)flags: " + showFlags() )
  
   	def events = []
   
   	if (description?.startsWith('catchall:')) 
		events = events + parseCatchAllMessage(description)
	else if (description?.startsWith('read attr -')) 
		events << parseReportAttributeMessage(description)
    else if (description?.startsWith('on/off: '))
        parseCustomMessage(description) 
    
    if ( events == [null] )
    	events = parseFlags()
    else 
    	events = events + parseFlags()
    
    def now = dat.format("HH:mm:ss EEE dd MMM '('zzz')'", location.timeZone) + "\n" + state.lastPressType
    events << createEvent(name: "lastCheckin", value: now, descriptionText: "Check-In", displayed: debugLogging)
    
    displayDebugLog( "Parse returned: $events" )
    return events
}

private def showFlags()
{
	return state.flag + " " + state.sw1 + " " + state.sw2 + " " + state.sw3 + " " + state.lastCheckTime
}

def updateTemp()
{
	// every half hour get the temperature
    def dat = new Date()
    def cmd = null
    if ( dat.time - state.lastTempTime > 1800000 ) 
    {
    	log.debug "Requesting Temperature"
        state.lastTempTime = dat.time
        cmd = [response(delayBetween(zigbee.readAttribute(0x0002,0),1000))]
    }
	return cmd
}

private def parseFlags()
{
    def events = []
    def lastPress
    def makeEvent = true
    displayDebugLog( "parsing flags: " + showFlags() )
   
    switch( state.flag )
    {
    	case "held":
        	if ( state.sw1 != null )
    		{
            	events << createEvent(name: 'switch', value: state.sw1 )
            	//if ( state.unwired == "First" )
                events << createEvent(name: 'button', value: 'held', data:[buttonNumber: 1], isStateChange: true)
                displayInfoLog('First switch held.')
            }
            else if ( state.sw2 != null )
            {
                getChildDevices()[0].sendEvent(name: 'switch', value: state.sw2 )
                //if ( state.unwired == "Second" )
                events << createEvent(name: 'button', value: 'held', data:[buttonNumber: 3], isStateChange: true)
                displayInfoLog('Second switch held.')
            }
            else if ( state.sw3 != null )
            {
                getChildDevices()[1].sendEvent(name: 'switch', value: state.sw3 )
                //if ( state.unwired == "Third" )
                events << createEvent(name: 'button', value: 'held', data:[buttonNumber: 5], isStateChange: true)
                displayInfoLog('Third switch held.')
            }
           	lastPress = "held"
            makeEvent = false
            //state.flag = null
            //clearFlags()
            break
         case "released":
         	if ( state.unwired != 'None' )
            {
            	if ( state.sw1 != null )
            	{
                	if ( state.unwired == 'First' )
                	{
                		events << createEvent(name: 'switch', value: 'off' )
                    	events	<< response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp1] ))
                	}
    			}
            	else if ( state.sw2 != null )
            	{
                	if ( state.unwired == 'Second' )
                	{
                		getChildDevices()[0].sendEvent(name: 'switch', value: 'off' )
                    	events	<< response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp2] ))
                	}
        		}
            	else if ( state.sw3 != null )
            	{
                	if ( state.unwired == 'Third' )
             		{
                		getChildDevices()[1].sendEvent(name: 'switch', value: 'off' )
                		events	<< response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp3] ))
                	}
       			}
            }
            lastPress = "released"
            state.flag = null
            clearFlags()
            break
         case "both":
            if ( state.sw1 != null && state.sw2 != null )
            {
            	events << createEvent(name: 'button', value: 'pushed', data:[buttonNumber: 5], isStateChange: true)
            	state.flag = null
            	if ( state.sw1 == 'off' )
            	{
                	events << createEvent(name: 'switch', value: 'off')
                    //events	<< response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp1] ))
                }
            	else if (state.sw1 == 'on' )
            	{
            		events 	<< createEvent(name: 'switch', value: 'on', isStateChange: true)
                	if ( state.unwired == 'First' )
                	{
                    	events	<< response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp1] ))
                        state.flag = 'soft'
                	}
            	}
            	state.sw1 = null
                //state.flag = 'soft'
            	if ( state.sw2 == 'off' )
            		getChildDevices()[0].sendEvent(name: 'switch', value: 'off' )
            	else if (state.sw2 == 'on' )
            	{
            		getChildDevices()[0].sendEvent(name: 'switch', value: 'on', isStateChange: true)
                	if ( state.unwired == 'Second' )
                	{
                    	events	<< response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp2] ))
                        state.flag = 'soft'
                	}
            	}
            	state.sw2 = null
            }
            break
         case "double":
         	displayDebugLog("Double Detected")
         case "refresh":
         case "soft":
         case "hard":
            if ( state.sw1 != null )
            {
            	if ( state.sw1 == 'off' )
            	{
                	events << createEvent(name: 'switch', value: 'off')
                    if ( state.unwired != 'First' )
                    	events 	<< createEvent(	name: 'button', value: 'pushed', data:[buttonNumber: (state.flag == 'double') ? 2 : 1], isStateChange: true)
                    state.flag = null
                }
            	else if (state.sw1 == 'on' )
            	{
                    events 	<< createEvent(name: 'switch', value: 'on', isStateChange: true)
                    events 	<< createEvent(	name: 'button', value: 'pushed', data:[buttonNumber: (state.flag == 'double') ? 2 : 1], isStateChange: true)
                    state.flag = null
                	if ( state.unwired == 'First' )
                    {
                    	events	<< response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp1] ))
                    	state.flag = 'soft'
                    }
            	}
            }
         	else if ( state.sw2 != null )
            {
            	displayDebugLog(getChildDevices())
                if ( state.sw2 == 'off' )
            	{
                	getChildDevices()[0].sendEvent(name: 'switch', value: 'off' )
                    if ( state.unwired != 'Second' )
                    	events 	<< createEvent(	name: 'button', value: 'pushed', data:[buttonNumber: (state.flag == 'double') ? 4 : 3], isStateChange: true)
                    state.flag = null
                }
            	else if (state.sw2 == 'on' )
            	{
                    getChildDevices()[0].sendEvent(name: 'switch', value: 'on', isStateChange: true)
                    events 	<< createEvent(	name: 'button', value: 'pushed', data:[buttonNumber: (state.flag == 'double') ? 4 : 3], isStateChange: true)
                    state.flag = null
                	if ( state.unwired == 'Second' )
                    {
                    	events	<< response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp2] ))
                        state.flag = 'soft'
                    }
            	}
                
            }
            else if ( state.sw3 != null )
            {
            	if ( state.sw3 == 'off' )
            	{
                	getChildDevices()[1].sendEvent(name: 'switch', value: 'off' )
                    if ( state.unwired != 'Third' )
                    	events 	<< createEvent(	name: 'button', value: 'pushed', data:[buttonNumber: (state.flag == 'double') ? 6 : 5], isStateChange: true)
                	state.flag = null
                }
            	else if (state.sw3 == 'on' )
            	{
            		getChildDevices()[1].sendEvent(name: 'switch', value: 'on', isStateChange: true)
                    events 	<< createEvent(	name: 'button', value: 'pushed', data:[buttonNumber: (state.flag == 'double') ? 6 : 5], isStateChange: true)
                    state.flag = null
                	if ( state.unwired == 'Third' )
                    {
                    	events	<< response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp3] ))
                        state.flag = 'soft'
                    }
            	}
            }
            clearFlags()
            makeEvent = events.size() > 0
            //if ( makeEvent )
            //{
            //	lastPress = state.flag
            //    displayDebugLog( "clearing flags" )
            //	clearFlags()
            //}
            break
        default:
        	break
    }
    if ( lastPress != null )
    	state.lastPressType = lastPress
  
  	events
}



private def clearFlags()
{
	//state.flag = null
    state.sw1 = null
    state.sw2 = null
    state.sw3 = null
    //displayDebugLog("Flags cleared.")
}

private def parseCatchAllMessage(String description) 
{
	def cluster = zigbee.parse(description)
	displayDebugLog( cluster )
    def event = []
    
    switch ( cluster.clusterId ) 
    {
    	case 0x0000: 
         	if ( cluster.command == 0x0a && cluster.data[0] == 0x01 )
            {
        		Map dtMap = dataMap(cluster.data)
                displayDebugLog( "Map: " + dtMap )
                if ( state.unwired == null )
                	state.unwired = unwiredSwitch
                //if ( dtMap.get(110) == 0x0002 )
                //	state.unwired = 'Left'
                //if ( dtMap.get(111) == 0x0002)
                //	state.unwired = 'Right'
                if ( state.unwired != 'None' && !state.numButtons )
                	getNumButtons()
                event = event + setTemp( dtMap.get(3) )
                displayDebugLog("Number of Switches: ${state.numSwitches}")
                def onoff = (dtMap.get(100) ? "on" : "off")
                switch ( state.numSwitches )
                {
                	case 1:
                    	displayInfoLog( 'Hardware Switch is ' + onoff )
                        displayInfoLog( 'Software Switch is ' + device.currentValue('switch') )
                        break
                    case 2:
                    	displayInfoLog( "Unwired Switch is ${state.unwired}" )
                		displayInfoLog( "Hardware Switches are (" + onoff + "," + (dtMap.get(101) ? "on" : "off") +")" )
                        displayInfoLog( 'Software Switches are (' + device.currentValue('switch') + ',' + getChildDevices()[0].device.currentValue('switch') + ')' )
                    	
                        try
                        {
                        	def onoff2 = (dtMap.get(101) ? 'on' : 'off' )
                        	if ( getChildDevices()[0].device.currentValue('switch') != onoff2 )
                            	getChildDevices()[0].sendEvent(name: 'switch', value: onoff2 )
                            displayDebugLog("DH synced with hardware - ${getChildDevices()[0].device.currentValue('switch')} - ${onoff2}")
                        }
                		catch(Exception e) 
        				{
							displayDebugLog( "${e}")
        				}    
                        break
                    case 3:
                    	displayInfoLog( "Switches are (" + onoff + "," + (dtMap.get(101) ? "on" : "off") + "," + (dtMap.get(102) ? "on" : "off")+")" )
                		break
                    default:
                    	displayDebugLog("Number of switches unrecognised: ${state.numSwitches}")
                }
                try
                {
                	if ( device.currentValue('switch') != onoff )
                	{
                    	event << createEvent(name: 'switch', value: onoff )
                        displayDebugLog("DH synced with hardware - ${device.currentValue('switch')} - ${onoff}")
                    }
                }
                catch(Exception e) 
        		{
					displayDebugLog( "${e}")
        		}
                displayDebugLog("Flags: " + showFlags() )
            }
        	break
        case 0x0006: 	
        	//def onoff = cluster.data[0] == 0x01 ? "on" : "off"
        	switch ( cluster.sourceEndpoint) 
            {
        		case state.endp1:
                case state.endp1b:
                case state.endp2:
                case state.endp2b:
				state.flag = "soft"
                    break
                default:
                	displayDebugLog( "Unknown SourceEndpoint: $cluster.sourceEndpoint" )
    		}
    }
     return event
}

private def setTemp(int temp)
{ 
    def event = []
    //tempOffset = tempOffset == null ? 0 : tempOffset
    if ( state.tempNow != temp || state.tempOffset != tempOffset )
    {
      	state.tempNow = temp
        state.tempOffset = tempOffset ? tempOffset : 0
        if ( getTemperatureScale() != "C" ) 
            temp = celsiusToFahrenheit(temp)
        //log.debug "${temp} - ${tempOffset}"
        state.tempNow2 = temp + ( state.tempOffset == null ? 0 : tempOffset )      
        event << createEvent(name: "temperature", value: state.tempNow2, unit: getTemperatureScale())
        displayDebugLog("Temperature is now ${state.tempNow2}°")          	
	}
    displayDebugLog("setTemp: ${event}")
    return event
}

private def parseReportAttributeMessage(String description) 
{
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
     }
	 def event = null
    
    switch (descMap.cluster) 
    {
    	case "0000":
        	displayDebugLog( "Basic Cluster: $descMap" )
            if ( descMap.attrId == "0007" && descMap.value != "03" )
            	state.batteryPresent = false
            break
    	case "0001": //battery
        	if ( descMap.value == "0000" )
            	state.batteryPresent = false
        	else if (descMap.attrId == "0020")
				event = getBatteryResult(convertHexToInt(descMap.value / 2))
            break
 		case "0002": // temperature
        	if ( descMap.attrId == '0000' ) 
            	event = setTemp( convertHexToInt(descMap.value) )
            break
 		case "0006":  //button press
        	parseSwitchOnOff(descMap)
            break
 		//case "0008":
        //	if ( descMap.attrId == "0000")
    	//		event = createEvent(name: "switch", value: "off")
        //    break
 		default:
        	displayDebugLog( "unknown cluster in $descMap" )
    }
	return event
}

def parseSwitchOnOff(Map descMap)
{
	//parse messages on read attr cluster 0x0006
	def onoff = descMap.value[-1] == "1" ? "on" : "off"
    if ( descMap.value[1] == "c" )
    	state.flag = 'double'
	switch ( descMap.endpoint.toInteger() )
    {
        case state.endp1:
            state.sw1 = onoff
            break
        case state.endp1b: // button 1 pressed
        	state.flag = 'hard'
            break
        case state.endp2: 
        	state.sw2 = onoff
        	break
        case state.endp2b: // button 2 pressed
        //case 0x07:
        //case 0x08:
        	state.flag = "hard"
            break
        case state.endpboth: // both buttons pressed
        	state.flag = "both"
			break
        default:
        	displayDebugLog( "ClusterID 0x0006 with unknown endpoint $descMap.endpoint" )
     }
     //displayDebugLog("$descMap.endpoint " + showFlags())
}

private def parseCustomMessage(String description) 
{
	displayDebugLog( "Parsing Custom Message: $description" )
	if (description?.startsWith('on/off: ')) {
    	if (description == 'on/off: 0')
        {
        	if ( state.flag != 'double' && state.flag != 'soft' )
            	state.flag = "held"
		}
		else if (description == 'on/off: 1')
        	state.flag = "released"
	}
}

def childOn(String dni) 
{
	def endp = [null,state.endp1, state.endp2, state.endp3]
    //dni[-1].toInteger() ) == 2 ? on2() : on3()
    def cmd = zigbee.command(0x0006, 0x01, "", [destEndpoint: endp[dni[-1].toInteger()]] )
    state.flag = 'soft'
    displayDebugLog( cmd )
    cmd 
}

def childOff(String dni) 
{
	def endp = [null,state.endp1, state.endp2, state.endp3]
    //(dni[-1].toInteger() == 2 ) ? off2() : off3()
    def cmd = zigbee.command(0x0006, 0x00, "", [destEndpoint: endp[dni[-1].toInteger()]] )
    state.flag = 'soft'
    displayDebugLog( cmd )
    cmd 
}

def on() 
{
    state.flag = 'soft'
    def cmd = zigbee.command(0x0006, 0x01, "", [destEndpoint: state.endp1] )
    displayDebugLog( cmd )
    cmd 
}


def off() 
{
    def cmd = zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp1] )
    state.flag = 'soft'
    displayDebugLog( cmd )
    cmd
}

def refresh() 
{
	displayInfoLog( "refreshing" )
    clearFlags()
    state.flag = "refresh"
    def dat = new Date()
    state.lastTempTime = dat.time
    settings.unwiredSwitch = settings.unwiredSwitch == null ? 'None' : settings.unwiredSwitch
    settings.tempOffset = settings.tempOffset == null ? 0 : settings.tempOffset 
    settings.infoLogging = settings.infologging == null ? true : settings.infoLogging
    settings.debugLogging = settings.debugLogging == null ? false : settings.debugLogging
    //log.debug "${unwiredSwitch}  ${tempOffset}  ${infoLogging}  ${debugLogging}"
    state.unwired = unwiredSwitch == null ? 'None' : unwiredSwitch 
    state.tempNow = state.tempNow == null ? 0 : state.tempNow
    state.tempNow2 = state.tempNow2 == null ? 0 : state.tempNow2
    state.tempOffset = tempOffset == null ? 0 : tempOffset
    
    state.final = 'off'
    
    getNumButtons()
    if ( state.numSwitches > 1 )
    {
    	//try { deleteChildDevice("${device.deviceNetworkId}-2")
    	//} catch(Exception e) { displayDebugLog("${e}") }
    	def childDevices = getChildDevices()
		displayDebugLog("${childDevices}: ${childDevices.size()}")
   
		if (childDevices.size() == 0) 
    	{
			displayInfoLog( "Creating Children" )
			try 
    		{
    			if ( state.numSwitches > 1)
    			{
                	for ( int i = 2; i <= state.numSwitches; i++ )
    					addChildDevice("Smartthings", "Child Switch Health", "${device.deviceNetworkId}-${i}", null,[label: "${device.displayName}-(${i})"])

                }
			} 
        	catch(Exception e) 
        	{
				displayDebugLog( "${e}")
        	}
			displayInfoLog("Child created")
		}    
        //sendEvent(name: "switch2", value: getChildDevices()[0])
    }                    
    if ( state.unwired != "None" )
    	sendEvent(name: 'supportedButtonValues', value: ['pushed', 'held', 'double'], isStateChange: true)
        
    def cmds = zigbee.readAttribute(0x0002, 0) + 
           		zigbee.readAttribute(0x0000, 0x0007) +
                zigbee.readAttribute(0x0006,0,[destEndpoint: 0x03] )
           
    //if ( state.batteryPresent )
    //	cmds += zigbee.readAttribute(0x0001, 0) //+ zigbee.readAttribute(0x0001,0x0001) + zigbee.readAttribute(0x0001,0x0002)
                
     //cmds += zigbee.configureReporting(0x0000, 0, 0x29, 1800,7200,0x01)
    
     displayDebugLog( cmds )
     //updated()
     
     cmds
}

def installed()
{
	refresh()
}

def configure()
{
	refresh()
}

def updated()
{
	refresh()
}

private getNumButtons()
{
	String model = device.getDataValue("model")
    switch ( model ) 
    {
    	case "lumi.ctrl_neutral1":
        case "lumi.switch.b1lacn02":
        	state.numSwitches = 1
     		state.numButtons = 2
            state.endp1 = 0x02
            state.endp2 = 0xF2
            state.endp1b = 0x04
            state.endp2b = 0xF5
            break
        case "lumi.ctrl_neutral2":
        case "lumi.switch.b2lacn02":
        	state.numSwitches = 2
        	state.numButtons = 5
            state.endp1 = 0x02
            state.endp2 = 0x03
            state.endp1b =0x04
            state.endp2b = 0x05
            break
        case "lumi.switch.l3acn3":
        	displayInfoLog("3-Button switch not yet fully implemented.")
            state.numSwitches = 3
            state.numButtons = 7
            break
        default:
        	displayDebugLog("Unknown device model: " + model)
    }
    state.endpboth = 0x06
    sendEvent(name: 'numberOfButtons', value: state.numButtons, displayed: false )
    displayDebugLog( "Setting Number of Buttons to " + state.numButtons )
}

private Integer convertHexToInt(hex) 
{
	Integer.parseInt(hex,16)
}

private Map dataMap(data)
{
	// convert the catchall data from check-in to a map.
	Map resultMap = [:]
	int maxit = data.size()
    int it = 4
    while ( it < maxit )
    {
    	int lbl = 0x00000000 | data.get(it)
        byte type = data.get(it+1)
        switch ( type)
       	{
        	case DataType.BOOLEAN: 
            	resultMap.put(lbl, (boolean)data.get(it+2))
                it = it + 3
                break
            case DataType.UINT8:
            	resultMap.put(lbl, (short)(0x0000 | data.get(it+2)))
                it = it + 3
                break
            case DataType.UINT16:
            	resultMap.put(lbl, (int)(0x00000000 | (data.get(it+3)<<8) | data.get(it+2)))
                it = it + 4
                break
            case DataType.UINT32:
            	resultMap.put(lbl, (long)(0x0000000000000000 | (((((data.get(it+5) << 8) | data.get(it+4)) << 8 ) | data.get(it+3)) << 8 ) | data.get(it+2)))
                it = it + 6
                break
              case DataType.UINT40:
            	long x = 0x000000000000000
                x |= data.get(it+6) << 32
                x |= data.get(it+5) << 24
                x |= data.get(it+4) << 16
                x |= data.get(it+3) << 8
                x |= data.get(it+2)
            	resultMap.put(lbl, x )
                it = it + 7
                break  
            case DataType.INT8:
            	resultMap.put(lbl, (short)(data.get(it+2)))
                it = it + 3
                break
             case DataType.INT16:
            	resultMap.put(lbl, (int)((data.get(it+3)<<8) | data.get(it+2)))
                it = it + 4
                break   
            default: displayDebugLog( "unrecognised type in dataMap: " + zigbee.convertToHexString(type) )
            	return resultMap
        }
    }
    return resultMap
}

private def displayDebugLog(message) 
{
	if (debugLogging)
		log.debug "${device.displayName} ${message}"
}

private def displayInfoLog(message) 
{
	//if (infoLogging || state.prefsSetCount < 3)
    if (infoLogging)
		log.info "${device.displayName} ${message}"
}