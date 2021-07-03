metadata {
	definition (	name: "Aqara Wall Switch Child", namespace: "aonghus-mor", author: "aonghus-mor", 
                	mnmn: "SmartThingsCommunity",
                	vid: "bfddb57e-f398-3d50-b994-fa3754eb4da6", // switch only
                	ocfDeviceType: "oic.d.switch"
                	//vid: "ca0b4663-15bd-3afe-bb35-60224cea7d32", //button only
    				//ocfDeviceType: "x.com.st.d.remotecontroller"
                	//vid: "ba059eea-2860-33b2-92c0-3029ca8a9101", //switch & button
    			) 
    {
		capability "Switch"
        capability "Button"
        capability "Momentary"
		//capability "Actuator"
		//capability "Sensor"
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
    if ( unwired )
    	sendEvent( name: 'switch', value: 'off' )
	else
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
    //endEvent(name: 'supportedButtonValues', value: ["down","down_hold","down_2x"].encodeAsJSON(), isStateChange: true)
    sendEvent(name: 'numberOfButtons', value: 1, displayed: false )
	parent.childRefresh(device.deviceNetworkId, settings)
}

void updated()
{
	parent.childRefresh(device.deviceNetworkId, settings)
}
