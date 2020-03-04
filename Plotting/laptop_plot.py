from pathlib import Path
from hashlib import md5
import matplotlib.pyplot as plt
import operator
import time
import datetime

BLOCKSIZE = 65536

drive_dir = 'C:\\Users\\User\Google Drive'

last_scanned_files = []
datapoints = []

class DataPoint:
    """A class to store the information associated with a single data point (date and value)"""

    def __init__(self, stationID, year, month, day, hour, minute, second, voltage):
        self.stationID = stationID
        self.year = int(year)
        self.month = int(month)
        self.day = int(day)
        self.hour = int(hour)
        self.minute = int(minute)
        self.second = int(second)
        self.voltage = float(voltage)

    def __str__(self):
        return "[%s %d-%02d-%02d %02d:%02d:%02d] %.2fV" % (
            self.stationID, self.year, self.month, self.day, self.hour, self.minute, self.second, self.voltage)

    def __eq__(self, other):
        return self.stationID == other.stationID and self.year == other.year and self.month == other.month and self.day == other.day and self.hour == other.hour and self.minute == other.minute and self.second == other.second and self.voltage == other.voltage


def disp_dir_contents(path):
    entries = Path(path)
    for entry in entries.iterdir():
        try:
            hasher = md5()
            file = open(path + '\\' + entry.name, 'rb')
            buf = file.read(BLOCKSIZE)
            while len(buf) > 0:
                hasher.update(buf)
                buf = file.read(BLOCKSIZE)
            print(entry.name + "\n\t" + hasher.hexdigest())
        except PermissionError:
            pass

def scan_directory(path):
    global last_scanned_files
    new_files = False
    entries = Path(path)
    this_scan_files = []
    for entry in entries.iterdir():
        try:
            hasher = md5()
            file = open(path + '\\' + entry.name, 'rb')
            buf = file.read(BLOCKSIZE)
            while len(buf) > 0:
                hasher.update(buf)
                buf = file.read(BLOCKSIZE)
            hash = hasher.hexdigest()
            file.close()
            if (not hash in this_scan_files):
                this_scan_files.append(hash)
            if (not hash in last_scanned_files):
                new_files = True
                datapoints_file = parseFile(path + '\\' + entry.name)
                for point in datapoints_file:
                    if not point in datapoints:
                        datapoints.append(point)
        except PermissionError:
            pass
    last_scanned_files = this_scan_files
    return new_files

def disp_drive_contents():
    disp_dir_contents(drive_dir)

def parseFile(filename):
    """
    Open filename (full path) and return an array of DataPoints for all data in the file
    """
    file = open(filename)
    datapoints = []
    #firstLine = file.readline();
    #if (not "MONTH" in firstLine):
    #    return datapoints
    try:
        for line in file:
            if "MONTH" in line:
                continue
            try:
                [lineNo, id, year, month, day, time, voltage] = line.strip().replace("\"", '').split(",")
            except ValueError:
                file.close()
                return []
            [hour, minute, second] = time.split(":")
            datapoint = DataPoint(id, year, month, day, hour, minute, second, voltage)
            datapoints.append(datapoint)
        file.close()
    except:
        file.close()
        return []
    return datapoints

def sortPoints(datapoints):
    datapoints.sort(key=operator.attrgetter("stationID", "year", "month", "day", "hour", "minute", "second"))

def plotPoints(datapoints, fig = None):
    if (fig is None):
        fig = plt.figure()
    voltages = [point.voltage for point in datapoints]
    plt.plot(voltages)
    plt.show()
    return fig

plt.ion()
fig = None

while (True):
    is_change = scan_directory(drive_dir)
    if is_change:
        fig = plotPoints(datapoints, fig)
        print("Updated at", datetime.datetime.now())
    time.sleep(10)