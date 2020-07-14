/**
 *  Copyright 2015 SmartThings
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
 */
metadata {

    definition (name: "Simulated Ring Alexa Contact", namespace: "aonghus-mor", author: "aonghus-mor") {
        capability "Switch"
        capability "Sensor"
        capability "Actuator"
        capability "Contact Sensor"	    		
    }
    
    simulator 
    {
		status "open": "contact:open"
		status "closed": "contact:closed"
	}

    tiles(scale: 2) 
    {
        multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4) 
        {
            tileAttribute("device.contact", key: "PRIMARY_CONTROL") 
            {
                attributeState "open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#00A0DC"
                attributeState "closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#cccccc"
            }
        }
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) 
        {
            state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
        }
        main "switch"
        details(["contact","switch"])
    }
}

def parse(description) {
}

def on() {
    sendEvent(name: "switch", value: "on")
    sendEvent(name: "contact", value: "open")
}

def off() {
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "contact", value: "closed")
}