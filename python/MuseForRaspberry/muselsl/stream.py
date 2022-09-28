from copy import deepcopy
import re
import subprocess
from time import time, sleep
from functools import partial
from shutil import which
import threading
import asyncio
import os
import websockets
from pylsl import StreamInfo, StreamOutlet, StreamInlet, resolve_byprop
from .muse import Muse
from .constants import MUSE_SCAN_TIMEOUT, AUTO_DISCONNECT_DELAY,  \
    MUSE_NB_EEG_CHANNELS, MUSE_SAMPLING_EEG_RATE, LSL_EEG_CHUNK,  \
    MUSE_NB_PPG_CHANNELS, MUSE_SAMPLING_PPG_RATE, LSL_PPG_CHUNK, \
    MUSE_NB_ACC_CHANNELS, MUSE_SAMPLING_ACC_RATE, LSL_ACC_CHUNK, \
    MUSE_NB_GYRO_CHANNELS, MUSE_SAMPLING_GYRO_RATE, LSL_GYRO_CHUNK, NUM_MAX_DEVICES_NEARBY, LSL_SCAN_TIMEOUT




data_sensor = '{"EEG":{"data":-1, "timestamp":-1}, "PPG":{"data":-1, "timestamp":-1}, "ACC":{"data":-1, "timestamp":-1}, "GYRO":{"data":-1, "timestamp":-1}}'

def _print_muse_list(muses):
    for m in muses:
        print(f'Found device {m["name"]}, MAC Address {m["address"]}')
    if not muses:
        print('No Muses found.')


# Returns a list of available Muse devices.
def list_muses(interface=None):
    if which('bluetoothctl') is not None:
        print("Bluetoothctl was found, using to list muses...")
        return _list_muses_bluetoothctl(MUSE_SCAN_TIMEOUT)


def _list_muses_bluetoothctl(timeout, verbose=True, verbose_timeout=False):
    """Identify Muse BLE devices using bluetoothctl.

    When using backend='gatt' on Linux, pygatt relies on the command line tool
    `hcitool` to scan for BLE devices. `hcitool` is however deprecated, and
    seems to fail on Bluetooth 5 devices. This function roughly replicates the
    functionality of `pygatt.backends.gatttool.gatttool.GATTToolBackend.scan()`
    using the more modern `bluetoothctl` tool.

    Deprecation of hcitool: https://git.kernel.org/pub/scm/bluetooth/bluez.git/commit/?id=b1eb2c4cd057624312e0412f6c4be000f7fc3617
    """
    try:
        import pexpect
    except (ImportError, ModuleNotFoundError):
        msg = ('pexpect is currently required to use bluetoothctl from within '
               'a jupter notebook environment.')
        raise ModuleNotFoundError(msg)

    # Run scan using pexpect as subprocess.run returns immediately in jupyter
    # notebooks
    if verbose: print('Searching for Muses, this may take up to 10 seconds...')
    scan = pexpect.spawn('bluetoothctl scan on')
    try:
        scan.expect('foooooo', timeout=timeout)
    except pexpect.EOF:
        before_eof = scan.before.decode('utf-8', 'replace')
        msg = f'Unexpected error when scanning: {before_eof}'
        raise ValueError(msg)
    except pexpect.TIMEOUT:
        if verbose_timeout: print(scan.before.decode('utf-8', 'replace').split('\r\n'))

    # List devices using bluetoothctl
    list_devices_cmd = ['bluetoothctl', 'devices']
    devices = subprocess.run(
        list_devices_cmd, stdout=subprocess.PIPE).stdout.decode(
            'utf-8').split('\n')

    if(len(devices)-1 > NUM_MAX_DEVICES_NEARBY): 
        if verbose: print("There may be errors in the connection, due to the presence of too many devices nearby.")

    muses = [{
            'name': re.findall('Muse.*', string=d)[0],
            'address': re.findall(r'..:..:..:..:..:..', string=d)[0]
        } for d in devices if 'Muse' in d]
    if verbose: _print_muse_list(muses)

    return muses


# Returns the address of the Muse with the name provided, otherwise returns address of first available Muse.
def find_muse(name=None):
    muses = list_muses()
    if name:
        for muse in muses:
            if muse['name'] == name:
                return muse
    elif muses:
        return muses[0]


# Begins LSL stream(s) from a Muse with a given address with data sources determined by arguments
def stream(
    address,
    interface=None,
    name=None,
    ppg_enabled=False,
    acc_enabled=False,
    gyro_enabled=False,
    eeg_disabled=False,
    preset=None,
    disable_light=False,
    timeout=AUTO_DISCONNECT_DELAY,
):
    # If no data types are enabled, we warn the user and return immediately.
    if eeg_disabled and not ppg_enabled and not acc_enabled and not gyro_enabled:
        print('Stream initiation failed: At least one data source must be enabled.')
        return

    # For any backend except bluemuse, we will start LSL streams hooked up to the muse callbacks.
    if not address:
        found_muse = find_muse(name)
        if not found_muse:
            return
        else:
            address = found_muse['address']
            name = found_muse['name']

    if not eeg_disabled:
        eeg_info = StreamInfo('Muse', 'EEG', MUSE_NB_EEG_CHANNELS, MUSE_SAMPLING_EEG_RATE, 'float32',
                            'Muse%s' % address)
        eeg_info.desc().append_child_value("manufacturer", "Muse")
        eeg_channels = eeg_info.desc().append_child("channels")

        for c in ['TP9', 'AF7', 'AF8', 'TP10', 'Right AUX']:
            eeg_channels.append_child("channel") \
                .append_child_value("label", c) \
                .append_child_value("unit", "microvolts") \
                .append_child_value("type", "EEG")

        eeg_outlet = StreamOutlet(eeg_info, LSL_EEG_CHUNK)

    if ppg_enabled:
        ppg_info = StreamInfo('Muse', 'PPG', MUSE_NB_PPG_CHANNELS, MUSE_SAMPLING_PPG_RATE,
                            'float32', 'Muse%s' % address)
        ppg_info.desc().append_child_value("manufacturer", "Muse")
        ppg_channels = ppg_info.desc().append_child("channels")

        for c in ['PPG1', 'PPG2', 'PPG3']:
            ppg_channels.append_child("channel") \
                .append_child_value("label", c) \
                .append_child_value("unit", "mmHg") \
                .append_child_value("type", "PPG")

        ppg_outlet = StreamOutlet(ppg_info, LSL_PPG_CHUNK)

    if acc_enabled:
        acc_info = StreamInfo('Muse', 'ACC', MUSE_NB_ACC_CHANNELS, MUSE_SAMPLING_ACC_RATE,
                            'float32', 'Muse%s' % address)
        acc_info.desc().append_child_value("manufacturer", "Muse")
        acc_channels = acc_info.desc().append_child("channels")

        for c in ['X', 'Y', 'Z']:
            acc_channels.append_child("channel") \
                .append_child_value("label", c) \
                .append_child_value("unit", "g") \
                .append_child_value("type", "accelerometer")

        acc_outlet = StreamOutlet(acc_info, LSL_ACC_CHUNK)

    if gyro_enabled:
        gyro_info = StreamInfo('Muse', 'GYRO', MUSE_NB_GYRO_CHANNELS, MUSE_SAMPLING_GYRO_RATE,
                            'float32', 'Muse%s' % address)
        gyro_info.desc().append_child_value("manufacturer", "Muse")
        gyro_channels = gyro_info.desc().append_child("channels")

        for c in ['X', 'Y', 'Z']:
            gyro_channels.append_child("channel") \
                .append_child_value("label", c) \
                .append_child_value("unit", "dps") \
                .append_child_value("type", "gyroscope")

        gyro_outlet = StreamOutlet(gyro_info, LSL_GYRO_CHUNK)

    def push(data, timestamps, outlet):
        for ii in range(data.shape[1]):
            outlet.push_sample(data[:, ii], timestamps[ii])

    push_eeg = partial(push, outlet=eeg_outlet) if not eeg_disabled else None
    push_ppg = partial(push, outlet=ppg_outlet) if ppg_enabled else None
    push_acc = partial(push, outlet=acc_outlet) if acc_enabled else None
    push_gyro = partial(push, outlet=gyro_outlet) if gyro_enabled else None

    muse = Muse(address=address, callback_eeg=push_eeg, callback_ppg=push_ppg, callback_acc=push_acc, callback_gyro=push_gyro, interface=interface, name=name, preset=preset, disable_light=disable_light)

    didConnect = muse.connect()

    if(didConnect):
        print('Connected.')
        muse.start()

        eeg_string = " EEG" if not eeg_disabled else ""
        ppg_string = " PPG" if ppg_enabled else ""
        acc_string = " ACC" if acc_enabled else ""
        gyro_string = " GYRO" if gyro_enabled else ""

        print("Streaming%s%s%s%s..." %
            (eeg_string, ppg_string, acc_string, gyro_string))

        while time() - muse.last_timestamp < timeout:
            try:
                sleep(1)
            except KeyboardInterrupt:
                muse.stop()
                muse.disconnect()
                break

        print('Disconnected.')




def muse_data_websocket(name, address_ws, port_ws):
    global data_sensor

    web_socket_server = WebSocketServer(address_ws=address_ws, port_ws=port_ws)
    web_socket_server.start()

    while True:
        found_muse = find_muse(name)
        if not found_muse: pass
        else:
            address = found_muse['address']
            name = found_muse['name']


            eeg_samples = []
            ppg_samples = []
            acc_samples = []
            gyro_samples = []

            lock_eeg = threading.Lock()
            lock_ppg = threading.Lock()
            lock_acc = threading.Lock()
            lock_gyro = threading.Lock()


            def save_data(data, timestamps, outlet, lock):
                with lock:
                    if type(data) != list: data = data.tolist()
                    if type(timestamps) != list: timestamps = timestamps.tolist()

                    for sample, timestamp in zip(data, timestamps):
                        outlet.append({"data":sample, "timestamp":timestamp})


            save_eeg = partial(save_data, outlet=eeg_samples, lock=lock_eeg)
            save_ppg = partial(save_data, outlet=ppg_samples, lock=lock_ppg)
            save_acc = partial(save_data, outlet=acc_samples, lock=lock_acc)
            save_gyro = partial(save_data, outlet=gyro_samples, lock=lock_gyro)

            muse = Muse(address, callback_eeg=save_eeg, callback_ppg=save_ppg, callback_acc=save_acc, callback_gyro=save_gyro)
            didConnect = muse.connect()
            if(didConnect):
                print("Connected")
                muse.start()

                data_sensor_temporary = {}

                sleep(1)

                

                while True:

                    data_sensor_temporary["EEG"] = get_data(eeg_samples, lock_eeg)
                    data_sensor_temporary["PPG"] = get_data(ppg_samples, lock_ppg)
                    data_sensor_temporary["ACC"] = get_data(acc_samples, lock_acc)
                    data_sensor_temporary["GYRO"] = get_data(gyro_samples, lock_gyro)

                    if data_sensor_temporary["EEG"] == {"data":-1, "timestamp":-1} and data_sensor_temporary["PPG"] == {"data":-1, "timestamp":-1} and  data_sensor_temporary["ACC"] == {"data":-1, "timestamp":-1} and  data_sensor_temporary["GYRO"] == {"data":-1, "timestamp":-1}:
                        data_sensor = '{"EEG":{"data":-1, "timestamp":-1}, "PPG":{"data":-1, "timestamp":-1}, "ACC":{"data":-1, "timestamp":-1}, "GYRO":{"data":-1, "timestamp":-1}}'
                        try: muse.stop()
                        except: pass
                        try: muse.disconnect()
                        except: pass
                        try: del muse
                        except: pass
                        muse=None
                        print("Muse timeout\nDisconnected")
                        sleep(1)
                        os.system("systemctl stop bluetooth")
                        sleep(3)
                        os.system("systemctl start bluetooth")
                        sleep(1)
                        break

            
                    data_sensor = str(data_sensor_temporary)

                    

                    #print(data_sensor)



def get_data(array_samples, lock):
    sleep(0.03) # to be increased in case of problems
    with lock:
        if len(array_samples) > 0: 
            sample = deepcopy(array_samples.pop(len(array_samples)-1))
            array_samples.clear()
            return sample

        else: return {"data":-1, "timestamp":-1}



def pull_chunk_sensor_data(var_inlet, var_chunk_length, var_data_source, var_temporary_data_sensor):
    try:
        data, timestamp = var_inlet.pull_chunk(timeout=2.0,max_samples=var_chunk_length)
        if timestamp: var_temporary_data_sensor[var_data_source] = {"data": data[0], "timestamp": timestamp[0]}
    except Exception as error: print(error)


def pull_sample_marker_data(var_inlet_marker, var_temporary_data_sensor):
    try:
        marker, timestamp = var_inlet_marker.pull_sample(timeout=2.0)
        if timestamp: var_temporary_data_sensor["marker"] = {"data": marker[0], "timestamp": timestamp[0]}
    except Exception as error: print(error)



class ErrorMuseTimeout(Exception):
    pass

class WebSocketServer(threading.Thread):
    def __init__(self, address_ws, port_ws):
        threading.Thread.__init__(self)
        self.address_ws = address_ws
        self.port_ws = port_ws
        self.start_server = None
        self.loop = None

    def run(self):

        print(f"Web socket started on ('{self.address_ws}',{self.port_ws})")

        async def handler(websocket, path):
            dict_prev_data_sensor = {"EEG":{"timestamp":-1}}
            while True:
                dict_data_sensor = eval(data_sensor)
                if dict_data_sensor["EEG"]["timestamp"] != dict_prev_data_sensor["EEG"]["timestamp"]:
                    await websocket.send(str(dict_data_sensor))
                dict_prev_data_sensor = dict_data_sensor
                sleep(0.05)

        self.loop = asyncio.new_event_loop()
        self.start_server = websockets.serve(handler, self.address_ws, self.port_ws, loop=self.loop)
        self.loop.run_until_complete(self.start_server)
        self.loop.run_forever()
        #asyncio.get_event_loop().run_until_complete(self.start_server)
        #asyncio.get_event_loop().run_forever()