
definition(
    name: "Smart Lock Delay",
    namespace: "paqogomez",
    author: "paqogomez@hotmail.com",
    description: "Throw lock event after X minutes of door being closed, reset when door is open",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
  section("When the door closes...") {
    input name: "openSensor", type: "capability.contactSensor", title: "Door Sensor"
  }

  section("Lock it..") {
    input name: "lockSensor", type: "capability.lock", title: "Lock"
  }

  section("After...") {
    input name: "smartDelay", type: "number", title: "Minutes", required: false
  }

}

def installed() {
  init()
}

def updated() {
  unsubscribe()
  init()
}

def init() {
  state.lastClosed = 0
  state.lastOpened = 0
  state.lastLocked = 0
  state.lastUnlocked = 0
  subscribe(lockSensor, "lock.lock", handleEvent)
  subscribe(lockSensor, "lock.unlock", handleEvent)
  subscribe(openSensor, "contact.closed", doorClosed)
  subscribe(openSensor, "contact.open", doorOpened)
}

def doorClosed(evt) {
  state.lastClosed = now()  
  def delay = knockDelay ?: 0
  log.debug($"Door closed at {now()}. Lock to trigger in {delay} minutes.")
  runIn(delay * 60, "lockDoor")
}

def doorOpened(evt) {
  state.lastOpened = now()
  log.debug($"Door opened at {now()}.")
  lockSensor.Unlock
}

def lockLocked(evt) {
  state.lastLocked = now()
}

def lockUnlocked(evt) {
  state.lastUnlocked = now()
  def delay = knockDelay ?: 0
  log.debug($"{evt}")
  log.debug($"Unlocked at {now()}.")
  runIn(0, "lockDoor", [data: [flag: true]])
}

def lockDoor(data) {
  if(data && data.flag) {
    return
  }
  lockSensor.lock
}
