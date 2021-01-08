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
metadata 
{
    definition (name: "Simulated Ring Alexa Contact", namespace: "aonghus-mor", author: "aonghus-mor",
    			vid: "generic-contact-4", ocfDeviceType: 'x.com.st.d.sensor.contact') 
    {
        capability "Contact Sensor"	
        capability "Sensor"
        capability "Switch"
        capability "Actuator"
            		
    }
}

def parse(description)
{}

def on() 
{
    sendEvent(name: "switch", value: "on", displayed: false)
    sendEvent(name: "contact", value: "open")
    log.info "contact opened"
}

def off()
{
    sendEvent(name: "switch", value: "off", displayed: false)
    sendEvent(name: "contact", value: "closed")
    log.info "contact closed"
}