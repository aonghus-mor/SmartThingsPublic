metadata {
	definition (name: "Aqara Wired Wall Switch Child", namespace: "aonghus-mor", author: "aonghus-mor", 
    			//vid:"generic-switch-power-energy"
                //vid: "c87da4b9-8640-3490-9a02-77c16810131f",
                vid: "ba059eea-2860-33b2-92c0-3029ca8a9101",
    			mnmn: "SmartThingsCommunity"
    			) 
    {
		capability "Switch"
        capability "Button"
        capability "Momentary"
		capability "Actuator"
		capability "Sensor"
        capability "Refresh"
        capability "Health Check"
        
        preferences
        {
         	input name: "unwired", type: "bool", title: "This switch is unwired?", required: true, displayDuringSetup: true
            input name: "decoupled", type: "bool", title: "Decoupled?", required: true, displayDuringSetup: true
        }
	}
}

void on() 
{
    if ( !decoupled )
    	sendEvent( name: 'button', value: 'pushed', data:[buttonNumber: 1], isStateChange: true)
    if ( unwired )
		sendEvent( name: 'switch', value: 'off' )
	else
    	parent.childOn(device.deviceNetworkId)
}

void off() 
{
	if ( !decoupled )
    	sendEvent( name: 'button', value: 'pushed', data:[buttonNumber: 1], isStateChange: true)
    if ( !unwired )
    	parent.childOff(device.deviceNetworkId)
}

void push()
{
	sendEvent( name: 'button', value: 'pushed', data:[buttonNumber: 1], isStateChange: true)
    if ( !decoupled )
    	parent.childToggle(device.deviceNetworkId)
}

void refresh() 
{
	sendEvent(name: 'supportedButtonValues', value: ['pushed', 'held', 'double'], isStateChange: true)
    sendEvent(name: 'numberOfButtons', value: 1, displayed: false )
	parent.childRefresh(device.deviceNetworkId, settings)
}

void updated()
{
	parent.childRefresh(device.deviceNetworkId, settings)
}
