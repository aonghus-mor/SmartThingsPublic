metadata 
{
	definition (	name: "Aqara Wall Switch Child", namespace: "aonghus-mor", author: "aonghus-mor", 
                	mnmn: "SmartThingsCommunity",
                	vid: "c4129d8e-95d1-3d09-a481-5b685ca134aa", // switch only (for wired) 
                	ocfDeviceType: "oic.d.switch"
                	//vid: "052c5cad-cf03-3d0c-a0c4-744d2ce6f59a", //button only (for unwired)
    				//ocfDeviceType: "x.com.st.d.remotecontroller"
    			) 
    {
		capability "Switch"
        capability "Button"
        capability "Momentary"
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
