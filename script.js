const lanes=["North","East","South","West"];
const queues={North:[],East:[],South:[],West:[]};
const emergencyQueue=[];
let activeLane=0,vehicleNumber=1,emergencyMode=false,passed=0;
const $=id=>document.getElementById(id);
const priority={Ambulance:1,"Fire Engine":2,"Police Vehicle":3};
const icons=["🚙","🚗","🚕","🚐","🚘","🏍️"];
function log(message){const li=document.createElement("li");li.textContent=new Date().toLocaleTimeString()+" — "+message;$("log").prepend(li);while($("log").children.length>6)$("log").lastChild.remove();}
function laneIndex(){return activeLane;}
function normalCount(){return lanes.reduce((n,l)=>n+queues[l].length,0);}
function densityAmount(){const d=$("density").value;return d==="Low"?1:d==="Medium"?2:4;}
function addNormal(lane){for(let i=0;i<densityAmount();i++)queues[lane].push({id:vehicleNumber++,name:"Car",icon:icons[Math.floor(Math.random()*icons.length)]});log(`${densityAmount()} normal vehicle(s) added to ${lane} lane.`);render();}
function addEmergency(type){const lane=$("emergencyLane").value;emergencyQueue.push({id:vehicleNumber++,name:type,lane,priority:priority[type],icon:type==="Ambulance"?"🚑":type==="Fire Engine"?"🚒":"🚓"});emergencyQueue.sort((a,b)=>a.priority-b.priority||a.id-b.id);log(`${type} added to ${lane} lane. Emergency priority enabled.`);render();}
function nextSignal(){if(emergencyQueue.length){activeLane=lanes.indexOf(emergencyQueue[0].lane);emergencyMode=true;log(`Emergency priority: ${emergencyQueue[0].name} in ${lanes[activeLane]} lane.`);}else{emergencyMode=false;activeLane=(activeLane+1)%4;const d=$("density").value;log(`${d} traffic: ${lanes[activeLane]} lane has the green signal.`);}render();}
function passVehicle(){if(emergencyQueue.length&&emergencyMode&&lanes[activeLane]===emergencyQueue[0].lane){const v=emergencyQueue.shift();passed++;log(`${v.name} #${v.id} passed through the intersection.`);emergencyMode=false;render();return;}const lane=lanes[activeLane];if(!queues[lane].length){log(`No normal vehicles waiting in ${lane} lane.`);}else{const v=queues[lane].shift();passed++;log(`${v.name}-${v.id} passed from ${lane} lane.`);}render();}
function reset(){lanes.forEach(l=>queues[l].length=0);emergencyQueue.length=0;activeLane=0;vehicleNumber=1;emergencyMode=false;passed=0;$("log").innerHTML="<li>Queues reset. Add vehicles to begin.</li>";render();}
function render(){const lane=lanes[activeLane];$("normalCount").textContent=normalCount();$("emergencyCount").textContent=emergencyQueue.length;$("activeSignal").textContent=lane;$("passedCount").textContent=passed;$("northTotal").textContent=queues.North.length+" waiting";$("eastTotal").textContent=queues.East.length+" waiting";$("southTotal").textContent=queues.South.length+" waiting";$("westTotal").textContent=queues.West.length+" waiting";$("modeBadge").textContent=emergencyMode?"Emergency priority": "Normal traffic";$("emergencyBanner").classList.toggle("show",emergencyQueue.length>0);$("signalNote").textContent=emergencyMode&&emergencyQueue[0]?`${emergencyQueue[0].name} has priority in the ${lane} lane.`:`${lane} lane currently has the green signal.`;$("status").textContent=emergencyMode&&emergencyQueue[0]?`Emergency priority: ${emergencyQueue[0].name} in ${lane} lane.`:`${lane} lane selected. Traffic density: ${$("density").value}.`;for(const id of ["lightRed","lightYellow","lightGreen"])$(id).classList.remove("active");$(emergencyMode?"lightGreen":"lightGreen").classList.add("active");$("signalText").textContent=emergencyMode?"GREEN - EMERGENCY":"GREEN";$("queues").innerHTML=lanes.map(l=>{const emergencies=emergencyQueue.filter(v=>v.lane===l).map(v=>`<span class="chip emergency" title="${v.name}">${v.icon}</span>`).join("");const normal=queues[l].map(v=>`<span class="chip" title="${v.name}-${v.id}">${v.icon}</span>`).join("");return `<div class="queue-card ${lanes[activeLane]===l?"active":""}"><h3>${l} Lane</h3><p>${queues[l].length} normal • ${emergencyQueue.filter(v=>v.lane===l).length} emergency</p><div class="chips">${emergencies}${normal||(!emergencies?'<span class="empty">No vehicles waiting</span>':'')}</div></div>`;}).join("");}
lanes.forEach(lane=>{const card=document.createElement("div");});
$("addAmbulance").addEventListener("click",()=>addEmergency("Ambulance"));
$("addFire").addEventListener("click",()=>addEmergency("Fire Engine"));
$("addPolice").addEventListener("click",()=>addEmergency("Police Vehicle"));
$("nextSignal").addEventListener("click",nextSignal);
$("passVehicle").addEventListener("click",passVehicle);
$("reset").addEventListener("click",reset);
$("density").addEventListener("change",()=>{log("Traffic density set to "+$("density").value+".");render();});
// Each lane queue gets a quick-add button below the lane cards, mirroring the Java application's lane-specific buttons.
function addLaneButtons(){document.querySelectorAll(".queue-card").forEach((card,i)=>{if(card.querySelector("button"))return;const b=document.createElement("button");b.className="secondary";b.textContent="＋ Add normal vehicle";b.style.marginTop="10px";b.style.width="100%";b.addEventListener("click",()=>addNormal(lanes[i]));card.appendChild(b);});}
const oldRender=render;
render=function(){oldRender();addLaneButtons();};
render();
