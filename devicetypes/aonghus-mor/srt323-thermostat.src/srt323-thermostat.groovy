/*
 * SRT323 Thermostat by Aonghus-Mor, based on the SRT321 device driver by MeavyDev.
 */
metadata 
{
	definition (name: "srt323-thermostat", namespace: "aonghus-mor", author: "Aonghus Mor",
                mnmn: "SmartThings", vid: "generic-radiator-thermostat", ocfDeviceType: "oic.d.thermostat") 
    {
    	capability "Actuator"
		capability "Temperature Measurement"
        capability "Thermostat"
		capability "Thermostat Heating Setpoint"
        capability "Thermostat Mode"
        capability "Thermostat Operating State"
		capability "Configuration"
		capability "Polling"
		capability "Sensor"
        capability "Health Check"
        capability "Battery" 
        capability "Refresh"
        		
		// fingerprint deviceId: "0x0800", inClusters: "0x25, 0x31, 0x40, 0x43, 0x70, 0x72, 0x80, 0x84, 0x85, 0x86, 0xef"
		fingerprint deviceId: "0x0800" 
        fingerprint inClusters: "0x72,0x86,0x80,0x84,0x31,0x43,0x85,0x70,0x42,0x40" // added by Aonghus-Mor
        fingerprint inClusters: "0x72,0x86,0x80,0x84,0x31,0x43,0x85,0x70,0x40,0x25"
	}

	// simulator metadata
	simulator 
    {
	}
	
    
    preferences
    {
        section
        {
        	input "userWakeUpInterval", "number", title: "Wakeup interval...", description: "Wake Up Interval (seconds)", defaultValue: 3600, required: false, displayDuringSetup: false
		}
        section
        {
        	input "deltaT", "decimal", title: "Temperature reporting step.", description: "Report Temperature when it changes by this step (0.1 - 10.0 C ).", defaultValue: 1.0, range: "0.1..10.0", required: false, displayDuringSetup: false
        }
        section
        {
        	input "T101", "number", title: "Home Temperature (101):", defaultValue: 21, required: false, displayDuringSetup: false
            input "T102", "number", title: "Away Temperature (102):", defaultValue: 15, required: false, displayDuringSetup: false
            input "T103", "number", title: "Frost Protection Temperature (103):", defaultValue: 5, required: false, displayDuringSetup: false
        }
     }
 }

def parse(String description)
{
//	log.debug "Parse $description"
    // parse codes amended by Aonghus-Mor, 0x42:2 added.
	//def result = zwaveEvent(zwave.parse(description, [0x72:1, 0x86:1, 0x80:1, 0x84:2, 0x31:1, 0x43:1, 0x85:1, 0x70:1, 0xEF:1, 0x40:1, 0x25:1]))
    def result = zwaveEvent(zwave.parse(description, [0x72:1, 0x86:1, 0x80:1, 0x84:2, 0x31:1, 0x43:1, 0x85:1, 0x70:1, 0xEF:1, 0x40:1, 0x42:2, 0x25:1, 0x20:1]))
	if (!result) 
    {
    	log.warn "Parse returned null"
        log.debug "Parse $description"
        log.debug "$cmd"
		return null
	}
    
	//log.debug "Parse returned $result"
	result
}

def installed() 
{
	log.debug "preferences installed"

	state.configNeeded = true
    sendHealthCheckInterval()
}


def updated() 
{
	log.debug "preferences updated"

	state.configNeeded = true
    sendHealthCheckInterval()
}

def sendHealthCheckInterval()
{
    // Device-Watch simply pings if no device events received for 60min(checkInterval)
	sendEvent(name: "checkInterval", value: 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def ping()
{
	log.debug "ping"
    state.refreshNeeded = true
}
/*
def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev1.ThermostatModeSet cmd)
{
	log.debug "ModeSet: $cmd"
    def map = [:]
	switch (cmd.mode) {
		case physicalgraph.zwave.commands.thermostatmodev1.ThermostatModeSet.MODE_OFF:
			map.value = "off"
			break
		case physicalgraph.zwave.commands.thermostatmodev1.ThermostatModeSet.MODE_HEAT:
			map.value = "heat"
	}
	map.name = "thermostatMode"
	createEvent(map)
}
*/

// Event Generation
def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointReport cmd)
{
	//log.debug "SetPointReport: $cmd"
    def map = [:]
	//map.value = cmd.scaledValue.toInteger().toString()
    map.value = cmd.scaledValue.toString()
	map.unit = cmd.scale == 1 ? "F" : "C"
	map.displayed = true
    map.isStateChange = true
    map.name = "heatingSetpoint"
    //map.constraints = [min:5,max:35]
    log.debug "SetPointReport: ${map.value} ${map.unit}"
    //log.debug cmd
	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision
    state.lastReport = (new Date()).time
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv1.SensorMultilevelReport cmd)
{
    def map = [:]
	map.value = cmd.scaledSensorValue.toString()
	map.unit = cmd.scale == 1 ? "F" : "C"
	map.name = "temperature"
    log.debug "Temperature: ${map.value} ${map.unit}"
    state.lastReport = (new Date()).time
	createEvent(map)
}

// Battery powered devices can be configured to periodically wake up and
// check in. They send this command and stay awake long enough to receive
// commands, or until they get a WakeUpNoMoreInformation command that
// instructs them that there are no more commands to receive and they can
// stop listening.
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
		//def map = [name:"thermostatWakeUp", value: "${device.displayName} woke up", isStateChange: true]
        def map = [name:"thermostatWakeUp", value: "", isStateChange: true, descriptionText: "Tasks completed."]   
		def cmds = updateIfNeeded()
        def events = []
        if (cmds.size() > 0)
    	{
    		cmds << "delay 2000"
            //map.name = "thermostatWakeUp"
            //map.value = ""
            //map.isStateChange = true
            // map.descriptionText = "Pending tasks completed."
            events << createEvent(map)
    	}
        //else
        //{
        	//map.descriptionText = "Woke up: nothing to do."
        //}
        //def event = createEvent(map)
        
		cmds << zwave.wakeUpV2.wakeUpNoMoreInformation().format()
        
        log.debug "Wakeup $cmds"
       
        events << response(delayBetween(cmds, 1000))
        events
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) 
{
    //log.debug "BatteryReport: $cmd"
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF) 
    {  // Special value for low battery alert
            map.value = 1
            map.descriptionText = "${device.displayName} has a low battery"
            map.isStateChange = true
    } 
    else 
    {
            map.value = cmd.batteryLevel
            log.debug ("Battery: $cmd.batteryLevel")
    }
    // Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
    state.lastbatt = new Date().time
    state.lastReport = state.lastbatt
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev1.ThermostatModeReport cmd) 
{
	//log.debug "ModeReport: $cmd"
    def map = [:]
	switch (cmd.mode) {
		case physicalgraph.zwave.commands.thermostatmodev1.ThermostatModeReport.MODE_OFF:
			map.value = "off"
			break
		case physicalgraph.zwave.commands.thermostatmodev1.ThermostatModeReport.MODE_HEAT:
			map.value = "heat"
			break
        default:
        	log.debug "Invalid Zwave Mode Event received: $cmd"
	}
	map.name = "thermostatMode"
    log.debug "Mode: ${map.value}"
    state.mode = map.value
    state.lastReport = new Date().time
	createEvent(map)
}

//ThermostatOperatingStateReport added by Aonghus_Mor
// returned by the device when the temperature setting is changed.
// functionality the same as MeavyDev's ThermostatModeReport 
def zwaveEvent(physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport cmd)
{
    def map = [:]
	switch (cmd.operatingState) {
		case physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_IDLE:
			map.value = "idle"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_HEATING:
			map.value = "heating"
			break
        default:
        	log.debug "Invalid Zwave Operating State Event received: $cmd"
	}
	map.name = "thermostatOperatingState"
    map.descriptionText = "Operating state: ${map.value}"
    map.isStateChange = true
    log.debug "Operating State: ${map.value}"
    state.lastReport = new Date().time
	createEvent(map)
}

// ThermostatModeSupportedReport added by Aonghus_Mor
// response to ThermostatModeSupportedGet which is run by 'configure'.
// trivial check that 'heat' is supported.
def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev1.ThermostatModeSupportedReport cmd)
{
	log.debug "Modes Supported: ${cmd}"
    /*
    if ( cmd.heat == true )
    {
    	log.info "Heat capability confirmed."
        return true
    }
    else if ( cmd.off == true )
    {
    	log.info "Off mode capability confirmed"
    }
    else
    {
    	log.warn "Heat capability NOT confirmed.  Something wrong?"
        return false
    }
    */
    state.lastReport = new Date().time
}

//def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointSupportedReport cmd)
//{
//	log.debug cnd
//}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalCapabilitiesReport cmd) 
{
    def map = [ name: "defaultWakeUpInterval", unit: "seconds" ]
	map.value = cmd.defaultWakeUpIntervalSeconds
	map.displayed = false
	state.defaultWakeUpInterval = cmd.defaultWakeUpIntervalSeconds
    state.lastReport = new Date().time
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd)
{
	log.debug "WakeUpIntervalReport: $cmd"
    def map = [ name: "reportedWakeUpInterval", unit: "seconds" ]
	map.value = cmd.seconds
	map.displayed = false
    state.lastReport = new Date().time
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv1.ManufacturerSpecificReport cmd)
{
	state.productId = cmd.productId
    log.debug "Manufacturers Product ID: ${state.productID}"
    state.lastReport = new Date().time
    return true
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd)
{
   //log.debug "${cmd}"
   switch (cmd.parameterNumber )
   {
      case 0x01:
      	def onoff = ( cmd.configurationValue[0] == 0xff ) ? "on" : "off"
        log.debug "Temperature measurement ${onoff}."
        break
      case 0x02: 
      	state.scale = cmd.configurationValue[0]
        log.debug "Temperature scale: ${state.scale} => ${state.scale == 0 ? 'C' : 'F'}"
        break
      case 0x03:
      	double deltaT = cmd.configurationValue[0] * 0.1
        log.debug "Temperature reporting step: ${deltaT}"
        break
      default:
      	log.debug "Please check the parameter number"
   }
   state.lastReport = new Date().time
   return true
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	log.debug "${cmd}"
    Map map = [:]
    map.value = (cmd.value == 0xFF ) ? 'heat' : 'off'
    map.name = 'thermostatMode'
    map.displayed = true
    return createEvent(map) 
}

def zwaveEvent(physicalgraph.zwave.Command cmd) 
{
	log.warn "Unexpected zwave command $cmd"
    def cmds = []
    sendRefresh(cmds)
    delayBetween(cmds,1000)
}

// Command Implementations

def refresh()
{
	log.debug "refresh"
	state.refreshNeeded = true
}

def configure() 
{
	log.debug "configure"
	state.configNeeded = true
    def cmds = []
    sendConfig(cmds)
    delayBetween(cmds,1000)
}

def poll() 
{
	def newtime = new Date().time
    log.debug "poll"
	state.refreshNeeded = state.refreshNeeeded || !state.lastReport || ( newtime - state.lastReport > 2000 * state.wakeUpInterval )
	state.lastReport = newtime
    //refresh()
}

def setHeatingSetpoint(degrees) 
{
    log.debug "Old setpoint: ${device.currentValue('heatingSetpoint')}"
    if ( degrees == state.setpoint )
    {
    	log.debug "Unchanged setpoint, ${degrees}, ignored."
    }
    else
    {
        log.debug "Heating setpoint to be set to: ${degrees}."
        // special codes, 100-103, are converted as below

        // Don't let the setpoint go below the frost protection temperature
        degrees = degrees < state.Tfrost ? Tfrost : degrees
        if ( degrees > 99 ) 
        {
            int degs = degrees.toInteger()
            switch ( degs )
            {
                case 100: 
                    degrees = state.previousSetpoint  // resets to the previous temperature setpoint 
                    break
                case 101: 
                    degrees = state.Thigh  // sets to the temperature corresponding to code 101 in the preferences section of the DH
                    break
                case 102:
                    degrees = state.Tlow  // ditto
                    break
                case 103:
                    degrees = state.Tfrost // ditto
                    break
                default:
                    log.debug "Invalid setpoint: $degrees" // invalid special code. setpoint abandoned 
                    return
            }
        }

		def CorF = (state.scale == 1 ? 'F' : 'C')
        def p = (state.precision == null) ? 1 : state.precision
        def dgs = p > 0 ? degrees.toDouble() : degrees.toInteger()
        state.p = p
        sendEvent(name: 'heatingSetpoint', value: dgs, unit: CorF, displayed: true, isStateChange: true)
        log.trace "setHeatingSetpoint scale: ${CorF} precision: ${p} setpoint: ${dgs}"    
        state.previousSetpoint = state.setpoint
        state.setpoint = degrees 
        state.updateNeeded = true
        // thermostatMode
	}
}

private getStandardDelay() 
{
	1000
}

def updateIfNeeded()
{
	def cmds = []
    
    //log.debug "updateIfNeeded ${state.updateNeeded} ${state.refreshNeeded} ${state.configNeeded}"
    //cmds << zwave.sensorMultilevelV1.sensorMultilevelGet().format() // current temperature
    //cmds << "delay 500"
   
    // Only ask for battery if we haven't had a BatteryReport in a while
    if (!state.lastbatt || (new Date().time) - state.lastbatt > 86400000 ) //24*60*60*1000 
    {
    	log.debug "Getting battery state"
    	cmds << zwave.batteryV1.batteryGet().format()
    }
 	
    if ( state.modeUpdateNeeded )
    {
    	//cmds << zwave.thermostatModeV1.thermostatModeGet().format()
        boolean heatmode = ( state.mode == 'heat' )
        cmds << zwave.basicV1.basicSet(value: heatmode ? 0xFF : 0x00 ).format()
        cmds << zwave.basicV1.basicGet().format()
        state.updateNeeded = heatmode
        state.modeUpdateNeeded = false
    }
    
    if (state.updateNeeded)
    {
        log.debug "Updating setpoint $state.setpoint"
		//sendEvent(name:"SRT321", value: "Updating setpoint $state.convertedDegrees")
        double setpoint = state.setpoint
        cmds << zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointSet.SETPOINT_TYPE_HEATING_1, 
        														scale: state.scale, 
                                                                precision: state.p, 
                                                                scaledValue: setpoint).format()
        if ( !state.refreshNeeded )
        	cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointSet.SETPOINT_TYPE_HEATING_1).format()
   		state.updateNeeded = false
   }
    if (state.refreshNeeded)
    {
        log.debug "Refresh"
        sendRefresh(cmds)
       	state.refreshNeeded = false
    }
    
    if (state.configNeeded)
    {
        log.debug "Config"
        sendConfig(cmds)
        state.configNeeded = false
    }
    
    cmds
}

private sendConfig(cmds)
{
    //def setpointRange = [4,105]
    //sendEvent(name: "heatingSetpointRange", value: setpointRange, displayed: false, isStateChange: true)
    // Nodes controlled by Thermostat Mode Set - not sure this is needed?
    cmds << zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()

    // Set hub to get battery reports / warnings
    cmds << zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()

    // Set hub to get set point reports
    cmds << zwave.associationV1.associationSet(groupingIdentifier:4, nodeId:[zwaveHubNodeId]).format()

    // Set hub to get multi-level sensor reports (defaults to temperature changes of > 1C)
    cmds << zwave.associationV1.associationSet(groupingIdentifier:5, nodeId:[zwaveHubNodeId]).format()

    // set the configuration parameters
    // log.debug userWakeUpInterval + " " + deltaT + " " + T101 + " " + T102 + " " + T103
    state.Thigh  = T101 ? T101 : 21
    state.Tlow   = T102 ? T102 : 15
    state.Tfrost = T103 ? T103 : 05
    // set the temperature sensor On
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0xff], parameterNumber: 1, size: 1).format()
    // set the temperature scale, "C" or "F"
    short Tscale = ( location.temperatureScale == "C" ) ? 0x00 : 0xff
    state.scale = ( Tscale == 0x00 ) ? 0 : 1
    cmds << zwave.configurationV1.configurationSet(configurationValue: [Tscale], parameterNumber: 2, size: 1).format()
	setThermostatMode('heat')
    // set temperature reporting step.
    short dT = deltaT ? (10 * deltaT) : 0x0A
    if ( dT != state.deltaT )
    {
    	state.deltaT = dT
    	//state.deltaTemperature = [dT]
    	log.debug "Delta T changed to ${dT}"
    }
    cmds << zwave.configurationV1.configurationSet(configurationValue: [dT], parameterNumber: 3, size: 1).format()
    
    def userWake = getUserWakeUp(userWakeUpInterval)
    // If user has changed userWakeUpInterval, send the new interval to the device 
    if (state.wakeUpInterval != userWake)
    {
        state.wakeUpInterval = userWake
        log.debug "Setting New WakeUp Interval to: " + state.wakeUpInterval
        cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds:state.wakeUpInterval, nodeid:zwaveHubNodeId).format()
        cmds << zwave.wakeUpV2.wakeUpIntervalGet().format()
    }  
    cmds << zwave.thermostatModeV1.thermostatModeSupportedGet().format()
    cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
}

private sendRefresh(cmds)
{
	cmds << zwave.sensorMultilevelV1.sensorMultilevelGet().format()
    cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointSet.SETPOINT_TYPE_HEATING_1).format()
		//
	//cmds << zwave.thermostatModeV1.thermostatModeGet().format()
	cmds << zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format()
}

private getUserWakeUp(userWake) 
{
    if (!userWake)  
    { 
    	userWake = '3600' // set default 1 hr if no user preference 
    } 
    // make sure user setting is within valid range for device 
    if (userWake.toInteger() < 60)
    { 
    	userWake = '600'   // 10 minutes - Mininum
    }
    if (userWake.toInteger() > 36000)
    {
    	userWake = '36000' // 10 hours - Maximum
    }  
    return userWake.toInteger()
}

def heat()
{
    log.debug "heat()"
    setThermostatMode('heat')
}

def off()
{
	log.debug "off()"
    setThermostatMode('off')
}

def setThermostatMode(mode)
{
	log.debug "setThermostatMode to ${mode} from ${state.mode}"
    if ( state.mode == mode )
    	state.modeUpdateNeeded = false
    else
    {
    	state.mode = mode
    	state.modeUpdateNeeded = true
    }
    sendEvent(name: 'thermostatMode', value: state.mode, displayed: true)
}