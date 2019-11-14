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
 *  20.06.2019 modified by @aonghus-mor to correctly display the temperature, react properly to button 'hold' and to detect both buttons pressed simulataneously. 
 *  13.08.2019 modified by @aonghus-mor to recognize whether each switch is wired and to allow the unwired switch to behave as a button or toggle
 *  12.10.2019 modified by @aonghus-mor to reorganise the main app screen to make each switch have equal weight.  Added button2 as a response to both switches being pressed simultaneously,
 */
 
import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata {
    //definition (name: "Aqara 2 Button Wired Wall Switch No Neutral", namespace: "simic", author: "simic") 
    definition (name: "Aqara 2 Button Wired Wall Switch No Neutral", namespace: "aonghus-mor", author: "aonghus-mor")
    {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Temperature Measurement"
        capability "Button"
        capability "Momentary"
        
        command "on2"
        command "off2"
        command "on1"
        command "off1"
        command "buttonpush"
        command "doButtonPush"
        command "leftButtonPush"
        command "rightButtonPush"
        
        attribute "switch1","ENUM", ["on","off"]
        attribute "switch2","ENUM", ["on","off"]
        attribute "lastCheckin", "string"
        attribute "lastPressType", "enum", ["soft","hard","both","held","released","refresh"]
        
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0001,0002,0003,0004,0005,0006,0010,000A", outClusters: "0019,000A", manufacturer: "LUMI", model: "lumi.ctrl_neutral2", deviceJoinName: "Aqara Switch QBKG03LM"
    }

    // simulator metadata
    /*
    simulator {
        // status messages
        status "on": "on/off: 1"
        status "off": "on/off: 0"

        // reply messages
        reply "zcl on-off on": "on/off: 1"
        reply "zcl on-off off": "on/off: 0"
    }
	*/
    tiles(scale: 2) {
    	/*
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: false){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") 
            { 
                attributeState("lefton", label:'Left On', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningLeftOff")
                attributeState("leftoff", label:'Left Off', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningLeftOn")
                attributeState("turningLeftOn", label:'Turning Left On', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"lefton")
                attributeState("turningLeftOff", label:'Turning Left Off', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"leftoff")
                attributeState("righton", label:'Right On', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningRightOff")
                attributeState("rightoff", label:'Right Off', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningRightOn")
                attributeState("turningRightOn", label:'Turning Right On', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"righton")
                attributeState("turningRightOff", label:'Turning Right Off', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"rightoff")
                attributeState("leftheld", label:'Left held', backgroundColor:"#ff0000", 
                				icon:'https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png')
                attributeState("rightheld", label:'Right Held', backgroundColor:"#ff0000", 
                				icon:'https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png')
               	attributeState("leftpushed", label:'Left Pushed', backgroundColor:"#008800", 
                				icon:'https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png')
                attributeState("rightpushed", label:'Right Pushed', backgroundColor:"#008800", 
                				icon:'https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png')
            	attributeState("leftreleased", label:'Left Released', action: "leftButtonPush", backgroundColor:"#ffffff", 
                				icon:'https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonReleased.png')
            	attributeState("rightreleased", label:'Right Released', action: "rightButtonPush", backgroundColor:"#ffffff", 
                				icon:'https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonReleased.png')    
            }
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") 
            {
    			attributeState("default", label:'Last Update: ${currentValue}',icon: "st.Health & Wellness.health9")
		   	}
        }
        */   
        
        standardTile("switch1", "device.switch1", width: 3, height: 3, canChangeIcon: false) 
        {
			state "off", label: '${name}', action: "on1", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
			state "on", label: '${name}', action: "off1", icon: "st.switches.light.on", backgroundColor: "#79b821", nextState: "turningOff"
            state "turningOn", label:'${name}', action:"off1", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"on"
            state "turningOff", label:'${name}', action:"on1", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"off"
            state "held", label:'${name}', backgroundColor:"#ff0000",
            		icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png"
            state "released", label:'${name}', action:"leftButtonPush", backgroundColor:"#ffffff", nextState: "pushed",
            		icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonReleased.png"
            state "pushed", label:'${name}', backgroundColor:"#008800", 
            		icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png"	
		}
        
        standardTile("switch2", "device.switch2", width: 3, height: 3, canChangeIcon: false) 
        {
			state "off", label: '${name}', action: "on2", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
			state "on", label: '${name}', action: "off2", icon: "st.switches.light.on", backgroundColor: "#79b821", nextState: "turningOff"
            state "turningOn", label:'${name}', action:"off2", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"on"
            state "turningOff", label:'${name}', action:"on2", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"off"
            state "held", label:'${name}', backgroundColor:"#ff0000",
            		icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png"
            state "released", label:'${name}', action: "rightButtonPush", backgroundColor:"#ffffff", nextState: "pushed",
            		icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonReleased.png"
            state "pushed", label:'${name}', backgroundColor:"#008800", 
            		icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png"	
		}

        valueTile("temperature", "device.temperature", width: 2, height: 2) 
        {
			state("temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"],
                    [value: 0, color: "#153591"],
					[value: 7, color: "#1e9cbb"],
					[value: 15, color: "#90d2a7"],
					[value: 18, color: "#44b621"],
					[value: 21, color: "#f1d801"],
					[value: 24, color: "#d04e00"],
					[value: 27, color: "#bc2323"]
				]
			)
		}
        /*
        valueTile("lastPressType","device.lastPressType", width: 2, height: 2)
        {
        	state "lastPressType", label: '${currentValue}'
        }
        */
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) 
        {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        valueTile("spacer1", "spacer1", decoration: "flat", inactiveLabel: false, width: 1, height: 2) {state "default", label:''}
        valueTile("spacer2", "spacer2", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {state "default", label:''}
        valueTile("lastcheckin", "device.lastCheckin", inactiveLabel: false, decoration:"flat", width: 4, height: 2) {
            state "lastcheckin", label:'Last Event:\n ${currentValue}'
        }
        /*
        if ( state != null && state.batteryPresent )
        {
         	valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) 
        	{
      			state "battery", label:'${currentValue}% battery'
    		}
        }
        */
        
        /*
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) 
        {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        */	
        
        //main ("switch")
        details([/*"switch",*/ "switch1", "switch2", "temperature", "spacer2", "refresh", "spacer1", "lastcheckin", "spacer1" /*, "battery"*/])
    }
    preferences 
    {
    	input name: "unwiredSwitch", type: "enum", options: ['None', 'Left', 'Right'], title: "Identify the unwired switch", 
        							required: true, displayDuringSetup: true
        input name: "tempOffset", type: "decimal", title:"Temperature Offset", description:"Adjust temperature by this many degrees", range:"*..*", defaultValue: 0                          
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
    if ( diffcheck > 2000 ) // if the state has not been resolved after 2 seconds, clear the flags
    {
    	if ( state.flag == null && ( state.sw1 != null || state.sw2 != null ) )
        	state.flag = 'soft'
        else
        	clearFlags()
    }
    state.lastCheckTime = newcheck
    displayDebugLog( "(parse)flags: " + showFlags() )
  
   	def events = []
   
   	if (description?.startsWith('catchall:')) 
		events << parseCatchAllMessage(description)
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
	return state.flag + " " + state.sw1 + " " + state.sw2 + " " + state.lastCheckTime
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
            	events << createEvent(name: 'switch1', value: 'held' )
            	if ( state.unwired == "Left" )
                	events << createEvent(name: 'button', value: 'held', data:[buttonNumber: 1], isStateChange: true)
                displayInfoLog('Left switch held.')
            }
            else if ( state.sw2 != null )
            {
            	events << createEvent(name: 'switch2', value: 'held' )
                if ( state.unwired == "Right" )
                	events << createEvent(name: 'button', value: 'held', data:[buttonNumber: 1], isStateChange: true)
                displayInfoLog('Right switch held.')
            }
           	lastPress = "held"
            makeEvent = false
            break
         case "released":
         	if ( state.sw1 != null )
            {
            	events << createEvent(name: 'switch1', value: 'released' )
    		}
            else if ( state.sw2 != null )
            {
                events << createEvent(name: 'switch2', value: 'released' )
    		}
            lastPress = "released"
            clearFlags()
            break
         case "both":
         	events << createEvent(name: 'button', value: 'pushed', data:[buttonNumber: 2], isStateChange: true)
         case "refresh":
         case "soft":
         case "hard":
            if ( state.sw1 != null )
            {
            	if ( state.unwired == null )
                	state.unwired = unwiredSwitch
                if ( state.unwired == 'Left' )
                {
                	displayDebugLog( "sending button pushed event from switch 1" )
                    doButtonPush('switch1')
                    runIn(1, clearLeftButtonStatus)
                }
                else
    			{
                	events << createEvent(name: 'switch1', value: state.sw1 )
                }
                displayInfoLog('Left switch pushed.')
            }
            else if ( state.sw2 != null )
            {
            	if ( state.unwired == 'Right' )
    			{
                	displayDebugLog( "sending button pushed event from switch 2" )
                    doButtonPush('switch2')
                    runIn(1, clearRightButtonStatus)
                } 
                else
                {
                	events << createEvent(name: 'switch2', value: state.sw2 )
                }	
                displayInfoLog('Right switch pushed.')
            }
            else
            	makeEvent = false
            if ( makeEvent )
            {
            	lastPress = state.flag
                displayDebugLog( "clearing flags" )
            	clearFlags()
            }
            break
        default:
        	break
    }
    if ( lastPress != null )
    	state.lastPressType = lastPress
  
  	events
}

def clearButtonStatus()
{
	if ( state.unwired == 'Left' )
    	clearLeftButtonStatus()
    else if ( state.unwired == 'Right' )
    	clearRightButtonStatus()
}

def clearLeftButtonStatus()
{
	displayDebugLog( "Clearing Left Button Status" )
	sendEvent(name: 'switch1', value: 'released', isStateChange: true)
    clearFlags()
}

def clearRightButtonStatus()
{
    displayDebugLog( "Clearing Right Button Status" )
    sendEvent(name: 'switch2', value: 'released', isStateChange: true)
    clearFlags()
}

def buttonpush()
{
	 displayDebugLog("button pushed " + showFlags())
     	if ( state.unwired == 'Left' )
    		leftButtonPush()
    	else if ( state.unwired == 'Right' )
    		rightButtonPush()
}    

def doButtonPush(sw)
{
	sendEvent(name: sw, value: 'pushed', isStateChange: true)
    sendEvent(name: 'button', value: 'pushed', data:[buttonNumber: 1], isStateChange: true)
}

def leftButtonPush()
{
	 displayDebugLog("left button pushed " + showFlags())
     doButtonPush('switch1')
     state.lastPressType = "soft"
	 runIn(1, clearLeftButtonStatus)
}   

def rightButtonPush()
{
	 displayDebugLog("right button pushed " + showFlags())
     doButtonPush('switch2')
     state.lastPressType = "soft"
	 runIn(1, clearRightButtonStatus)
}  

private def clearFlags()
{
	state.flag = null
    state.sw1 = null
    state.sw2 = null
    displayDebugLog("Flags cleared.")
}

private def parseCatchAllMessage(String description) 
{
	def cluster = zigbee.parse(description)
	displayDebugLog( cluster )
    def event = null
    
    switch ( cluster.clusterId ) 
    {
    	case 0x0000: 
         	if ( cluster.command == 0x0a && cluster.data[0] == 0x01 )
            {
        		Map dtMap = dataMap(cluster.data)
                displayDebugLog( "Map: " + dtMap )
                if ( state.unwired == null )
                	state.unwired = unwiredSwitch
                if ( dtMap.get(110) == 0x0002 )
                	state.unwired = 'Left'
                if ( dtMap.get(111) == 0x0002)
                	state.unwired = 'Right'
                if ( state.unwired != 'None' && !state.numButtons )
                {
                	state.numButtons = 2
                    sendEvent(name: 'numberOfButtons', value: state.numButtons, displayed: false )
      				displayDebugLog( "Setting Number of Buttons to " + state.numButtons )
                }
                def temp = dtMap.get(3)
                if ( state.tempNow != temp )
                {
                	state.tempNow = temp
                    if ( getTemperatureScale() != "C" ) 
                    	temp = celsiusToFahrenheit(temp)
                	state.tempNow2 = temp + tempOffset      
                	event = createEvent(name: "temperature", value: state.tempNow2, unit: getTemperatureScale())
                    displayDebugLog("Temperature updated.")
            	}
                displayInfoLog( "Unwired Switch is ${state.unwired}" )
                displayInfoLog( "Switches are (" + (dtMap.get(100) ? "on" : "off") + "," + (dtMap.get(101) ? "on" : "off") +")" )
                displayInfoLog( "Temperature is now ${state.tempNow2}°" )
            }
        	break
        case 0x0006: 	
        	def onoff = cluster.data[0] == 0x01 ? "on" : "off"
        	switch ( cluster.sourceEndpoint) 
            {
        		case 0x02:
                case 0x03:
                case 0x04:
                case 0x05:
                	state.flag = "soft"
                    break
                default:
                	displayDebugLog( "Unknown SourceEndpoint: $cluster.sourceEndpoint" )
    		}
    }
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
        	if ( descMap.attrId == "0000") 
            {
    			def temp = convertHexToInt(descMap.value)
        		if ( getTemperatureScale() != "C" ) temp = celsiusToFahrenheit(temp)
				event = createEvent(name: "temperature", value: temp, unit: getTemperatureScale())
				displayDebugLog( "Temperature Hex convert to ${temp}°" )
                state.lastTempTime = (new Date()).time
                state.tempCheck = state.tempCheck == null ? true : ( state.tempNow == temp ) && state.tempCheck
                displayDebugLog( "Temperature Check: ${state.tempCheck}" )
            }
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
	switch ( descMap.endpoint )
    {
    	case "02":
            state.sw1 = onoff
            break
        case "03":
        	state.sw2 = onoff
        	break
        case "04": // button 1 pressed
        case "05": // button 2 pressed
        	state.flag = "hard"
            break
        case "06": // both buttons pressed
        	state.flag = "both"
		break
        default:
        	displayDebugLog( "ClusterID 0x0006 with unknown endpoint $descMap.endpoint" )
     }
     displayDebugLog("$descMap.endpoint " + showFlags())
}

private def parseCustomMessage(String description) 
{
	 displayDebugLog( "Parsing Custom Message: $description" )
	if (description?.startsWith('on/off: ')) {
    	if (description == 'on/off: 0')
        	state.flag = "held"
        else if (description == 'on/off: 1')
        	state.flag = "released"
	}
}

def on() 
{
    def cmd = on()
    cmd
}

def on1() 
{
    displayDebugLog( "on1()" )
	if ( state.unwired == 'Left' )
    {	
    	leftButtonPush()
        return []
    }
    sendEvent(name: "switch1", value: "on")
	def cmd = zigbee.command(0x0006, 0x01, "", [destEndpoint: 0x02] )
    displayDebugLog( cmd )
    cmd 
}

def on2() {
   	displayDebugLog( "on2()" )
	if ( state.unwired == 'Right' )
    {	leftButtonPush()
        return []
    }
    sendEvent(name: "switch2", value: "on")
	//"st cmd 0x${device.deviceNetworkId} 3 6 1 {}"
    def cmd = zigbee.command(0x0006, 0x01, "",[destEndpoint: 0x03] )
    displayDebugLog( cmd )
    cmd
}

def off() 
{
	def cmd = off1()
    cmd
}

def off1() 
{
	displayDebugLog( "off1()" )
	if ( state.unwired == 'Left' )
    {	
    	leftButtonPush()
        return []
    }
    sendEvent(name: "switch1", value: "off")
    def cmd = zigbee.command(0x0006, 0x00, "", [destEndpoint: 0x02] )
    displayDebugLog( cmd )
    cmd
}

def off2() {
    displayDebugLog( "off2()" )
	if ( state.unwired == 'Right' )
    {	leftButtonPush()
        return []
    }
    sendEvent(name: "switch2", value: "off")
	//"st cmd 0x${device.deviceNetworkId} 3 6 0 {}"
    def cmd = zigbee.command(0x0006, 0x00, "", [destEndpoint: 0x03] )
    displayDebugLog( cmd )
    cmd
}

def refresh() {
	displayInfoLog( "refreshing" )
    clearFlags()
    state.flag = "refresh"
    def dat = new Date()
    state.lastTempTime = dat.time
    state.unwired = unwiredSwitch
    state.tempNow = state.tempNow == null ? 0 : state.tempNow
    state.tempNow2 = state.tempNow2 == null ? 0 : state.tempNow2
    //def cmds = zigbee.configureReporting(0x0002, 0x0000, 0x29, 1800, 7200, 0x01)
    def cmds = zigbee.readAttribute(0x0006,0,[destEndpoint: 0x01] ) + 
    			zigbee.readAttribute(0x0006,0,[destEndpoint: 0x02] ) + 
             	zigbee.readAttribute(0x0006,0,[destEndpoint: 0x03] ) +
        		zigbee.readAttribute(0x0006,0,[destEndpoint: 0x04] ) + 
             	zigbee.readAttribute(0x0006,0,[destEndpoint: 0x05] )
        
    cmds += zigbee.readAttribute(0x0002, 0) + 
           zigbee.readAttribute(0x0000, 0x0007)
    //if ( state.batteryPresent )
    	cmds += zigbee.readAttribute(0x0001, 0) //+ zigbee.readAttribute(0x0001,0x0001) + zigbee.readAttribute(0x0001,0x0002)
                
     //cmds += zigbee.configureReporting(0x0000, 0, 0x29, 1800,7200,0x01)
    
     displayDebugLog( cmds )
     
     cmds
}

private Integer convertHexToInt(hex) {
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

private def displayInfoLog(message) {
	//if (infoLogging || state.prefsSetCount < 3)
    if (infoLogging)
		log.info "${device.displayName} ${message}"
}