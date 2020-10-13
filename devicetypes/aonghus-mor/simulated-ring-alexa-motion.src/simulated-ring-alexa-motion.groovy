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
    definition (name: "Simulated Ring Alexa Motion", namespace: "aonghus-mor", author: "aonghus-mor",
    			vid: "generic-motion-9") 
    {
        capability "Switch"
        capability "Sensor"
        capability "Actuator"
        capability "Motion Sensor"	
        capability "Health Check"
    }
}

def parse(description) 
{}

def on() 
{
    sendEvent(name: "switch", value: "on", displayed: false)
    sendEvent(name: "motion", value: "active")
    log.info "motion detected"
}

def off() 
{
    sendEvent(name: "switch", value: "off", displayed: false)
    sendEvent(name: "motion", value: "inactive")
    log.info "motion ceased"
}

def ping()
{
	def stt = device.currentValue('motion')
	sendEvent(name: 'switch', value: stt)
}