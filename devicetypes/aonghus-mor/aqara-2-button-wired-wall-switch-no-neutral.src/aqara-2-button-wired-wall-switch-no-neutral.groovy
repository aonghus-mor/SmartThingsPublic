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
 */

metadata {
    //definition (name: "Aqara 2 Button Wired Wall Switch No Neutral", namespace: "simic", author: "simic") 
    definition (name: "Aqara 2 Button Wired Wall Switch No Neutral", namespace: "aonghus-mor", author: "aonghus-mor")
    {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Temperature Measurement"
        
        command "on2"
        command "off2"
        command "on1"
        command "off1"
        
        attribute "switch2","ENUM", ["on","off"]
        
        attribute "lastCheckin", "string"
        
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0001,0002,0003,0004,0005,0006,0010,000A", outClusters: "0019,000A", manufacturer: "LUMI", model: "lumi.ctrl_neutral2", deviceJoinName: "Aqara Switch QBKG03LM"
    }

    // simulator metadata
    simulator {
        // status messages
        status "on": "on/off: 1"
        status "off": "on/off: 0"

        // reply messages
        reply "zcl on-off on": "on/off: 1"
        reply "zcl on-off off": "on/off: 0"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") { 
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"On"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"Off"
                attributeState "held", label:'${name}', action: "switch.off", icon:"st.switches.light.on", backgroundColor:"#ff0000"
            }
            
            
            
           	tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'Last Update: ${currentValue}',icon: "st.Health & Wellness.health9")
		   	}
        }
        
        standardTile("switch2", "device.switch2", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${name}', action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821"
            state "held", label:'${name}', action: "off2", icon:"st.switches.light.on", backgroundColor:"#ff0000"
		}

        valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
			)
		}
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        if ( state != null && state.batteryPresent )
        {
         	valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) 
            {
      			state "battery", label:'${currentValue}% battery'
    		}
        }
        /*
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) 
        {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        */
        main (["switch", "switch2", "temperature"])
        details(["switch", "switch2", "temperature", "refresh", "battery"])
    }
}

// Parse incoming device messages to generate events
def parse(String description)
{
   	log.debug "Parsing '${description}'"
   	log.debug "(parse)state.code: " + hexString(state.code)
  
   	def event 
   
   	if (description?.startsWith('catchall:')) 
   	{
		event = parseCatchAllMessage(description)
	}
	else if (description?.startsWith('read attr -')) 
    {
		event = parseReportAttributeMessage(description)
	}
    else if (description?.startsWith('on/off: '))
    {
        parseCustomMessage(description) 
    }
	if ( state.code > 0x0000 )
    	event = parseStateCode()
    
    //  send event for heartbeat
    //def cmds = [results]
    def dat = new Date()
    def now = dat.format("HH:mm:ss EEE dd MMM '('zzz')'", location.timeZone)
    sendEvent(name: "lastCheckin", value: now, descriptionText: "Check-In")
   
    log.debug "Parse returned: $event"
    return event
}

def updateTemp()
{
	// every half hour get the temperature
    def dat = new Date()
    def cmd = null
    if ( dat.time - state.lastTempTime > 1800000 ) {
    	log.debug "Requesting Temperature"
        state.lastTempTime = dat.time
        //state.gettingTemp = true
        cmd = [response(delayBetween(zigbee.readAttribute(0x0002,0),1000))]
    }
	return cmd
}

private def parseStateCode()
{
	// state.code is a binary pattern where the bits are defined as follows
    //
    // 0x0001 (0x0010) switch is on for sitch 1 (2)
    // 0x0002 (0x0020) current event refers to switch 1 (2)
    // 0x0004 (0x0040) current event from 'catchall' message on endpoint 0x02 (0x03)
    // 0x0008 (0x0080) current event from 'read attr' message on endpoint 0x04 (0x05)
    // 0x0100 current event both switches pressed
    // 0x0200 current event from refresh
    // 0x0800 / 0x0C00 current event switch held / released
    //
    def events = []
    log.debug "parsing state code: " + hexString(state.code)
    def codeList = [0x0006, 0x000A, 0x0060, 0x00A0, 0x0122, 0x0222, 0x0244, 0x0802, 0x0820, 0x0C02, 0x0C20 ]
    if ( codeList.contains( state.code & 0xFFEE ) )
    {
    	if ( state.code & 0x0800 )  // button held
        {
        	if ( state.code & 0x0400 )
        	{
        		if ( state.code & 0x0002 )
    				events << createEvent(name: 'switch', value: 'off' )
    			else if ( state.code & 0x0020 )
    				events << createEvent(name: 'switch2', value: 'off' )
                state.code = 0x0000
            }
            else
            {
            	if ( state.code & 0x0002 )
    				events << createEvent(name: 'switch', value: 'held' )
    			else if ( state.code & 0x0020 )
    				events << createEvent(name: 'switch2', value: 'held' )
            }
        }
        else
        {
			if ( state.code & 0x0006 )
    			events << createEvent(name: 'switch', value: (state.code & 0x0001) ? "on" : "off" )
    		if ( state.code & 0x0060 )
    			events << createEvent(name: 'switch2', value: (state.code & 0x0010) ? "on" : "off" ) 
            state.code = 0x0000
        }
    	
    }
    return events
}

private def parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)
	log.debug cluster
    def event = null
    
    switch ( cluster.clusterId ) 
    {
    	case 0x0000: 
         	//state.code = 0x0000
        	event = updateTemp()
        	break
        case 0x0006: 	
        	def onoff = cluster.data[0] == 0x01
        	switch ( cluster.sourceEndpoint) 
            {
        		case 0x02:
                    state.code = state.code | ( 0x0004 | ( onoff ? 0x0001 : 0x0000 ) )
                    break
                case 0x04:
                	state.code = state.code | 0x0004
                    break
                case 0x03:
                	state.code = state.code | (0x0040 | ( onoff ? 0x0010 : 0x0000 ) )
                	break
                case 0x05:
                	state.code = state.code | 0x0040
                    break
                default:
                	log.debug "Unknown SourceEndpoint: $cluster.sourceEndpoint"
    		}
            //log.debug "$cluster.sourceEndpoint  $state.code"
     }
     return event
}

private def parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
     }
	//log.debug "Desc Map: $descMap"
 
	Map resultMap = [:]
    
    switch (descMap.cluster) {
    	case "0000":
        	log.debug "Basic Cluster: $descMap"
            break
    	case "0001": //battery
        	if ( descMap.value == "0000" )
            	state.batteryPresent = false
        	else if (descMap.attrId == "0020")
				resultMap = getBatteryResult(convertHexToInt(descMap.value / 2))
            break
 		case "0002": // temperature
        	if ( descMap.attrId == "0000") {
    			def temp = convertHexToInt(descMap.value)
        		if ( getTemperatureScale() != "C" ) temp = celsiusToFahrenheit(temp)
				//resultMap = createEvent(name: "temperature", value: zigbee.parseHATemperatureValue("temperature: " + (convertHexToInt(descMap.value) / 2), "temperature: ", getTemperatureScale()), unit: getTemperatureScale())
				resultMap = createEvent(name: "temperature", value: temp, unit: getTemperatureScale())
				log.debug "Temperature Hex convert to ${resultMap.value}°"
                state.lastTempTime = (new Date()).time
            }
            break
 		case "0006":  button press
        	//resultMap = 
            parseSwitchOnOff(descMap)
            break
 		case "0008":
        	if ( descMap.attrId == "0000") {
    			resultMap = createEvent(name: "switch", value: "off")
			}
            break
 		default:
        	log.debug "unknown cluster in $descMap"
    }
	return ResultMap
	//return createEvent(resultMap)
}

def parseSwitchOnOff(Map descMap)
{
	//parse messages on read attr cluster 0x0006
	def onoff = descMap.value[-1] == "1"
	switch ( descMap.endpoint )
    {
    	case "02":
        	state.code = state.code | (0x0002 | ( onoff ? 0x0001 : 0x0000 ) )
            break
        case "03":
        	state.code = state.code | (0x0020 | ( onoff ? 0x0010 : 0x0000 ) )
        	break
        case "04": // button 1 pressed
        	state.code = state.code | 0x0008
            break
        case "05": // button 2 pressed
        	state.code = state.code | 0x0080
            break
        case "06": // botyh buttons pressed
        	state.code = state.code | 0x0100
            break
        default:
        	log.debug "ClusterID 0x0006 with unknown endpoint $descMap.endpoint"
     }
     //log.debug "$descMap.endpoint  $state.code"
}

private def parseCustomMessage(String description) {
	//def result
    log.debug "Parsing Custom Message: $description"
	if (description?.startsWith('on/off: ')) {
    	if (description == 'on/off: 0')
        	state.code = state.code | 0x0800
    		//result = createEvent(name: "switch", value: "off")
    	else if (description == 'on/off: 1')
        	state.code = state.code | 0x0C00
    		//result = createEvent(name: "switch", value: "on")
	}
    
    //return result
}

def off() {
    log.debug "off()"
	sendEvent(name: "switch", value: "off")
	//"st cmd 0x${device.deviceNetworkId} 2 6 0 {}"
    //def cmd = zigbee.off()
    def cmd = zigbee.command(0x0006, 0x00, "", [destEndpoint: 0x02] )
    log.debug cmd
    cmd
}

def on() {
   log.debug "on()"
	sendEvent(name: "switch", value: "on")
	//"st cmd 0x${device.deviceNetworkId} 2 6 1 {}"
    //def cmd = zigbee.on()
     def cmd = zigbee.command(0x0006, 0x01, "", [destEndpoint: 0x02] )
    log.debug cmd
    cmd
}

def on1() {
	log.debug "on1()"
    sendEvent(name: "switch", value: "on")
    //zigbee.on()
    zigbee.command(0x0006, 0x01, "", [destEndpoint: 0x02] )
}

def off1() {
	log.debug "off1()"
    sendEvent(name: "switch", value: "off")
    //zigbee.off()
    zigbee.command(0x0006, 0x00, "", [destEndpoint: 0x02] )
}

def off2() {
    log.debug "off2()"
	sendEvent(name: "switch2", value: "off")
	//"st cmd 0x${device.deviceNetworkId} 3 6 0 {}"
    def cmd = zigbee.command(0x0006, 0x00, "", [destEndpoint: 0x03] )
    log.debug cmd
    cmd
}

def on2() {
   log.debug "on2()"
	sendEvent(name: "switch2", value: "on")
	//"st cmd 0x${device.deviceNetworkId} 3 6 1 {}"
    def cmd = zigbee.command(0x0006, 0x01, "",[destEndpoint: 0x03] )
    log.debug cmd
    cmd
}

def refresh() {
	log.debug "refreshing"
  	state.code = 0x0200
    //def cmds = zigbee.configureReporting(0x0002, 0x0000, 0x29, 1800, 7200, 0x01)
    def cmds = zigbee.readAttribute(0x0006,0,[destEndpoint: 0x02] ) + 
             	zigbee.readAttribute(0x0006,0,[destEndpoint: 0x03] )
        
        
    cmds += zigbee.readAttribute(0x0002, 0) + 
           zigbee.readAttribute(0x0000, 0)
    //if ( state.batteryPresent )
    	cmds += zigbee.readAttribute(0x0001, 0)
                
     //cmds += zigbee.configureReporting(0x0000, 0, 0x29, 1800,7200,0x01)
     log.debug cmds
     
     cmds
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}
