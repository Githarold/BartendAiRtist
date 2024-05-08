#!/usr/bin/python3
import os
from gi.repository import GLib
from pydbus import SystemBus

class BluetoothAgent:
    def __init__(self, bus, path):
        self.bus = bus
        self.path = path
        self.agent_manager = bus.get("org.bluez", "/org/bluez")
        self.agent_manager.RegisterAgent(self.path, "NoInputNoOutput")
        self.agent_manager.RequestDefaultAgent(self.path)

    def Release(self):
        print("Released")

    def AuthorizeService(self, device, uuid):
        print(f"AuthorizeService: {device}, {uuid}")
        return True

    def RequestPinCode(self, device):
        print(f"RequestPinCode: {device}")
        return '0000'

    def RequestPasskey(self, device):
        print(f"RequestPasskey: {device}")
        return 0

    def DisplayPinCode(self, device, pincode):
        print(f"DisplayPinCode: {device}, {pincode}")

    def DisplayPasskey(self, device, passkey, entered):
        print(f"DisplayPasskey: {device}, {passkey}, {entered}")

    def RequestConfirmation(self, device, passkey):
        print(f"RequestConfirmation: {device}, {passkey}")
        return True

    def ConfirmModeChange(self, mode):
        print(f"ConfirmModeChange: {mode}")
        return True

    def Cancel(self):
        print("Canceled")

def main():
    bus = SystemBus()
    path = "/test/agent"
    agent = BluetoothAgent(bus, path)
    loop = GLib.MainLoop()
    loop.run()

if __name__ == "__main__":
    main()
