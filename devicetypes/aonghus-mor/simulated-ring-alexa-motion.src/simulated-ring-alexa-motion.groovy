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

    definition (name: "Simulated Ring Alexa Motion", namespace: "aonghus-mor", author: "aonghus-mor") {
        capability "Switch"
        capability "Sensor"
        capability "Actuator"
        capability "Motion Sensor"	    		
    }
    /*
    simulator 
    {
		status "open": "contact:open"
		status "closed": "contact:closed"
	}
	*/
    tiles(scale: 2) 
    {
        multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4) 
        {
            tileAttribute("device.motion", key: "PRIMARY_CONTROL") 
            {
                attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#e86d13"
                attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#00a0dc"
            }
        }
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) 
        {
            state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
        }
        main "motion"
        details(["motion","switch"])
    }
}

def parse(description) 
{}

def on() 
{
    sendEvent(name: "switch", value: "on")
    sendEvent(name: "motion", value: "active")
    log.info "motion detected"
}

def off() 
{
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "motion", value: "inactive")
    log.info "motion ceased"
}