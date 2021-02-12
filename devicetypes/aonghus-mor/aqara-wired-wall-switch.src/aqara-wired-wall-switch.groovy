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
 *  27.10.2020 Adapted for the new 3 button switch QBKG25LM ( Thanks to @Chiu for his help).
*/
 
import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata 
{
    definition (name: "Aqara Wired Wall Switch", namespace: "aonghus-mor", author: "aonghus-mor",
                mnmn: "SmartThingsCommunity", 
                vid: "822341f9-0eac-3dc8-b02a-fbdc64fd9541", 
                //vid: "c24838eb-ca6e-355f-a3c1-ce9b829365dc",
    			//mnmn: "LUMI", vid: "generic-switch", 
                ocfDeviceType: "oic.d.switch")
    {
        capability "Actuator"
        capability "Sensor"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Momentary"
        capability "Button"
        capability "Temperature Measurement"
        capability "Health Check"
        capability "Power Meter"
        capability "Polling"
        
        command "childOn"
        command "childOff"
        
        attribute "lastCheckin", "string"
        attribute "lastPressType", "enum", ["soft","hard","both","held","released","refresh","double"]
        //attribute "momentary", "ENUM", ["Pressed", "Standby"]
        //attribute "button", "ENUM", ["Pressed", "Held", "Standby"]
        //attribute "tempOffset", "number"
   
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0001,0002,0003,0004,0005,0006,0010,000A", outClusters: "0019,000A", 
        		manufacturer: "LUMI", model: "lumi.ctrl_neutral2", deviceJoinName: "Aqara Switch QBKG03LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.ctrl_neutral1", deviceJoinName: "Aqara Switch QBKG04LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.ctrl_ln1.aq1", deviceJoinName: "Aqara Switch QBKG11LM"      
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.ctrl_ln2.aq1", deviceJoinName: "Aqara Switch QBKG12LM"       
    	fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.b1lacn02", deviceJoinName: "Aqara Switch QBKG21LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.b2lacn02", deviceJoinName: "Aqara Switch QBKG22LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.b1nacn02", deviceJoinName: "Aqara Switch QBKG23LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.b2nacn02", deviceJoinName: "Aqara Switch QBKG24LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.l3acn3", deviceJoinName: "Aqara Switch QBKG25LM" 
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.n3acn3", deviceJoinName: "Aqara Switch QBKG26LM"   
        fingerprint deviceId: "5F01", inClusters: "0000 0003 0019 FFFF 0012", outClusters: "0000 0004 0003 0005 0019 FFFF 0012", 
        		manufacturer: "LUMI", model: "lumi.remote.b186acn02", deviceJoinName: "Aqara Switch WXKG06LM"
		        
    }
	
    preferences 
    {	
       	input name: "unwiredSwitch", type: "enum", options: ['None', 'First', 'Second', 'Third', 'First_and_Second', 'First_and_Third', 'Second_and_Third'], title: "Identify the unwired switch", 
        							displayDuringSetup: true
        input name: "tempOffset", type: "decimal", title:"Temperature Offset", 
        							description:"Adjust temperature by this many degrees", range:"*..*", required: false, displayDuringSetup: false                         
        input name: "infoLogging", type: "bool", title: "Display info log messages?", required: false, displayDuringSetup: false
		input name: "debugLogging", type: "bool", title: "Display debug log messages?", required: false, displayDuringSetup: false
    }
}


// Parse incoming device messages to generate events
def parse(String description)
{
   	displayDebugLog( "Parsing '${description}'" )
    
    def dat = new Date()
    def newcheck = dat.time
    state.lastCheckTime = state.lastCheckTime == null ? 0 : state.lastCheckTime
    def diffcheck = newcheck - state.lastCheckTime
    //displayDebugLog(newcheck + " " + state.lastCheckTime + " " + diffcheck)
    state.lastCheckTime = newcheck
  
    if ( diffcheck > 2000 ) // if the state has not been resolved after 2 seconds, clear the flags
    {
    	//if ( state.flag == null && ( state.sw1 != null || state.sw2 != null || state.sw3 != null ) )
        //	state.flag = 'soft'
        //else
        	state.flag = null
        	clearFlags()
            displayDebugLog("Flags timed out")
    }
    
   	def events = []
   
   	if (description?.startsWith('catchall:')) 
		events = events + parseCatchAllMessage(description)
	else if (description?.startsWith('read attr -')) 
		events = events + parseReportAttributeMessage(description)
    else if (description?.startsWith('on/off: '))
        parseCustomMessage(description) 
    
    if ( events == [null] )
    	events = parseFlags()
    else 
    	events = events + parseFlags()
        
    if ( events[0] != null && state.flag != 'held' )
    {
    	state.flag = null
        clearFlags()
        displayDebugLog("Flags cleared")
    }
    
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
    def lastPress = state.flag
    def makeEvent = true
    displayDebugLog( "parsing flags: " + showFlags() )
    if ( state.unwired instanceof String || state.unwired == null )
    	state.unwired = parseUnwiredSwitch()
    
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
                getChild(2).sendEvent(name: 'switch', value: state.sw2 )
                //if ( state.unwired == "Second" )
                events << createEvent(name: 'button', value: 'held', data:[buttonNumber: 3], isStateChange: true)
                displayInfoLog('Second switch held.')
            }
            else if ( state.sw3 != null )
            {
                getChild(3).sendEvent(name: 'switch', value: state.sw3 )
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
         	if ( state.unwired != 0x00 )
            {
            	if ( state.sw1 != null )
            	{
                	if ( (state.unwired & 0x01) != 0x00 )
                	{
                		events << createEvent(name: 'switch', value: 'off' )
                    	events	<< response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp1] ))
                	}
    			}
            	else if ( state.sw2 != null )
            	{
                	if ( (state.unwired & 0x02) != 0x00 )
                	{
                		getChild(2).sendEvent(name: 'switch', value: 'off' )
                    	events	<< response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp2] ))
                	}
        		}
            	else if ( state.sw3 != null )
            	{
                	if ( (state.unwired & 0x04) != 0x00 )
             		{
                		getChild(3).sendEvent(name: 'switch', value: 'off' )
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
                	if ( (state.unwired & 0x01) != 0x00 )
                	{
                    	events	<< response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp1] ))
                        state.flag = 'soft'
                	}
            	}
            	state.sw1 = null
                //state.flag = 'soft'
            	if ( state.sw2 == 'off' )
            		getChild(2).sendEvent(name: 'switch', value: 'off' )
            	else if (state.sw2 == 'on' )
            	{
            		getChild(2).sendEvent(name: 'switch', value: 'on', isStateChange: true)
                	if ( (state.unwired & 0x02) != 0x00 )
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
                    if ( (state.unwired & 0x01) == 0x00 )
                    	events = events + buttondouble(1,2)
                    state.flag = null
                }
            	else if (state.sw1 == 'on' )
            	{
                    events 	<< createEvent(name: 'switch', value: 'on', isStateChange: true)
                    events = events + buttondouble(1,2)
                    state.flag = null
                    // displayDebugLog("Unwired type: ${state.unwired} ${state.unwired & 0x01}")
                	if ( (state.unwired & 0x01) != 0x00 )
                    {
                    	events	<< response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp1] ))
                    	state.flag = 'soft'
                    }
            	}
                else if ( state.sw1 == 'pressed' )
                {
                	events 	<< createEvent(name: 'switch', value: 'on', isStateChange: true)
                    events = events + buttondouble(1,2)
                    state.flag = null
                    events	<< ["delay 1000"] << createEvent(name: 'switch', value: 'off', isStateChange: true)
                }
            }
         	else if ( state.sw2 != null )
            {
                if ( state.sw2 == 'off' )
            	{
                    getChild(2).sendEvent(name: 'switch', value: 'off' )
                    if ( (state.unwired & 0x02) == 0x00 )
                    	buttondouble(3,4)
                    state.flag = null
                }
            	else if (state.sw2 == 'on' )
            	{
                    getChild(2).sendEvent(name: 'switch', value: 'on', isStateChange: true)
                    events = events + buttondouble(3,4)
                    state.flag = null
                	if ( (state.unwired & 0x02) != 0x00 )
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
                	getChild(3).sendEvent(name: 'switch', value: 'off' )
                    if ( (state.unwired & 0x04) == 0x00 )
                    	events = events + buttondouble(5,6)
                    state.flag = null
                }
            	else if (state.sw3 == 'on' )
            	{
            		getChild(3).sendEvent(name: 'switch', value: 'on', isStateChange: true)
                    events = events + buttondouble(5,6)
                    state.flag = null
                	if ( (state.unwired & 0x04) != 0x00 )
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

private def buttondouble(i,j)
{
	def events = []
    if ( state.flag == 'double' )
    {
        events 	<< createEvent(	name: 'button', value: 'double', data:[buttonNumber: i], isStateChange: true)
        events 	<< createEvent(	name: 'button', value: 'pushed', data:[buttonNumber: j], isStateChange: true)
    }
    else
        events 	<< createEvent(	name: 'button', value: 'pushed', data:[buttonNumber: i], isStateChange: true)
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
    def events = []
    
    switch ( cluster.clusterId ) 
    {
    	case 0x0000: 
         	if ( cluster.command == 0x0a && cluster.data[0] == 0x01 )
            {
        		Map dtMap = dataMap(cluster.data)
                displayDebugLog( "Map: " + dtMap )
                if ( state.unwired instanceof String || state.unwired == null )
                	state.unwired = parseUnwiredSwitch()
                if ( state.unwired != 0x00 && !state.numButtons )
                	getNumButtons()
                events = events + setTemp( dtMap.get(3) ) + ( dtMap.get(149) ? getWatts( dtMap.get(149) ) : [] )
                //{
            	//	int x = Integer.parseInt('3fb851ea', 16)
            	//	float y = Float.intBitsToFloat(x)
                //	event = event + getWatts(y)
            	//}
                displayDebugLog("Number of Switches: ${state.numSwitches}")
                def onoff = (dtMap.get(100) ? "on" : "off")
                switch ( state.numSwitches )
                {
                	case 1:
                    	displayInfoLog( "Hardware Switch is ${onoff}" )
                        displayDebugLog( 'Software Switch is ' + device.currentValue('switch') )
                        break
                    case 2:
                    	def onoff2 = (dtMap.get(101) ? 'on' : 'off' )
                        def child = getChild(2)
                    	displayInfoLog( "Unwired Switch Code: ${state.unwired}" )
                		displayInfoLog( "Hardware Switches are (" + onoff + "," + onoff2 +")" )
                        displayDebugLog( 'Software Switches are (' + device.currentValue('switch') + ',' + child.device.currentValue('switch') + ')' )
                    	
                        try
                        {
                        	//Try to stop child device(s) from going offline
                            displayDebugLog("Child DH synced with hardware - ${child.device.currentValue('switch')} - ${onoff2}")
                            if ( (state.unwired & 0x02 ) == 0x00 )
                          		child.sendEvent(name: 'switch', value: onoff2 )
                            else
                            {	
                            	if ( child.device.currentValue('switch') == 'on' )
                               	{
                                	events << response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp2] ))
                        			state.flag = 'soft'
                           		}
                            }
                        }
                		catch(Exception e) 
        				{
							displayDebugLog( "${e}")
        				}    
                        break
                    case 3:
                    	def onoff2 = (dtMap.get(101) ? 'on' : 'off' )
                        def child2 = getChild(2)
                        def onoff3 = (dtMap.get(102) ? 'on' : 'off' )
                        def child3 = getChild(3)
                    	displayInfoLog( "Unwired Switch Code: ${state.unwired}" )
                		displayInfoLog( "Hardware Switches are (${onoff}, ${onoff2}, ${onoff3})" )
                        displayDebugLog( 'Software Switches are (' + device.currentValue('switch') + ',' + child2.device.currentValue('switch') + ',' + child3.device.currentValue('switch')+ ')' )
                    	
                        try
                        {
                        	//Try to stop child device(s) from going offline
                           	displayDebugLog("Child 2 DH synced with hardware - ${child2.device.currentValue('switch')} - ${onoff2}")
                            if ( (state.unwired & 0x02 ) == 0x00 )
                          		child2.sendEvent(name: 'switch', value: onoff2 )
                            else
                            {	
                            	if ( child2.device.currentValue('switch') == 'on' )
                               	{
                                	events << response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp2] ))
                        			state.flag = 'soft'
                           		}
                            }
 							displayDebugLog("Child 3 DH synced with hardware - ${child3.device.currentValue('switch')} - ${onoff3}")
                            if ( (state.unwired & 0x04 ) == 0x00 )
                          		child3.sendEvent(name: 'switch', value: onoff3 )
                            else
                            {	
                            	if ( child3.device.currentValue('switch') == 'on' )
                               	{
                                	events << response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp3] ))
                        			state.flag = 'soft'
                           		}
                            }
                        }
                		catch(Exception e) 
        				{
							displayDebugLog( "${e}")
        				}    
                        break
                    	
                    default:
                    	displayDebugLog("Number of switches unrecognised: ${state.numSwitches}")
                }
                try
                {
                	displayDebugLog("DH synced with hardware - ${device.currentValue('switch')} - ${onoff}")
                    if ( (state.unwired & 0x01 ) != 0x00 && device.currentValue('switch') == 'on' )
                    {	
                    	events << response(["delay 1000"] + zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endp1] ))
                        state.flag = 'soft'
                    }
                }
                catch(Exception e) 
        		{
					displayDebugLog( "${e}")
        		}
                //displayDebugLog("Flags: " + showFlags() )
            }
        	break
        case 0x0006: 	
        	def onoff = cluster.data[0] == 0x01 ? "on" : "off"
            state.flag = "soft"
        	switch ( cluster.sourceEndpoint) 
            {
        		case state.endp1:
                	state.sw1 = onoff
                    break
                case state.endp2:
                	state.sw2 = onoff
                    break
                case state.endp3:
                	state.sw3 = onoff
                    break
                case state.endp1b:
                case state.endp2b:
                case state.endp3b:
                    break
                default:
                	displayDebugLog( "Unknown SourceEndpoint: $cluster.sourceEndpoint" )
                    state.flag = null
    		}
    }
    //displayDebugLog(events)
    return events
}

private def setTemp(int temp)
{ 
    def event = []
    temp = temp ? temp : 0
    //tempOffset = tempOffset == null ? 0 : tempOffset
    if ( state.tempNow != temp || state.tempOffset != tempOffset )
    {
      	state.tempNow = temp
        state.tempOffset = tempOffset ? tempOffset : 0
        if ( getTemperatureScale() != "C" ) 
            temp = celsiusToFahrenheit(temp)
        //log.debug "${temp} - ${tempOffset}"
        state.tempNow2 = temp + state.tempOffset     
        event << createEvent(name: "temperature", value: state.tempNow2, unit: getTemperatureScale())
        displayDebugLog("Temperature is now ${state.tempNow2}°")          	
	}
    displayDebugLog("setTemp: ${event}")
    return event
}

private def getWatts(float pwr)
{
	def event = []
    pwr = pwr ? pwr : 0.0
    if ( abs( pwr - (float)state.power ) > 1e-4 )
    {	
    	state.power = (float)pwr
    	event << createEvent(name: 'power', value: pwr, unit: 'W')
    }
    displayDebugLog("Power: ${pwr} W")
	return event
}

private def abs(x) { return ( x > 0 ? x : -x ) } 

private def parseReportAttributeMessage(String description) 
{
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
     }
	 def events = []
    
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
				events = events + getBatteryResult(convertHexToInt(descMap.value / 2))
            break
 		case "0002": // temperature
        	if ( descMap.attrId == '0000' ) 
            	events = events + setTemp( convertHexToInt(descMap.value) )
            break
 		case "0006":  //button press
        	parseSwitchOnOff(descMap)
            break
        case "000C": //analog input
        	if ( descMap.attrID == "0055" )
            {
            	int x = Integer.parseInt(descMap.value, 16)
            	float y = Float.intBitsToFloat(x)
                events = events + getWatts(y)
            }
        	//displayDebugLog("Power: ${y} Watts")
        	break
        case "0012": //Multistate Input
        	state.flag = 'hard'
            parsePressed(descMap)
   			//displayDebugLog("Cluster 0x0012 seen for hard press.")
            break
 		//case "0008":
        //	if ( descMap.attrId == "0000")
    	//		event = createEvent(name: "switch", value: "off")
        //    break
 		default:
        	displayDebugLog( "unknown cluster in $descMap" )
    }
	return events
}

def parseSwitchOnOff(Map descMap)
{
	//parse messages on read attr cluster 0x0006
	def onoff = descMap.value[-1] == "1" ? "on" : "off"
    if ( descMap.value[1] == "c" )
    	state.flag = 'dble'
    //displayDebugLog("Value: ${descMap.value[2..5]}")
    if ( descMap.value[2..5] != "0000" )
	{
    	switch ( descMap.endpoint.toInteger() )
    	{
        	case state.endp1:
            	state.sw1 = onoff
            	break
        	case state.endp2: 
        		state.sw2 = onoff
        		break
        	case state.endp3:
        		state.sw3 = onoff
            	break
        	case state.endpboth: // both buttons pressed
        		state.flag = "both"
				break
        	default:
        		state.flag = 'hard'
        	//displayDebugLog( "ClusterID 0x0006 with unknown endpoint $descMap.endpoint" )
     	}
	}
     //displayDebugLog("$descMap.endpoint " + showFlags())
}

def parsePressed(Map descMap)
{
	//parse messages on read attr cluster 0x0006
	def action
    state.flag = 'hard'
    switch( descMap.value.toInteger() )
    {
    	case 0: action = 'held'
        		break
        case 1: action = 'pressed'
        		break
        case 2: action = 'double'
        		break
        default: displayDebugLog("Unrecognised value ${descMap.value}")
    }
    if ( true )
	{
    	switch ( descMap.endpoint.toInteger() )
    	{
        	case state.endp1:
            	state.sw1 = action
            	break
        	case state.endp2: 
        		state.sw2 = action
        		break
        	case state.endp3:
        		state.sw3 = action
            	break
        	case state.endpboth: // both buttons pressed
        		state.flag = "both"
				break
        	default:
        		state.flag = 'hard'
        	//displayDebugLog( "ClusterID 0x0006 with unknown endpoint $descMap.endpoint" )
     	}
	}
}

private def parseCustomMessage(String description) 
{
	displayDebugLog( "Parsing Custom Message: $description" )
    //displayDebugLog("lastPressType: ${state.lastPressType}")
	if (description?.startsWith('on/off: ')) 
    {
    	if (description == 'on/off: 0')
        	state.flag = ( state.lastPressType == 'dble' ) ? 'double' : 'held'
		else if (description == 'on/off: 1')
        	state.flag = "released"
	}
}

private def getChild(int i)
{
    def children = getChildDevices()
    for (child in children)
    {	
        if ( child.deviceNetworkId[-1].toInteger() == i )
            return child
    }
    displayDebugLog("Child Device ${i} unrecognised.")
    return null
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
   	displayDebugLog(settings)
    //settings.unwiredSwitch = settings.unwiredSwitch == null ? 'None' : settings.unwiredSwitch
    //settings.tempOffset = settings.tempOffset == null ? 0 : settings.tempOffset 
    //settings.infoLogging = settings.infologging == null ? true : settings.infoLogging
    //settings.debugLogging = settings.debugLogging == null ? false : settings.debugLogging
   
    //displayDebugLog("unwired enum: ${unwiredSwitch} ${unwiredSwitch.value}")
    
    state.unwired = parseUnwiredSwitch()
    
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
		displayDebugLog("Children: ${childDevices}: ${childDevices.size()}")
   
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
        getChild(2).sendEvent(name: 'checkInterval', value: '3000')
    }                    
    if ( state.unwired != 0x00 )
    	sendEvent(name: 'supportedButtonValues', value: ['pushed', 'held', 'double'], isStateChange: true)
    sendEvent( name: 'checkInterval', value: 3000, data: [ protocol: 'zigbee', hubHardwareId: device.hub.hardwareID ] )
        
    def cmds =  zigbee.readAttribute(0x0001, 0) + zigbee.readAttribute(0x0002, 0) /*+ 
           		zigbee.readAttribute(0x0000, 0x0007) + 
                zigbee.readAttribute(0x0006,0,[destEndpoint: state.endp1] ) +
    			zigbee.readAttribute(0x0006,0,[destEndpoint: 0x02] ) +
    			zigbee.readAttribute(0x0006,0,[destEndpoint: 0x03] ) */
    //if ( state.numButtons > 1 )
    //	cmds += zigbee.readAttribute(0x0006,0,[destEndpoint: state.endp2] )
           
    //if ( state.batteryPresent )
    //	cmds += zigbee.readAttribute(0x0001, 0) //+ zigbee.readAttribute(0x0001,0x0001) + zigbee.readAttribute(0x0001,0x0002)
     //cmds += zigbee.readAttribute(0x000C, 0x0055) + zigbee.readAttribute(0x0012, 0x0055)           
    // cmds = //zigbee.configureReporting(0x0000, 0, DataType.INT16, 1800, 3000, null) //+
     //		 zigbee.configureReporting(0x0002, 0, DataType.INT16, 1800, 3000, 0x0001)
     //cmds = zigbee.command(0x0000, 0x000a, 0x0000,[mfgCode: 0x115f])
    
     displayDebugLog( cmds )
     //updated()
     state.flag = null
     cmds
}

def installed()
{
	displayDebugLog('imnstalled')
    refresh()
}

def configure()
{
	displayDebugLog('configure')
	refresh()
}

def updated()
{
	displayDebugLog('updated')
	refresh()
}

def ping()
{
	displayDebugLog("Pinged")
    zigbee.readAttribute(0x0002, 0)
}

def poll()
{
	displayDebugLog("Polled")
    zigbee.readAttribute(0x0002, 0)
}

private getNumButtons()
{
    String model = device.getDataValue("model")
    switch ( model ) 
    {
    	case "lumi.ctrl_neutral1": //QBKG04LM
        case "lumi.switch.b1lacn02": //QBKG21LM
        	state.numSwitches = 1
     		state.numButtons = 2
            state.endp1 = 0x02
            state.endp2 = 0xF2
            state.endp3 = 0xF3
            state.endp1b = 0x04
            state.endp2b = 0xF5
            state.endp3b = 0xF6
            break
        case "lumi.ctrl_ln1.aq1": //QBKG11LM
        	state.numSwitches = 1
     		state.numButtons = 2
            state.endp1 = 0x01
            state.endp2 = 0xF1
            state.endp3 = 0xF2
            state.endp1b = 0x04
            state.endp2b = 0xF5
            state.endp3b = 0xF6
			break
		case "lumi.switch.b1nacn02": //QBKG23LM
            state.numSwitches = 1
     		state.numButtons = 2
            state.endp1 = 0x01
            state.endp2 = 0xF1
            state.endp3 = 0xF2
            state.endp1b = 0x05
            state.endp2b = 0xF5
            state.endp3b = 0xF6
            break
        case "lumi.ctrl_neutral2": //QBKG03LM
        	state.numSwitches = 2
        	state.numButtons = 5
            state.endp1 = 0x02
            state.endp2 = 0x03
            state.endp3 = 0xF3
            state.endp1b =0x04
            state.endp2b = 0x05
            state.endp3b = 0xF6
            state.endpboth = 0x06
            break
        case "lumi.switch.b2lacn02": //QBKG22LM 
        	state.numSwitches = 2
        	state.numButtons = 5
            state.endp1 = 0x02
            state.endp2 = 0x03
            state.endp3 = 0xF3
            state.endp1b =0x2A
            state.endp2b = 0x2B
            state.endp3b = 0xF6
            break
        case "lumi.ctrl_ln2.aq1": //QBKG12LM      
        case "lumi.switch.b2nacn02": //QBKG24LM
           	state.numSwitches = 2
        	state.numButtons = 5
            state.endp1 = 0x01
            state.endp2 = 0x02
            state.endp3 = 0xF3
            state.endp1b =0x05
            state.endp2b = 0x06
            state.endp3b = 0xF6
            state.endpboth = 0x07
            break
        case "lumi.switch.l3acn3": //QBKG25LM
        case "lumi.switch.n3acn3": //QBKG26LM
            state.numSwitches = 3
            state.numButtons = 7
            state.endp1 = 0x01
            state.endp2 = 0x02
            state.endp3 = 0x03
            state.endp1b = 0x29
            state.endp2b = 0x2A
            state.endp3b = 0x2B
            break
        default:
        	displayDebugLog("Unknown device model: " + model)
    }
    
    sendEvent(name: 'numberOfButtons', value: state.numButtons, displayed: false )
    displayDebugLog( "Setting Number of Buttons to " + state.numButtons )
}

private byte parseUnwiredSwitch()
{
	byte unwired
	switch ( unwiredSwitch )
    {
    	case null:
        case 'None': 
        	unwired = 0x00
            break
        case 'First':
        	unwired = 0x01
            break
        case 'Second':
        	unwired = 0x02
            break
        case 'Third':
        	unwired = 0x04
            break
        case 'First_and_Second':
        	unwired = 0x03
            break
        case 'First_and_Third':
        	unwired = 0x05
            break
        case 'Second_and_Third':
        	unwired = 0x06
            break
        default:
        	displayDebugLog("Invalid unwired switch: ${unwiredSwitch}")
    }
    //displayDebugLog("Unwired Code: ${state.unwired}")
    return unwired
}

private Integer convertHexToInt(hex) 
{
	int result = Integer.parseInt(hex,16)
    //displayDebugLog("HextoInt: ${hex}  ${result}")
    return result
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
            	long x = 0x0000000000000000
                for ( int i = 0; i < 4; i++ )
              		x |= data.get(it+i+2) << 8*i
            	resultMap.put(lbl, x )
                it = it + 6
                break
              case DataType.UINT40:
            	long x = 0x000000000000000
                for ( int i = 0; i < 5; i++ )
              		x |= data.get(it+i+2) << 8*i
            	resultMap.put(lbl, x )
                it = it + 7
                break  
            case DataType.UINT64:
            	long x = 0x0000000000000000
                for ( int i = 0; i < 8; i++ )
                	x |= data.get(it+i+2) << 8*i
            	resultMap.put(lbl, x )
                it = it + 10
                break 
            case DataType.INT8:
            	resultMap.put(lbl, (short)(data.get(it+2)))
                it = it + 3
                break
             case DataType.INT16:
            	resultMap.put(lbl, (int)((data.get(it+3)<<8) | data.get(it+2)))
                it = it + 4
                break
            case DataType.FLOAT4:
                int x = 0x00000000 
                for ( int i = 0; i < 4; i++ ) 
                	x |= data.get(it+i+2) << 8*i
                float y = Float.intBitsToFloat(x) 
            	resultMap.put(lbl,y)
                it = it + 6
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